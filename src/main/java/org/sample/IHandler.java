package org.sample;

/**
 * Created by U0128754 on 12/14/2015.
 */
public interface IHandler {

    public void handle();

    public ByteBufferQueue getOutputQ();

    public ByteBufferQueue getInputQ();

}
