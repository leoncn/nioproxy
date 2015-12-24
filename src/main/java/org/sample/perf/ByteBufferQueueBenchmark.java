package org.sample.perf;

import  org.openjdk.jmh.annotations.*;
import org.sample.ByteBufferQueue;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by U0128754 on 12/22/2015.
 */

@State(Scope.Thread)
public class ByteBufferQueueBenchmark {

    private ByteBufferQueue queue = null;
    private int BLOCK_SZ = 128;
    private int NUM_OF_LINES = 32;
    private byte[] block = new byte[BLOCK_SZ];

    {
        Arrays.fill(block, (byte)'-');
        block[BLOCK_SZ - 1] = '\n';
    }

    @Setup
    public void init() {
        this.queue = new ByteBufferQueue(512);
    }


    @Benchmark
    public int enqueue() {
       return IntStream.range(0, NUM_OF_LINES).mapToObj(i -> {
            return this.queue.equeue(ByteBuffer.wrap(block));
        }).mapToInt(i->i).sum();
    }


    @TearDown
    public  void destory (){
        queue.dequeue(BLOCK_SZ * NUM_OF_LINES);
        this.queue = null;
    }

}