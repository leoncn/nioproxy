package org.sample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by U0128754 on 12/15/2015.
 */
public class Acceptor {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel svr = ServerSocketChannel.open();
        svr.bind(new InetSocketAddress(1234));

        Reactor reactor = new Reactor();
        reactor.start();

        System.out.println("reactor is running.");
        SocketChannel sc = null;
        while( ( sc = svr.accept() ) != null) {
            reactor.registerChannel(sc, new EchoHandler());
        }
    }
}
