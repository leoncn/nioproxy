package org.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by U0128754 on 12/21/2015.
 */
public class ByteBufferQueue {
    private final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private Logger logger = LogManager.getLogger();
    private LinkedList<ByteBuffer> buffers = new LinkedList<>();
    private ByteBuffer active = EMPTY_BUFFER;
    private int bufferSize = 1024;
    private int numOfByteBuffered = 0;

    public ByteBufferQueue() {
    }

    public ByteBufferQueue(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int equeue(ByteBuffer buffer) {
        int bufferedBytes = appendToBuffers(buffer);
        numOfByteBuffered += bufferedBytes;
        return bufferedBytes;
    }

    public ByteBuffer dequeue() {
        return this.dequeue(bufferSize);
    }

    public ByteBuffer dequeue(int length) {

        if (this.isEmpty()) {
            return null;
        }

        int bufSize = Math.min(numOfByteBuffered, length);
        ByteBuffer retBuf = ByteBuffer.allocate(bufSize);

        while (bufSize > 0 && (active = this.nextBufferToRead()) != EMPTY_BUFFER) {
            bufSize -= this.bufferCopy(active, retBuf, bufSize);
        }

        retBuf.flip();

        numOfByteBuffered-=retBuf.remaining();

        return retBuf;
    }

    public int indexOf(byte b) {
        int idx = -1;

        int numOfByteBeforeFound = (idx = indexOf(active.slice(), b)) > -1 ? idx : active.remaining();
        if (idx > -1)
            return numOfByteBeforeFound;

        for (ByteBuffer buf : this.buffers) {
        ByteBuffer dup = buf.duplicate(); dup.flip();

        numOfByteBeforeFound += (idx = indexOf(dup, b)) > -1 ? idx : dup.remaining();

            if (idx > -1)
                return numOfByteBeforeFound;
        }

        return -1;
    }

    private int indexOf(ByteBuffer buf,  byte b) {
        int i, len = buf.remaining();

        for (i = 0; i < len && buf.get(i) != b; i++)
            ;

        return i == len ? -1 : i;
    }


    public boolean isEmpty() {
        return numOfByteBuffered == 0;

    }

    private ByteBuffer nextBufferToRead() {
        if (this.isEmpty()) {
            return EMPTY_BUFFER;
        }

        if (active.hasRemaining()) {
            return active;
        }

        ByteBuffer temp = this.buffers.removeFirst();
        temp.flip();

        return temp;
    }

    private int appendToBuffers(ByteBuffer buf) {
        if(!buf.hasRemaining()) {
            return 0;
        }

        int n = 0;
        while(buf.hasRemaining()) {
            int ntopUp = topUpTolastBuffer(buf);

            if(ntopUp == 0) {
                this.buffers.add(this.allocBuffer());
                continue;
            }
            n += ntopUp;
        }
        return n;
    }

    private int bufferCopy(ByteBuffer src, ByteBuffer dest, int count) {
        int c = 0;
        while (src.hasRemaining() && dest.hasRemaining() && count-- > 0) {
            dest.put(src.get());
            c++;
        }

        return c;
    }

    private ByteBuffer last() {
        return buffers.isEmpty() ? null : buffers.removeLast();
    }

    private int topUpTolastBuffer(ByteBuffer buf) {
        ByteBuffer last = this.last();
        if(last == null) return 0;

        int ncopy = bufferCopy(buf, last, buf.remaining());
        this.buffers.addLast(last);
        return ncopy;
    }

    private ByteBuffer allocBuffer() {
        return ByteBuffer.allocate(bufferSize);
    }
}
