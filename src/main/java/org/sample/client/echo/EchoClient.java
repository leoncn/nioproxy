package org.sample.client.echo;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Created by U0128754 on 12/16/2015.
 */
public class EchoClient {
    private static final String CONN_PIPE_KEY = "PIPE";

    private static Logger logger = LogManager.getLogger();

    private SocketAddress remoteAddr = null;
    private NioSocketConnector connector = new NioSocketConnector();

    public EchoClient(SocketAddress remoteAddr) {
        this.remoteAddr = remoteAddr;
        this.connector.setConnectTimeoutCheckInterval(3 * 1000L);
        connector.setHandler(new Handler());
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(
                new TextLineCodecFactory()
        ));
    }

    public static void main(String[] args) throws InterruptedException {

        int argc = args.length;

        if (argc < 2) {
            throw new IllegalArgumentException("host port.");
        }

        EchoClient client = new EchoClient(new InetSocketAddress(args[0], Integer.parseInt(args[1])));

        ConcurrentHashMap<ConnecionPipe, AtomicLong> cache = new ConcurrentHashMap<>();

       // ForkJoinPool fjp = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newCachedThreadPool();

        int N = 10240, M = 11;
        CountDownLatch doneSignal = new CountDownLatch(M);
        CountDownLatch startSinal = new CountDownLatch(M + 1);


        Runnable task = () -> {
          //  int i = 0;
            try {
                ConnecionPipe pipe = client.createConnect(Thread.currentThread().getName(),
                        (cp, bytes) -> {
                            AtomicLong v = cache.computeIfPresent(cp, (key, value) -> {
                                value.incrementAndGet();
                                return value;
                            });

                            logger.printf(Level.INFO, "%s : %d", cp.getName(), v.get());
                            if (v.get() == N) {
                                logger.printf(Level.INFO, "%s has done.", cp.getName());
                                doneSignal.countDown();
                                cp.shutdown();
                            }
                        }
                );

                cache.put(pipe, new AtomicLong(0L));
                pipe.ready();

                startSinal.countDown();
                startSinal.await();
                IntStream.range(0, N).forEach(i -> pipe.send(() -> String.format("MSG-#%d %s", i, Thread.currentThread().getName())));


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        IntStream.range(0, M).forEach(i -> pool.submit(task));

        startSinal.countDown();
        doneSignal.await();
        cache.forEach((key, v) -> client.logger.printf(Level.INFO, "%s:%s%n", key.hashCode(), v));
        logger.printf(Level.INFO, "All %d * %d have been done.", M, N);

    }

    public ConnecionPipe createConnect(String name, BiConsumer<? super ConnecionPipe, byte[]> consumer) throws InterruptedException {
        ConnectFuture future = this.connector.connect(this.remoteAddr);
        future.await();

        IoSession session = future.getSession();
        ConnecionPipe pipe = new ConnecionPipe(name, consumer);

        pipe.setSession(session);
        session.setAttribute(CONN_PIPE_KEY, pipe);

        return pipe;
    }

    private class ConnecionPipe {
        private IoSession session = null;
        private String name;
        private BiConsumer<? super ConnecionPipe, byte[]> consumer = null;

        public ConnecionPipe(String name, BiConsumer<? super ConnecionPipe, byte[]> consumer) {
            this.name = name;
            this.consumer = consumer;
        }

        public String getName() {
            return this.name + " R: " + this.session.getReadMessages() + " W: " + session.getWrittenMessages();
        }

        public void setSession(IoSession session) {
            this.session = session;
        }

        public boolean ready() {
            this.session.resumeRead();
            this.session.resumeWrite();

            return !(this.session.isReadSuspended() || this.session.isWriteSuspended());
        }


        public void send(Supplier<String> supplier) {
            this.session.write(supplier.get());
        }

        void receive(byte[] bytes) {
            consumer.accept(this, bytes);
        }

        public void shutdown() {
            this.session.close(true);
        }
    }

    private class Handler extends IoHandlerAdapter {
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            super.sessionCreated(session);
            session.suspendRead();
            session.suspendWrite();

            InetSocketAddress remote = (InetSocketAddress) session.getRemoteAddress();

            logger.printf(Level.INFO, "Connect to %s:%d%n", remote.getHostName(), remote.getPort());
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            super.sessionOpened(session);
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            super.exceptionCaught(session, cause);
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            super.messageReceived(session, message);
            ConnecionPipe pipe = (ConnecionPipe) session.getAttribute(CONN_PIPE_KEY);
            pipe.receive(((String) message).getBytes());
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
        }

        @Override
        public void inputClosed(IoSession session) throws Exception {
            super.inputClosed(session);
        }
    }
}


