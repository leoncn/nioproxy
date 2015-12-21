package org.sample.client.echo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sample.*;


/**
 * Created by U0128754 on 12/14/2015.
 */
public class EchoHandler implements IHandler {

    private Logger logger = LogManager.getLogger();

    private ByteBufferQueue outputQ = new ByteBufferQueue();
    private ByteBufferQueue inputQ = new ByteBufferQueue();
    private TextLineDecoder decoder = new TextLineDecoder();
    private TextLineEncoder encoder = new TextLineEncoder();

    @Override
    public void handle() {
        String inMsg = null;

        while( ( inMsg = decoder.decode(inputQ)) != null) {

            this.getOutputQ().equeue(encoder.encode(inMsg.toUpperCase()));
        }
    }

    @Override
    public ByteBufferQueue getOutputQ() {
        return outputQ;
    }

    @Override
    public ByteBufferQueue getInputQ() {
        return inputQ;
    }
}
