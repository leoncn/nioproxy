package org.sample;

/**
 * Created by U0128754 on 12/21/2015.
 */
public interface ByteBufferQueueDecoder<T> {

    public T decode(ByteBufferQueue queue);
}
