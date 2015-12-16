package org.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by U0128754 on 12/14/2015.
 */
public class EchoHandler implements IHandler {

    private Logger logger = LogManager.getLogger();

    private IOutputQueue<String> outputQ = null;
    private IInputQueue<String> inputQ = null;

    @Override
    public void handle() {
        Object req = null;
        while ((req = this.getInputQ().nextMessage()) != null) {
            req = req + "\n";
            this.getOutputQ().enqueue(req.toString().toUpperCase().getBytes());
        }
    }

    @Override
    public IOutputQueue getOutputQ() {
        return outputQ;
    }

    @Override
    public void setOutputQ(IOutputQueue queue) {
        this.outputQ = queue;

    }

    @Override
    public IInputQueue getInputQ() {
        return inputQ;
    }

    @Override
    public void setInputQ(IInputQueue queue) {
        this.inputQ = queue;
    }
}
