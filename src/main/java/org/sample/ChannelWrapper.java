package org.sample;

import org.apache.logging.log4j.Level;
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

    private int reqCnt = 0;
    private int resCnt = 0;

    private int intrestingOps = 0, readyOps = 0;
    private ByteBuffer inputBuf = ByteBuffer.allocate(1024);

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

        if (handler.getInputQ() == null) {
            handler.setInputQ(new StringBuilderInputQueue());
        }
    }

    public void process() throws IOException {
        try {
            this.fillInput();
            this.drainOutput();
            handler.handle();
        } catch (IOException e) {
            this.offWriteOps();
            this.offReadOps();
            throw e;
        }
    }

    public boolean isDone() {
        boolean done = this.isWriteOpsOff() && this.isReadOpsOff();
        logger.printf(Level.INFO, " total req %d , res %d.", this.reqCnt, this.resCnt);

        return done;
    }

    private void fillInput() throws IOException {
        int nr = sc.read(inputBuf);

        if (nr == -1) {
            this.offReadOps();
            this.sc.shutdownInput();
            logger.printf(Level.INFO, "No inputs, closing input stream.%n");
            return;
        }

        do {
            inputBuf.flip();
            final byte[] bytes = new byte[inputBuf.remaining()];
            inputBuf = inputBuf.get(bytes, 0, inputBuf.remaining());
            handler.getInputQ().equeue(bytes);
            inputBuf.compact();
        } while ((nr = sc.read(inputBuf)) > 0);

        onReadOps();
    }

    private void drainOutput() throws IOException {

        Object msg = this.handler.getOutputQ().dequeue();
        boolean writePending = msg != null;

        if (writePending) {
            do {
                sc.write(ByteBuffer.wrap(msg.toString().getBytes()));
            } while ((msg = this.handler.getOutputQ().dequeue()) != null);
        }

        if (this.handler.getOutputQ().isEmpty() && !writePending) {
            this.offWriteOps();
        } else {

            this.onWriteOps();
        }
    }

    public void restoreOps() {
        this.key.interestOps(this.intrestingOps);
    }

    private void onReadOps() {
        intrestingOps |= SelectionKey.OP_READ;
    }

    private void offReadOps() {
        intrestingOps &= ~SelectionKey.OP_READ;
    }

    private boolean isReadOpsOff() {
        return (intrestingOps & SelectionKey.OP_READ) == 0;
    }

    private void onWriteOps() {
        intrestingOps |= SelectionKey.OP_WRITE;
    }

    private void offWriteOps() {
        intrestingOps &= ~SelectionKey.OP_WRITE;
    }

    private boolean isWriteOpsOff() {
        return (intrestingOps & SelectionKey.OP_WRITE) == 0;
    }

    private class StringBuilderOutputQueue implements IOutputQueue<String> {
        private StringBuilder builder = new StringBuilder();

        @Override
        public void enqueue(byte[] bytes) {
            onWriteOps();
            builder.append(new String(bytes));
        }

        @Override
        public String dequeue() {
            int lineFeed = this.builder.indexOf("\n");

            if (lineFeed < 0) {
                return null;
            }

            resCnt++;
            String temp = this.builder.substring(0, lineFeed + 1);
            this.builder = this.builder.delete(0, lineFeed + 1);
            return temp;
        }

        @Override
        public boolean isEmpty() {
            return builder.length() == 0;
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

            reqCnt++;
            String temp = this.builder.substring(0, lineFeed);
            this.builder = this.builder.delete(0, lineFeed + 1);
            return temp;
        }

        @Override
        public boolean isEmpty() {
            return builder.length() == 0;
        }
    }
}
