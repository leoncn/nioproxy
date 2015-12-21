package org.sample;

import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 * Created by U0128754 on 12/21/2015.
 */
public class TextLineEncoder implements ByteBufferQueueEncoder<String> {
    @Override
    public ByteBuffer encode(String s) {
        if(s == null) return null;

        if(!s.endsWith("\n"))
            s += "\n";

        return ByteBuffer.wrap(s.getBytes());
    }
}
