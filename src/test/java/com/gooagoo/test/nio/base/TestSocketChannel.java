package com.gooagoo.test.nio.base;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;
import com.gooagoo.test.BuildData;
import com.gooagoo.test.ConstantsTest;

public class TestSocketChannel
{
    private static Charset charset = Charset.forName(ConstantsTest.CHARSET);

    public static void main(String[] args) throws Exception
    {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress(9999));
        sc.configureBlocking(false);

        boolean bSend = false;
        while (true)
        {
            if (!sc.isConnected())
            {
                continue;
            }
            else if (!bSend)
            {
                send(sc);
                bSend = true;
                //Thread.sleep(80L);
            }
        }
    }

    private static void send(SocketChannel sc) throws IOException
    {
        ByteBuffer buf = encode(BuildData.buildMsg());
        sc.write(buf);
    }

    private static ByteBuffer encode(Object obj)
    {
        String json = JSON.toJSONString(obj);

        ByteBuffer body = charset.encode(json);
        ByteBuffer header = ByteBuffer.allocate(5);
        header.put(ConstantsTest.MSG_TYPE_1);
        header.putInt(body.limit());
        header.flip();

        ByteBuffer buf = ByteBuffer.allocate(body.limit() + header.limit());
        buf.put(header);
        buf.put(body);
        buf.flip();
        return buf;
    }
}
