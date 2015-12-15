package org.sample;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Created by U0128754 on 12/14/2015.
 */
public class Reactor {

    private final Logger logger = LogManager.getLogger();

    private final Selector sel;

    private final ReentrantReadWriteLock srwLock = new ReentrantReadWriteLock();

    private final ForkJoinPool pool = new ForkJoinPool();

    private volatile Future<?> dispatcher = null;

    private ArrayBlockingQueue<ChannelWrapper> completeHandlers = new ArrayBlockingQueue<>(10);

    private Runnable dispatchTask = null;

    private Consumer<SelectionKey> handleIOEvent = null;

    public Reactor() throws IOException {
        this.sel = Selector.open();

        dispatchTask = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                this.clearHandlerQueue();
                try {
                    selectorBarrier();
                    this.sel.select();
                    this.sel.selectedKeys().forEach(handleIOEvent);
                    this.sel.selectedKeys().clear();
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        };

        handleIOEvent = (key) -> {

            logger.printf(Level.INFO, "channel key : %s %s, %s%n", key.hashCode(), key.isReadable(), key.isWritable());
            ChannelWrapper handler = (ChannelWrapper) key.attachment();
            handler.prepare();

//            RecursiveAction task = new RecursiveAction() {
//                @Override
//                protected void compute() {
//                    try {
//                        handler.process();
//                    } catch (IOException e) {
//                        logger.error(e);
//                    } finally {
//                        logger.info("process done.");
//                        completeHandlers.add(handler);
//                        sel.wakeup();
//                    }
//                }
//            };
//
//            task.fork();
            Runnable task = () -> {
                try {
                    handler.process();
                } catch (IOException e) {
                    logger.error(e);
                } finally {
                    logger.info("process done.");
                    completeHandlers.add(handler);
                    sel.wakeup();
                }
            };

            this.pool.submit(task);

        };
    }

    public void start() {
        if (this.dispatcher != null) {
            return;
        }
        this.dispatcher = this.pool.submit(this.dispatchTask);
    }

    public void stop() throws InterruptedException {
        this.dispatcher.cancel(true);
        this.pool.awaitTermination(5, TimeUnit.SECONDS);
        this.dispatcher = null;
    }

    public ChannelWrapper registerChannel(SocketChannel sc, IHandler handler) throws IOException {
        acquireSelectorRLock();
        try {
            sc.configureBlocking(false);
            SelectionKey key = sc.register(this.sel, SelectionKey.OP_READ);
            ChannelWrapper wrapper = new ChannelWrapper(key, sc, handler);
            key.attach(wrapper);
            logger.printf(Level.INFO, "accept a new connection form %s %n", sc.getRemoteAddress());
            return wrapper;
        } finally {
            releaseSelectorRLock();
        }
    }

    public void unRegisterChannel(ChannelWrapper wrapper) throws IOException {
        acquireSelectorRLock();
        try {
            SelectionKey key = wrapper.getKey();
            key.cancel();
            key.channel().close();
        } finally {
            releaseSelectorRLock();
        }
    }

    private void selectorBarrier() {
        srwLock.writeLock().lock();
        srwLock.writeLock().unlock();
    }

    private void acquireSelectorRLock() {
        srwLock.readLock().lock();
        this.sel.wakeup();
    }

    private void releaseSelectorRLock() {
        srwLock.readLock().unlock();
    }

    private void clearHandlerQueue() {
        this.completeHandlers.forEach(handler -> {
            if (handler.isDone()) {
                try {
                    this.unRegisterChannel(handler);
                } catch (IOException e) {
                    logger.error(e);
                }
            } else {
                handler.restoreOps();
            }
        });
    }
}
