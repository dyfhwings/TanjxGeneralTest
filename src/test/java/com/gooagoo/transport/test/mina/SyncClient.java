package com.gooagoo.transport.test.mina;

import java.net.InetSocketAddress;
import java.util.Scanner;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class SyncClient
{
    private static final int PORT = 8999;

    public static void main(String[] args)
    {
        NioSocketConnector connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(30000L);
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        SocketSessionConfig cfg = connector.getSessionConfig();
        cfg.setUseReadOperation(true);
        ConnectFuture connectFuture = connector.connect(new InetSocketAddress("127.0.0.1", PORT));
        //等待建立连接
        connectFuture.awaitUninterruptibly();
        System.out.println("连接成功");
        IoSession session = connectFuture.getSession();
        //现在已实现了连接，接下来就是发送-接收-断开了

        System.out.println("请输入发送的信息，以回车键结束：");
        Scanner sc = new Scanner(System.in);

        //发送
        String str = sc.next();
        WriteFuture writeF = session.write(str);
        writeF.awaitUninterruptibly();
        if (writeF.getException() != null)
        {
            System.out.println("client发送信息发生异常. cause by=" + writeF.getException().getMessage());
        }
        else if (writeF.isWritten())
        {
            boolean bFinish = false;
            // 接收
            do
            {
                ReadFuture readFuture = session.read();
                readFuture.awaitUninterruptibly();
                if (readFuture.getException() != null)
                {
                    // TODO 异常处理
                    System.out.println("client接收信息发生异常. cause by=" + readFuture.getException().getMessage());
                }
                else
                {
                    Object message = readFuture.getMessage();
                    if (message == null)
                    {
                        System.out.println("client同步接收信息为空");
                        continue;
                    }
                    if (message instanceof String && message.toString().equals("finish"))
                    {
                        bFinish = true;
                    }
                    System.out.println("client同步接收信息=" + readFuture.getMessage());
                }
            }
            while (!bFinish);

        }
        else
        {
            System.out.println("error!");
        }

        //关闭
        if (session != null)
        {
            if (session.isConnected())
            {
                session.close(true).awaitUninterruptibly();
            }
            connector.dispose(true);
        }
        System.out.println("--------------------------end");

    }
}
