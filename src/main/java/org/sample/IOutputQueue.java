package org.sample;

/**
 * Created by U0128754 on 12/14/2015.
 */
public interface IOutputQueue<T> {

    public void enqueue(byte[] bytes);

    public T dequeue();

    public boolean isEmpty();
}
