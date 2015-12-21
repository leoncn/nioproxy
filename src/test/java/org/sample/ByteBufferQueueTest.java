package org.sample;

import junit.framework.TestCase;
import org.junit.Assert;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Created by U0128754 on 12/21/2015.
 */
public class ByteBufferQueueTest extends TestCase {


    @org.junit.Test
    public void testEqueue() throws Exception {
        String line = "This a line for " + ByteBufferQueue.class.getCanonicalName() + " unit testing.";
        ByteBufferQueue queue = new  ByteBufferQueue(16);
        int n = queue.equeue(ByteBuffer.wrap(line.getBytes()));
        Assert.assertEquals(n, line.length());
    }

    @org.junit.Test
    public void testDequeue() throws Exception {
        String line = "This a line for " + ByteBufferQueue.class.getCanonicalName() + " unit testing.";
        ByteBufferQueue queue = new  ByteBufferQueue(16);
        int n = queue.equeue(ByteBuffer.wrap(line.getBytes()));

        String result  = new String(queue.dequeue(line.length()).array());

        Assert.assertEquals(line, result);

    }

    @org.junit.Test
    public void testDequeue1024() throws Exception {
        String line = "This a line for " + ByteBufferQueue.class.getCanonicalName() + " unit testing.";
        ByteBufferQueue queue = new  ByteBufferQueue();
        int n = queue.equeue(ByteBuffer.wrap(line.getBytes()));

        queue.dequeue(line.length() * 10).array();

        Assert.assertTrue(line.equals(result));
    }

    @org.junit.Test
    public void testIndexOf() throws Exception {
        String line = "This a line for " + ByteBufferQueue.class.getCanonicalName() + " unit testing.";
        ByteBufferQueue queue = new  ByteBufferQueue(16);
        int n = queue.equeue(ByteBuffer.wrap(line.getBytes()));

        char c = 'a';
        Assert.assertEquals(line.indexOf(c), queue.indexOf((byte)c));
    }

    @org.junit.Test
    public void testIsEmpty() throws Exception {
        ByteBufferQueue queue = new  ByteBufferQueue();
        Assert.assertTrue("Q should be empty", queue.isEmpty());
        queue.equeue(ByteBuffer.allocate(0));

        Assert.assertTrue("Q should be empty with a empty buffer", queue.isEmpty());

        byte[] bytes = "abc".getBytes();
        queue.equeue(ByteBuffer.wrap(bytes));

        Assert.assertFalse("Q should be have " + bytes.length + " bytes", queue.isEmpty());

    }
}