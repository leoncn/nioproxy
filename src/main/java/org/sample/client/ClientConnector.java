package org.sample.client;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by U0128754 on 12/16/2015.
 */
public class ClientConnector  {

    public static void main(String[] args) {
        NioSocketConnector connector = new NioSocketConnector();
        connector.setConnectTimeoutCheckInterval( 3 * 1000L);

        connector.setHandler(new IoHandler() {
            @Override
            public void sessionCreated(IoSession ioSession) throws Exception {

            }

            @Override
            public void sessionOpened(IoSession ioSession) throws Exception {

            }

            @Override
            public void sessionClosed(IoSession ioSession) throws Exception {

            }

            @Override
            public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {

            }

            @Override
            public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {

            }

            @Override
            public void messageReceived(IoSession ioSession, Object o) throws Exception {

            }

            @Override
            public void messageSent(IoSession ioSession, Object o) throws Exception {

            }

            @Override
            public void inputClosed(IoSession ioSession) throws Exception {

            }
        });
        SocketAddress remoteAddr = new InetSocketAddress("localhost", 1234);
        connector.connect(remoteAddr);
        connector.connect(remoteAddr);

    }
}
