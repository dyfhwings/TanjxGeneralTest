package com.gooagoo.test.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class TestSocketServerChannel
{
    private static Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception
    {
        System.out.println("测试服务端启动");
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9999));
        serverSocketChannel.configureBlocking(false);

        while (true)
        {
            SocketChannel sc = serverSocketChannel.accept();
            if (sc == null)
            {
                //System.out.println("wait for connection ……");
                // no connections, snooze a while ...
                Thread.sleep(1000);
            }
            else
            {
                System.out.println("Incoming connection from " + sc.socket().getRemoteSocketAddress());
                sc.configureBlocking(false);
                readChannel(sc);

                sc.close();
            }
        }

    }

    public static void readChannel(SocketChannel sc) throws IOException
    {
        int bytesRead = 0;
        int msgsize = 0;
        ByteBuffer oldBuf = null;
        while (true)
        {
            ByteBuffer buff = ByteBuffer.allocate(16);
            bytesRead = sc.read(buff);
            if (bytesRead > 0)
            {
                buff.flip();
                ByteBuffer newBuf;
                if (oldBuf != null)
                {
                    newBuf = ByteBuffer.allocate(oldBuf.limit() + buff.limit());
                    newBuf.put(oldBuf);
                }
                else
                {
                    newBuf = ByteBuffer.allocate(buff.limit());
                }
                newBuf.put(buff);
                newBuf.flip();

                if (newBuf.limit() >= 4)
                {
                    msgsize = newBuf.getInt(0);
                }
                if (msgsize > newBuf.limit())
                {
                    //消息不完整,继续接收
                    oldBuf = newBuf;
                    continue;
                }
                else if (msgsize == (newBuf.limit() - 4))
                {
                    System.out.println("msgsize=" + msgsize);
                    String msg = decode(newBuf, msgsize);
                    System.out.println("消息接收完毕A=" + msg);
                    oldBuf = null;
                    continue;
                }
                else
                {
                    System.out.println("msgsize=" + msgsize);
                    //消息超出长度，需要截取
                    Map<String, Object> map = Split(newBuf, msgsize);
                    System.out.println("消息接收完毕B=" + map.get("msg"));
                    oldBuf = (ByteBuffer) map.get("newmsg");
                    continue;
                }
            }
            else if (bytesRead == 0)
            {
                continue;
            }
            else
            {
                sc.close();
                break;
            }
        }
    }

    private static String decode(ByteBuffer msgBuf, int size)
    {
        byte[] bytes = new byte[size];
        msgBuf.position(4);
        msgBuf.get(bytes);
        return new String(bytes);
    }

    private static Map<String, Object> Split(ByteBuffer msgBuf, int size)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        //消息超出长度，需要截取
        map.put("msg", decode(msgBuf, size));
        byte[] bytes = new byte[msgBuf.limit() - size];
        msgBuf.get(bytes);
        ByteBuffer newmsgBuf = ByteBuffer.allocate(msgBuf.limit() - size);
        newmsgBuf.put(bytes);
        newmsgBuf.flip();
        map.put("newmsg", newmsgBuf);
        return map;
    }
}
