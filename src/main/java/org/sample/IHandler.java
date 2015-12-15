package org.sample;

/**
 * Created by U0128754 on 12/14/2015.
 */
public interface IHandler {

    public void handle();

    public IOutputQueue getOutputQ();

    public IInputQueue getInputQ();

    public void setOutputQ(IOutputQueue queue);

    public void setInputQ(IInputQueue queue);

}
