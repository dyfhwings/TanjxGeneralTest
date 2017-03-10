package com.tanjx.concurrent.future;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.tanjx.utils.UniqueThreadIdGenerator;

public class FutureTest
{
    public static void main(String[] args) throws Exception
    {
        testFuture();
        System.out.println("-----------------------");
        testFutureTask();
        System.out.println("-----------------------");
        testMyFutureTask();
    }

    private static void testFuture() throws InterruptedException, ExecutionException
    {
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "Future call";
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(callable);
        String result = future.get();
        System.out.println("result=" + result);
    }

    private static void testFutureTask() throws InterruptedException, ExecutionException
    {
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "FutureTask call";
            }
        };
        FutureTask<String> ft = new FutureTask<String>(callable);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(ft);
        String result = ft.get();
        System.out.println("result=" + result);
    }

    private static void testMyFutureTask() throws InterruptedException, ExecutionException
    {
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                System.out.println("Callable ThreadID=" + UniqueThreadIdGenerator.getCurrentThreadId());
                return "MyFutureTask call";
            }
        };
        System.out.println("主线程ID=" + UniqueThreadIdGenerator.getCurrentThreadId());
        MyFutureTask<String> ft = new MyFutureTask<String>(callable);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(ft);
        String result = ft.get();
        System.out.println("result=" + result);
    }

    private static class MyFutureTask<V> extends FutureTask<V>
    {
        public MyFutureTask(Callable<V> callable)
        {
            super(callable);
        }

        public MyFutureTask(Runnable runnable, V result)
        {
            super(runnable, result);
        }

        @Override
        protected void done()
        {
            System.out.println("MyFutureTask ThreadID=" + UniqueThreadIdGenerator.getCurrentThreadId());
            System.out.println("MyFutureTask is done.");
        }
    }
}
