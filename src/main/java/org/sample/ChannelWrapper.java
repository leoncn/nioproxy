package org.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by U0128754 on 12/14/2015.
 */
class ChannelWrapper {

    private Logger logger = LogManager.getLogger();

    private SelectionKey key;
    private SocketChannel sc;
    private IHandler handler;

    private boolean hasError = false;

    private int intrestingOps = 0, readyOps = 0;

    private IOutputQueue<String> outputQ = null;
    private IInputQueue<String> inputQ = null;
    private ByteBuffer buf = ByteBuffer.allocate(1024);

    public ChannelWrapper(SelectionKey key, SocketChannel sc, IHandler handler) {
        this.key = key;
        this.sc = sc;
        this.handler = handler;
    }

    public SelectionKey getKey() {
        return this.key;
    }

    public void prepare() {
        this.intrestingOps = this.key.interestOps();
        this.readyOps = this.key.readyOps();

        this.key.interestOps(0); //disable all ops

        if (handler.getOutputQ() == null) {
            handler.setOutputQ(new StringBuilderOutputQueue());
        }
        outputQ = handler.getOutputQ();

        if (handler.getInputQ() == null) {
            handler.setInputQ(new StringBuilderInputQueue());
        }
        inputQ = this.handler.getInputQ();
    }

    public void process() throws IOException {
        try {
            logger.info("in process.");
            this.drainOutput();
            this.fillInput();
            handler.handle();
        } catch (IOException e) {
            hasError = true;
            throw e;
        }
    }

    public boolean isDone() {
        return hasError || this.writeDisabled() && this.readDisabled();
    }

    private void fillInput() throws IOException {
        int nr = sc.read(buf);

        if (nr == -1) {
            this.disableRead();
            this.sc.shutdownInput();
            logger.info("on inputs, closing input stream.");
            return;
        }

        enableRead();
        buf.flip();


        while (buf.hasRemaining()) {
            final byte[] bytes = new byte[buf.remaining()];
            buf = buf.get(bytes, 0, buf.remaining());
             handler.getInputQ().equeue(bytes);
        }

        buf.compact();
    }

    private void drainOutput() throws IOException {

        Object msg = this.handler.getOutputQ().dequeue();
       // buf.clear();

        if (msg != null) {
            this.enableWrite();
            do {
                sc.write(ByteBuffer.wrap(msg.toString().getBytes()));
//                byte[] bytes = msg.toString().getBytes();
//                int nw = 0, remain = bytes.length;
//
//                while (nw < remain && buf.hasRemaining()) {
//                    int len = Math.min(remain, buf.remaining());
//                    buf.put(bytes, nw, len);
//                    nw += len;
//                }
//
//                buf.flip();
//                logger.info("res" + new String(buf.array()));
//                while (buf.hasRemaining()) {
//                    sc.write(buf);
//                }
            } while ((msg = this.handler.getOutputQ().dequeue()) != null);
        }
    }

    public void restoreOps() {
        this.key.interestOps(this.intrestingOps);
    }

    private void enableRead() {
        intrestingOps |= SelectionKey.OP_READ;
    }

    private void disableRead() {
        intrestingOps ^= SelectionKey.OP_READ;
    }

    private boolean readDisabled() {
        return (intrestingOps & SelectionKey.OP_READ) == 0;
    }

    private void enableWrite() {
        intrestingOps |= SelectionKey.OP_WRITE;
    }

    private void disableWrite() {
        intrestingOps ^= SelectionKey.OP_WRITE;
    }

    private boolean writeDisabled() {
        return (intrestingOps & SelectionKey.OP_WRITE) == 0;
    }

    private class StringBuilderOutputQueue implements IOutputQueue<String> {
        private StringBuilder builder = new StringBuilder();

        @Override
        public void enqueue(byte[] bytes) {
            enableWrite();
           builder.append(new String(bytes));
        }

        @Override
        public String dequeue() {
            int lineFeed = this.builder.indexOf("\n");

            if (lineFeed < 0) {
                return null;
            }

            String temp = this.builder.substring(0, lineFeed);
            this.builder = this.builder.delete(0, lineFeed + 1);
            return temp;
        }
    }

    private class StringBuilderInputQueue implements IInputQueue<String> {
        private StringBuilder builder = new StringBuilder();

        @Override
        public void equeue(byte[] bytes) {
            builder.append(new String(bytes));
        }

        @Override
        public String nextMessage() {
            int lineFeed = this.builder.indexOf("\n");

            if (lineFeed < 0) {
                return null;
            }

            String temp = this.builder.substring(0, lineFeed);
            this.builder = this.builder.delete(0, lineFeed + 1);
            return temp;
        }
    }
}
