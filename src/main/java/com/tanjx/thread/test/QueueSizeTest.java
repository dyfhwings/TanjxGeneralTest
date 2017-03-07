package com.tanjx.thread.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueSizeTest
{
    public static void main(String[] args) throws InterruptedException
    {
        while (true)
        {
            try
            {
                getexecutor().submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Thread.sleep(2000);
                        }

                        catch (Throwable e)
                        {
                        }
                    }
                });
            }
            catch (RejectedExecutionException e)
            {
                String info = "Too many requests and AsyncProcessorThread pool busy,processRequestAsync RejectedExecutionException.cause by=" + e.getMessage();
                System.out.println(info);
                break;
            }
        }
        Thread.sleep(20);

    }

    private static ExecutorService getexecutor()
    {
        //异步请求
        ExecutorService asyncProcessorExecutor = Executors.newFixedThreadPool(23, new ThreadFactory()
        {
            private AtomicInteger n = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r)
            {
                return new Thread(r, "MinaTransportServerAsyncProcessorThread" + this.n.decrementAndGet());
            }
        });
        return asyncProcessorExecutor;
    }

}
