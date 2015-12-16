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

    private boolean hasError = false;

    private int intrestingOps = 0, readyOps = 0;

    private IOutputQueue<String> outputQ = null;
    private IInputQueue<String> inputQ = null;
    private ByteBuffer inputBuf = ByteBuffer.allocate(1024);
    private ByteBuffer outputBuf = ByteBuffer.allocate(1024);

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
            logger.printf(Level.INFO, "in process.%n");
            this.fillInput();
            this.drainOutput();
            handler.handle();
        } catch (IOException e) {
            hasError = true;
            throw e;
        }
    }

    public boolean isDone() {
        return hasError || this.isWriteDisabled() && this.isReadDisabled();
    }

    private void fillInput() throws IOException {
        int nr = sc.read(inputBuf);

        if (nr == -1) {
            this.disableRead();
            this.sc.shutdownInput();
            logger.printf(Level.INFO,"on inputs, closing input stream.%n");
            return;
        }

        logger.printf(Level.INFO, "read %d bytes.%n", nr);
        inputBuf.flip();

        while (inputBuf.hasRemaining()) {
            final byte[] bytes = new byte[inputBuf.remaining()];
            inputBuf = inputBuf.get(bytes, 0, inputBuf.remaining());
            handler.getInputQ().equeue(bytes);
        }

        inputBuf.compact();

        enableRead();
    }

    private void drainOutput() throws IOException {

        Object msg = this.handler.getOutputQ().dequeue();
        // inputBuf.clear();

        boolean writePending = false;
        if (msg != null) {
            writePending = true;
            do {
                sc.write(ByteBuffer.wrap(msg.toString().getBytes()));
                logger.printf(Level.INFO," write %s%n" , msg.toString());
//                byte[] bytes = msg.toString().getBytes();
//                int nw = 0, remain = bytes.length;
//
//                while (nw < remain && inputBuf.hasRemaining()) {
//                    int len = Math.min(remain, inputBuf.remaining());
//                    inputBuf.put(bytes, nw, len);
//                    nw += len;
//                }
//
//                inputBuf.flip();
//                logger.info("res" + new String(inputBuf.array()));
//                while (inputBuf.hasRemaining()) {
//                    sc.write(inputBuf);
//                }
            } while ((msg = this.handler.getOutputQ().dequeue()) != null);
        }

        if (this.handler.getOutputQ().isEmpty() && !writePending) {
            this.disableWrite();
        } else {
            this.enableWrite();
        }
    }

    public void restoreOps() {
        this.key.interestOps(this.intrestingOps);
    }

    private void enableRead() {
        intrestingOps |= SelectionKey.OP_READ;
    }

    private void disableRead() {
        intrestingOps &= ~SelectionKey.OP_READ;
    }

    private boolean isReadDisabled() {
        return (intrestingOps & SelectionKey.OP_READ) == 0;
    }

    private void enableWrite() {
        intrestingOps |= SelectionKey.OP_WRITE;
    }

    private void disableWrite() {
        intrestingOps &= ~SelectionKey.OP_WRITE;
    }

    private boolean isWriteDisabled() {
        return (intrestingOps & SelectionKey.OP_WRITE) == 0;
    }

    private class StringBuilderOutputQueue implements IOutputQueue<String> {
        private StringBuilder builder = new StringBuilder();

        @Override
        public void enqueue(byte[] bytes) {
            logger.printf(Level.INFO, "enqueue %s and turn on write%n", new String(bytes));
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
