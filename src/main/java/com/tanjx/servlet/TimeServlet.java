package com.tanjx.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TimeServlet extends HttpServlet
{
    private MyThread thread;

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        this.thread = new MyThread();
        this.thread.setName("Thread_Test");
        this.thread.start();
    }

    public class MyThread extends Thread
    {
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    Thread.sleep(5000L);
                    System.out.println("输出-------------------------------");
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        throw new ServletException("Post mode is forbidden.");
    }

}
