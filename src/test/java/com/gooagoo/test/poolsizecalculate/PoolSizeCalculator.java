package com.gooagoo.test.poolsizecalculate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * 根据我的理解，该工具类就是给定一个测试时长，然后在单个线程内重复执行任务，达到测试时长后中止（误差20毫秒），
 * 最后计算在整个测试时长内CPU空闲时间和CPU占用时间之比（CPU占用时间越长，则合理开启的线程数越少），再乘以CPU数量，由此可以得到CPU数量下合理的线程池大小。 by tanjixiang
 * 
 * A class that calculates the optimal thread pool boundaries. It takes the
 * desired target utilization and the desired work queue memory consumption as
 * input and retuns thread count and work queue capacity.
 * 
 * @author Niklas Schlimm
 * 
 */
public abstract class PoolSizeCalculator
{

    /**
     * The sample queue size to calculate the size of a single {@link Runnable}
     * element.
     */
    private final int SAMPLE_QUEUE_SIZE = 1000;

    /**
     * Accuracy of test run. It must finish within 20ms of the testTime
     * otherwise we retry the test. This could be configurable.
     * 测试运行精度。运行误差需要在20毫秒内，否则我们将重新执行测试。
     */
    private final int EPSYLON = 20;

    /**
     * Control variable for the CPU time investigation.
     */
    private volatile boolean expired;

    /**
     * Time (millis) of the test run in the CPU time calculation.
     */
    private final long testtime = 3000;

    /**
     * Calculates the boundaries of a thread pool for a given {@link Runnable}.
     * 
     * @param targetUtilization
     *            the desired utilization of the CPUs (0 <= targetUtilization <=
     *            1)
     * @param targetQueueSizeBytes
     *            the desired maximum work queue size of the thread pool (bytes)
     */
    protected void calculateBoundaries(BigDecimal targetUtilization,
            BigDecimal targetQueueSizeBytes)
    {
        this.calculateOptimalCapacity(targetQueueSizeBytes);
        Runnable task = this.creatTask();
        this.start(task);
        this.start(task); // warm up phase
        long cputime = this.getCurrentThreadCPUTime();
        this.start(task); // test intervall
        //CPU占用时间
        cputime = this.getCurrentThreadCPUTime() - cputime;
        //CPU等待时间
        long waittime = (this.testtime * 1000000) - cputime;
        this.calculateOptimalThreadCount(cputime, waittime, targetUtilization);
    }

    private void calculateOptimalCapacity(BigDecimal targetQueueSizeBytes)
    {
        long mem = this.calculateMemoryUsage();
        BigDecimal queueCapacity = targetQueueSizeBytes.divide(new BigDecimal(
                mem), RoundingMode.HALF_UP);
        System.out.println("Target queue memory usage (bytes): "
                + targetQueueSizeBytes);
        System.out.println("createTask() produced "
                + this.creatTask().getClass().getName() + " which took " + mem
                + " bytes in a queue");
        System.out.println("Formula: " + targetQueueSizeBytes + " / " + mem);
        System.out.println("* Recommended queue capacity (bytes): "
                + queueCapacity);
    }

    /**
     * Brian Goetz' optimal thread count formula, see 'Java Concurrency in
     * Practice' (chapter 8.2)
     * 
     * @param cpu
     *            cpu time consumed by considered task
     * @param wait
     *            wait time of considered task
     * @param targetUtilization
     *            target utilization of the system
     */
    private void calculateOptimalThreadCount(long cpu, long wait,
            BigDecimal targetUtilization)
    {
        BigDecimal waitTime = new BigDecimal(wait);
        BigDecimal computeTime = new BigDecimal(cpu);
        BigDecimal numberOfCPU = new BigDecimal(Runtime.getRuntime()
                .availableProcessors());
        BigDecimal optimalthreadcount = numberOfCPU.multiply(targetUtilization)
                .multiply(
                        new BigDecimal(1).add(waitTime.divide(computeTime,
                                RoundingMode.HALF_UP)));
        System.out.println("Number of CPU: " + numberOfCPU);
        System.out.println("Target utilization: " + targetUtilization);
        System.out.println("Elapsed time (nanos): " + (this.testtime * 1000000));
        System.out.println("Compute time (nanos): " + cpu);
        System.out.println("Wait time (nanos): " + wait);
        System.out.println("Formula: " + numberOfCPU + " * "
                + targetUtilization + " * (1 + " + waitTime + " / "
                + computeTime + ")");
        System.out.println("* Optimal thread count: " + optimalthreadcount);
    }

    /**
     * Runs the {@link Runnable} over a period defined in {@link #testtime}.
     * Based on Heinz Kabbutz' ideas
     * (http://www.javaspecialists.eu/archive/Issue124.html).
     * 
     * @param task
     *            the runnable under investigation
     */
    public void start(Runnable task)
    {
        long start = 0;
        int runs = 0;
        do
        {
            if (++runs > 5)
            {
                throw new IllegalStateException("Test not accurate");
            }
            this.expired = false;
            start = System.currentTimeMillis();
            Timer timer = new Timer();
            //延迟testtime时长后执行，中断下面的线程执行循环
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    PoolSizeCalculator.this.expired = true;
                }
            }, this.testtime);
            while (!this.expired)
            {
                //循环运行线程方法，直到循环expired为true
                task.run();
            }
            start = System.currentTimeMillis() - start;
            timer.cancel();
        }
        while (Math.abs(start - this.testtime) > this.EPSYLON);//运行误差需要在20毫秒内，否则将重新执行测试
        this.collectGarbage(3);
    }

    private void collectGarbage(int times)
    {
        for (int i = 0; i < times; i++)
        {
            System.gc();
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Calculates the memory usage of a single element in a work queue. Based on
     * Heinz Kabbutz' ideas
     * (http://www.javaspecialists.eu/archive/Issue029.html).
     * 
     * @return memory usage of a single {@link Runnable} element in the thread
     *         pools work queue
     */
    public long calculateMemoryUsage()
    {
        BlockingQueue<Runnable> queue = this.createWorkQueue();
        for (int i = 0; i < this.SAMPLE_QUEUE_SIZE; i++)
        {
            queue.add(this.creatTask());
        }
        long mem0 = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();
        long mem1 = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();
        queue = null;
        this.collectGarbage(15);
        mem0 = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();
        queue = this.createWorkQueue();
        for (int i = 0; i < this.SAMPLE_QUEUE_SIZE; i++)
        {
            queue.add(this.creatTask());
        }
        this.collectGarbage(15);
        mem1 = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();
        return (mem1 - mem0) / this.SAMPLE_QUEUE_SIZE;
    }

    /**
     * Create your runnable task here.
     * 
     * @return an instance of your runnable task under investigation
     */
    protected abstract Runnable creatTask();

    /**
     * Return an instance of the queue used in the thread pool.
     * 
     * @return queue instance
     */
    protected abstract BlockingQueue<Runnable> createWorkQueue();

    /**
     * Calculate current cpu time. Various frameworks may be used here,
     * depending on the operating system in use. (e.g.
     * http://www.hyperic.com/products/sigar). The more accurate the CPU time
     * measurement, the more accurate the results for thread count boundaries.
     * 
     * @return current cpu time of current thread
     */
    protected abstract long getCurrentThreadCPUTime();

}