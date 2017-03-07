package com.gooagoo.transport.test.mina;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class Client
{
    private static final int PORT = 8999;

    public static void main(String[] args) throws Exception
    {
        IoConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        connector.setHandler(new ClientHandler());

        ConnectFuture connectFuture = connector.connect(new InetSocketAddress("127.0.0.1", PORT));
        //等待建立连接
        connectFuture.awaitUninterruptibly();
        System.out.println("连接成功");

        IoSession session = connectFuture.getSession();

        sendFile(session);

        System.out.println("client send finished and wait success");

        Thread.sleep(15000L);
        sendString(session);

        //关闭
        /*        if (session != null)
                {
                    if (session.isConnected())
                    {
                        session.getCloseFuture().awaitUninterruptibly();
                    }
                    connector.dispose(true);
                }*/

    }

    private static void sendFile(IoSession session) throws IOException
    {
        String fileName = "d://test.zip";
        FileInputStream fis = new FileInputStream(new File(fileName));
        byte[] buf = new byte[1024];
        FileUploadRequest request = new FileUploadRequest();
        request.setFilename("test.zip");
        request.setHostname("localhost");
        while (fis.read(buf, 0, buf.length) != -1)
        {
            request.setFileContent(buf);
            //像session中写入信息供服务端获得
            session.write(request);
        }
        fis.close();
        //发送完成的标志
        session.write(new String("finish"));
    }

    private static void sendString(IoSession session) throws IOException
    {
        if (session.isConnected())
        {
            System.out.println("请输入发送的信息，以回车键结束：");
            Scanner sc = new Scanner(System.in);
            String str = sc.next();
            WriteFuture writeF = session.write(str.getBytes());
            writeF.awaitUninterruptibly();
            //发送完成的标志
            session.write(new String("finish"));
        }
        else
        {
            System.out.println("连接已关闭");
        }
    }
}
