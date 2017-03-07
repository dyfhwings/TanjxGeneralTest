package com.gooagoo.transport.test.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class Server
{

    private static final int PORT = 8999;

    public static void main(String[] args) throws IOException
    {

        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        acceptor.setHandler(new ServerHandler());

        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);

        acceptor.bind(new InetSocketAddress(PORT));

        CloseThread ct = new CloseThread(acceptor);
        ct.start();
    }

    private static class CloseThread extends Thread
    {
        private IoAcceptor acceptor;

        public CloseThread(IoAcceptor acceptor)
        {
            this.acceptor = acceptor;
        }

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(10000L);
            }
            catch (InterruptedException e)
            {
            }
            this.acceptor.unbind();
            this.acceptor.dispose();
        }
    }
}
