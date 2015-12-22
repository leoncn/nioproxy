package org.sample;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
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
    private InetSocketAddress remoteAddr = null;

    private int reqCnt = 0;
    private int resCnt = 0;

    private int intrestingOps = 0, readyOps = 0;
    private ByteBuffer inputBuf = ByteBuffer.allocate(1024);

    public ChannelWrapper(SelectionKey key, SocketChannel sc, IHandler handler) {
        this.key = key;
        this.sc = sc;
        this.handler = handler;
        try {
            this.remoteAddr = (InetSocketAddress) sc.getRemoteAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InetSocketAddress getRemoteAddr() {
        return remoteAddr;
    }

    public SelectionKey getKey() {
        return this.key;
    }

    public void prepare() {
        this.intrestingOps = this.key.interestOps();
        this.readyOps = this.key.readyOps();

        this.key.interestOps(0); //disable all ops
    }

    public void process() throws IOException {
        try {
            this.fillInput();
            handler.handle();
            this.drainOutput();
        } catch (Throwable e) {
            this.offWriteOps();
            this.offReadOps();
            throw e;
        }
    }

    public boolean isDone() {
        boolean done = this.isWriteOpsOff() && this.isReadOpsOff();
        logger.printf(Level.DEBUG, "%s total req %d , res %d | read:%s write:%s.",
                this.key.hashCode(), this.reqCnt, this.resCnt, !this.isReadOpsOff(), !this.isWriteOpsOff());

        return done;
    }

    private void fillInput() throws IOException {
        int nr = sc.read(inputBuf);

        if (nr == -1) {
            logger.info("remote peer closed input stream.");
            this.offReadOps();
            this.sc.shutdownInput();
            return;
        }

        do {
            inputBuf.flip();

            int numEqueue = handler.getInputQ().equeue(inputBuf);
            if (inputBuf.hasRemaining() && numEqueue == 0) {
                logger.info("input queue is full.");
            }

            inputBuf.compact();

        } while ((nr = sc.read(inputBuf)) > 0);

        onReadOps();
    }

    private void drainOutput() throws IOException {

        ByteBuffer output = this.handler.getOutputQ().dequeue();
        boolean writePending = output != null;

        if (writePending) {
            //     logger.info("write out message.");
            do {
                while (output.hasRemaining()) {
                    sc.write(output);
                }
            } while ((output = this.handler.getOutputQ().dequeue()) != null);
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
}
