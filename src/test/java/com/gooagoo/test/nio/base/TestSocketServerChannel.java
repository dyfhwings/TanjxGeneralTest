package com.gooagoo.test.nio.base;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import com.gooagoo.test.ConstantsTest;

public class TestSocketServerChannel
{
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

    public static void readChannel(SocketChannel sc) throws IOException, InterruptedException
    {
        boolean hadHeader = false;
        int msgsize = 0;
        ByteBuffer recvBuff = null;
        while (true)
        {
            ByteBuffer buff = ByteBuffer.allocate(1024);
            int bytesRead = sc.read(buff);
            if (bytesRead > 0)
            {
                buff.flip();
                ByteBuffer newRecvBuf = buildRecvBuffer(recvBuff, buff);

                if (!hadHeader && newRecvBuf.limit() >= 5)
                {
                    byte msgtype = newRecvBuf.get();
                    if (msgtype != ConstantsTest.MSG_TYPE_1)
                    {
                        System.out.println("未知的类型，丢弃。type=" + msgtype);
                        recvBuff = null;
                        continue;
                    }
                    msgsize = newRecvBuf.getInt();
                    if (msgsize <= 0 || msgsize > ConstantsTest.MINA_MAX_PACKET_SIZE)
                    {
                        System.out.println("消息长度不合法，丢弃。size=" + msgsize);
                        recvBuff = null;
                        continue;
                    }
                    newRecvBuf.compact();
                    newRecvBuf.flip();
                }

                if (msgsize > newRecvBuf.limit())
                {
                    //消息不完整,继续接收
                    recvBuff = newRecvBuf;
                    continue;
                }
                else if (msgsize == newRecvBuf.limit())
                {
                    String msg = decode(newRecvBuf, msgsize);
                    System.out.println("消息接收完毕A=" + msg);
                    recvBuff = null;
                    hadHeader = false;
                    continue;
                }
                else
                {
                    //消息超出长度，需要截取
                    Map<String, Object> map = Split(newRecvBuf, msgsize);
                    System.out.println("消息接收完毕B=" + map.get("msg"));
                    recvBuff = (ByteBuffer) map.get("newmsg");
                    hadHeader = false;
                    continue;
                }
            }
            else if (bytesRead < 0)
            {
                sc.close();
                break;
            }
        }
    }

    private static ByteBuffer buildRecvBuffer(ByteBuffer recvBuff, ByteBuffer buff)
    {
        ByteBuffer newBuf = null;
        if (recvBuff != null)
        {
            newBuf = ByteBuffer.allocate(recvBuff.limit() + buff.limit());
            newBuf.put(recvBuff);
        }
        else
        {
            newBuf = ByteBuffer.allocate(buff.limit());
        }
        newBuf.put(buff);
        newBuf.flip();
        return newBuf;
    }

    private static String decode(ByteBuffer msgBuf, int size) throws UnsupportedEncodingException
    {
        byte[] bytes = new byte[size];
        msgBuf.get(bytes);
        return new String(bytes, ConstantsTest.CHARSET);
    }

    private static Map<String, Object> Split(ByteBuffer msgBuf, int size) throws UnsupportedEncodingException
    {
        Map<String, Object> map = new HashMap<String, Object>();
        //消息超出长度，需要截取
        map.put("msg", decode(msgBuf, size));
        msgBuf.compact();
        msgBuf.flip();
        map.put("newmsg", msgBuf);
        return map;
    }
}
