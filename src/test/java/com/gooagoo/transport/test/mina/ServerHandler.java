package com.gooagoo.transport.test.mina;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Date;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class ServerHandler implements IoHandler
{
    private BufferedOutputStream out;

    private int size;

    private int count;

    private Date startTime;

    @Override
    public void exceptionCaught(IoSession arg0, Throwable arg1)
            throws Exception
    {
        arg1.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message)
    {
        System.out.println("server received");

        try
        {
            if (message instanceof FileUploadRequest)
            {
                //FileUploadRequest 为传递过程中使用的DO。
                FileUploadRequest request = (FileUploadRequest) message;
                System.out.println(request.getFilename());
                if (this.out == null)
                {
                    //新建一个文件输入对象BufferedOutputStream，随便定义新文件的位置
                    this.out = new BufferedOutputStream(new FileOutputStream(
                            "D:/logs/" + request.getFilename()));
                    this.out.write(request.getFileContent());
                    this.startTime = new Date();
                }
                else
                {
                    this.out.write(request.getFileContent());
                }
                this.count++;
                this.size += request.getFileContent().length;

            }
            else if (message instanceof String)
            {
                if (((String) message).equals("finish"))
                {
                    System.out.println("size=" + this.size);
                    System.out.println("count=" + this.count);
                    if (this.out != null)
                    {
                        int sec = TimeUtils.dateDiff(6, this.startTime, new Date());
                        System.out.println("second=" + sec);
                        //这里是进行文件传输后，要进行flush和close否则传递的文件不完整。
                        this.out.flush();
                        this.out.close();
                    }
                    //回执客户端信息，上传文件成功
                    session.write("success");
                    //类变量数量重置
                    this.out = null;
                    this.size = 0;
                    this.count = 0;
                }
                else
                {
                    System.out.println("回推消息");
                    for (int i = 0; i < 3; i++)
                    {
                        session.write("收到并返回消息=" + message + "-" + i);
                    }
                    session.write("finish");
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void messageSent(IoSession arg0, Object arg1) throws Exception
    {
        // TODO Auto-generated method stub
        System.out.println("发送信息:" + arg1.toString());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception
    {
        // TODO Auto-generated method stub
        System.out.println("IP:" + session.getRemoteAddress().toString() + "断开连接");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception
    {
        // TODO Auto-generated method stub
        System.out.println("IP:" + session.getRemoteAddress().toString());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception
    {
        // TODO Auto-generated method stub
        System.out.println("IDLE " + session.getIdleCount(status));

    }

    @Override
    public void sessionOpened(IoSession arg0) throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void inputClosed(IoSession arg0) throws Exception
    {
        // TODO Auto-generated method stub

    }

}
