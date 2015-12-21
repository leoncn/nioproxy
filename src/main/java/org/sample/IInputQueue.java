package org.sample;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

/**
 * Created by U0128754 on 12/14/2015.
 */
public interface IInputQueue<T> {

    public void equeue(ByteBuffer buffer);

    public T dequeue() ;

    public boolean isEmpty();
}
