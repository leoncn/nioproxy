package org.sample;

import junit.framework.TestCase;
import org.junit.Assert;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by U0128754 on 12/21/2015.
 */
public class ByteBufferQueueTest extends TestCase {


    @org.junit.Test
    public void testEqueue() throws Exception {
        String line = "This a line for " + ByteBufferQueue.class.getCanonicalName() + " unit testing.";
        ByteBufferQueue queue = new ByteBufferQueue(16);
        int n = queue.equeue(ByteBuffer.wrap(line.getBytes()));
        Assert.assertEquals(n, line.length());
    }

    @org.junit.Test
    public void testDequeue() throws Exception {
        String line = "This a line for " + ByteBufferQueue.class.getCanonicalName() + " unit testing.";
        ByteBufferQueue queue = new ByteBufferQueue(16);
        int n = queue.equeue(ByteBuffer.wrap(line.getBytes()));

        String result = new String(queue.dequeue(line.length()).array());

        Assert.assertEquals(line, result);

    }

    @org.junit.Test
    public void testDequeue1024() throws Exception {
        String line = "This a line for " + ByteBufferQueue.class.getCanonicalName() + " unit testing.";
        ByteBufferQueue queue = new ByteBufferQueue();
        int n = queue.equeue(ByteBuffer.wrap(line.getBytes()));

        ByteBuffer retBuf = queue.dequeue(line.length() * 10);

        Assert.assertEquals("Buffer should have the same num of byte as its origin input byte array.", line.length(), retBuf.remaining());

        String result = new String(retBuf.array());

        Assert.assertEquals("Buffer should have the same content as its origin input byte array.", line, result);
    }

    @org.junit.Test
    public void testIndexOf() throws Exception {
        byte b2Find = '\n';
        int N = 1024, NUM_LINES = 11;
        Random ran = new Random(N);
        char[] kcs = new char[ran.nextInt(N)];

        Arrays.fill(kcs, '-');
        kcs[kcs.length - 1] = (char) b2Find;

        String line = String.valueOf(kcs);

        ByteBufferQueue bigQ = new ByteBufferQueue(N * 10);
        ByteBufferQueue smallQ = new ByteBufferQueue(8);

        Stream.of(bigQ, smallQ).forEach(
                q -> {
                    IntStream.range(0, NUM_LINES).forEach((i) -> {

                        byte[] bytes = line.getBytes();
                        int n = q.equeue(ByteBuffer.wrap(bytes));

                        Assert.assertEquals(bytes.length, n);
                    });

                    int p = -1, c = 0;
                    while ((p = q.indexOf(b2Find)) > -1) {
                        q.dequeue(p + 1);
                        c++;
                    }

                    Assert.assertEquals(NUM_LINES, c);
                }
        );
    }

    @org.junit.Test
    public void testIsEmpty() throws Exception {
        ByteBufferQueue queue = new ByteBufferQueue();
        Assert.assertTrue("Q should be empty", queue.isEmpty());
        queue.equeue(ByteBuffer.allocate(0));

        Assert.assertTrue("Q should be empty with a empty buffer", queue.isEmpty());

        byte[] bytes = "abc".getBytes();
        queue.equeue(ByteBuffer.wrap(bytes));

        Assert.assertFalse("Q should be have " + bytes.length + " bytes", queue.isEmpty());

    }
}