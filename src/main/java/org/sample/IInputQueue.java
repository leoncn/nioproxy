package org.sample;

import java.nio.channels.SelectableChannel;

/**
 * Created by U0128754 on 12/14/2015.
 */
public interface IInputQueue<T> {

    public void equeue(byte[] bytes);

    public T nextMessage() ;
}
