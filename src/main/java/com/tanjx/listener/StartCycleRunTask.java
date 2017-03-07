package com.tanjx.listener;

//代码清单StartCycleRunTask：容器监听器
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class StartCycleRunTask implements ServletContextListener
{
    private Timer timer;

    @Override
    public void contextDestroyed(ServletContextEvent arg0)
    {
        // ②该方法在Web容器关闭时执行
        System.out.println("Web应用程序启动关闭...");
        this.timer.cancel();
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0)
    {
        //②在Web容器启动时自动执行该方法
        System.out.println("Web应用程序启动...");
        this.timer = new Timer("Timer_Daemon_Test");//②-1:创建一个Timer，Timer内部自动创建一个背景线程
        TimerTask task = new SimpleTimerTask();
        this.timer.schedule(task, 1000L, 5000L); //②-2:注册一个5秒钟运行一次的任务
    }

    public static class SimpleTimerTask extends TimerTask
    {//③任务
        private int count;

        @Override
        public void run()
        {
            System.out.println((++this.count) + "execute task..." + (new Date()));
        }
    }
}
