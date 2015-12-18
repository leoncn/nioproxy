package org.sample;



import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sample.client.echo.EchoHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by U0128754 on 12/15/2015.
 */
public class Acceptor {

    private static Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel svr = ServerSocketChannel.open();
        svr.bind(new InetSocketAddress(1234));

        Reactor reactor = new Reactor();
        reactor.start();

        logger.printf(Level.INFO, "reactor is running.");
        SocketChannel sc = null;
        while( ( sc = svr.accept() ) != null) {
            reactor.registerChannel(sc, new EchoHandler());
        }
    }
}
