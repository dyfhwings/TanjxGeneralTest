package com.tanjx.calc.object.size;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延迟类，用于重发消息的处理
 * @author tanjx
 *
 */
public class DelayedItem<T> implements Delayed
{
    private final T item;
    private final long expireTime;

    /**
     * @param item 被延迟的类实例
     * @param timeout 类实例延迟处理的等待时间，毫秒。
     */
    public DelayedItem(T item, long delayTime)
    {
        this.item = item;
        this.expireTime = System.currentTimeMillis() + delayTime;
    }

    public T getItem()
    {
        return this.item;
    }

    @Override
    public int compareTo(Delayed o)
    {
        if (o == this)
        {
            return 0;
        }
        long d = (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
    }

    @Override
    public long getDelay(TimeUnit unit)
    {
        return unit.convert(this.expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
}
