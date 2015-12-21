package org.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Created by U0128754 on 12/21/2015.
 */
public class ByteBufferQueue {
    private Logger logger = LogManager.getLogger();

    private final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private LinkedList<ByteBuffer> buffers = new LinkedList<>();
    private ByteBuffer active = EMPTY_BUFFER;
    private int qsize = 1024;

    public ByteBufferQueue() {
    }

    public ByteBufferQueue(int qsize) {
        this.qsize = qsize;
    }

    public int equeue(ByteBuffer buffer) {
        return appendToBuffers(buffer);
    }

    public ByteBuffer dequeue() {
        return this.dequeue(qsize);
    }

    public ByteBuffer dequeue(int length) {

        if (this.isEmpty()) {
            return null;
        }

        ByteBuffer retBuf = ByteBuffer.allocate(length);

        while (length > 0 && (active = this.getNextActive()) != EMPTY_BUFFER) {
            length -= this.bufferCopy(active, retBuf, length);
        }

        retBuf.flip();

        return retBuf;
    }

    public int indexOf(byte b) {
        int idx = -1;

        int from = active.position(), to = active.limit();
        int numOfByteBeforeFound = (idx = indexOf(active.array(), from, to, b)) > -1 ? idx : active.remaining();
        if (idx > -1)
            return numOfByteBeforeFound - from;

        for (ByteBuffer buf : this.buffers) {
            from = 0;
            to = buf.position();
            numOfByteBeforeFound += (idx = indexOf(buf.array(), from, to, b)) > -1 ? idx : buf.position();

            if (idx > -1)
                return numOfByteBeforeFound - from;
        }

        return -1;
    }

    private int indexOf(byte[] bytes, int from, int to, byte b) {
        int i;

        for (i = from; i < to && bytes[i] != b; i++)
            ;

        return i == to ? -1 : i;
    }


    public boolean isEmpty() {
        if(this.buffers.isEmpty()) {
            return !this.active.hasRemaining();
        }

        ByteBuffer temp = this.buffers.stream()
                .filter( buf -> buf.capacity() > 0).findAny().orElse(EMPTY_BUFFER);

       return !this.active.hasRemaining() && temp == EMPTY_BUFFER;

    }

    private ByteBuffer getNextActive() {
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
        int n = topUp(buf);

        while(buf.hasRemaining()) {
            ByteBuffer newBuf = this.allocBuffer();
            int ncopy = this.bufferCopy(buf, newBuf, buf.remaining());
            n += ncopy;
            this.buffers.add(newBuf);
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
        return buffers.isEmpty() ? allocBuffer() : buffers.removeLast();
    }

    private int topUp(ByteBuffer buf) {
        if(!buf.hasRemaining())
            return 0;

        ByteBuffer last = this.last();
        int ncopy = bufferCopy(buf, last, buf.remaining());
        this.buffers.addLast(last);
        return ncopy;
    }

    private ByteBuffer allocBuffer() {
        return ByteBuffer.allocate(qsize);
    }
}
