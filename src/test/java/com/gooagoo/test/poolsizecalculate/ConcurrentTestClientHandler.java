package com.gooagoo.test.poolsizecalculate;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class ConcurrentTestClientHandler extends IoHandlerAdapter
{

    private static final AtomicInteger ai = new AtomicInteger();

    @Override
    public void messageSent(IoSession ioSession, Object message) throws Exception
    {
        System.out.println("【客户端发送信息】:" + message);
    }

    @Override
    public void messageReceived(IoSession session, Object message)
    {
        int i = ai.addAndGet(1);
        if (i % 10000 == 0)
        {
            System.out.println("received num=" + i);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) message;
        System.out.println("【客户端接收信息】:" + map);

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
    {
        System.out.println("【客户端出现异常】:" + cause);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception
    {
        System.out.println("【客户端连接关闭】");
    }
}
