package com.tanjx.timingwheel;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.mina.util.ExpirationListener;

/**
 * A timing-wheel optimized for approximated I/O timeout scheduling.<br>
 * {@link TimingWheel} creates a new thread whenever it is instantiated and started, so don't create many instances.
 * <p>
 * <b>The classic usage as follows:</b><br>
 * <li>using timing-wheel manage any object timeout</li>
 * <pre>
 *    // Create a timing-wheel with 60 ticks, and every tick is 1 second.
 *    private static final TimingWheel<CometChannel> TIMING_WHEEL = new TimingWheel<CometChannel>(1, 60, TimeUnit.SECONDS);
 *
 *    // Add expiration listener and start the timing-wheel.
 *    static {
 *     TIMING_WHEEL.addExpirationListener(new YourExpirationListener());
 *     TIMING_WHEEL.start();
 *    }
 *
 *    // Add one element to be timeout approximated after 60 seconds
 *    TIMING_WHEEL.add(e);
 *
 *    // Anytime you can cancel count down timer for element e like this
 *    TIMING_WHEEL.remove(e);
 * </pre>
 *
 * After expiration occurs, the {@link ExpirationListener} interface will be invoked and the expired object will be
 * the argument for callback method {@link ExpirationListener#expired(Object)}
 * <p>
 * {@link TimingWheel} is based on <a href="http://cseweb.ucsd.edu/users/varghese/">George Varghese</a> and Tony Lauck's paper,
 * <a href="http://cseweb.ucsd.edu/users/varghese/PAPERS/twheel.ps.Z">'Hashed and Hierarchical Timing Wheels: data structures
 * to efficiently implement a timer facility'</a>.  More comprehensive slides are located <a href="http://www.cse.wustl.edu/~cdgill/courses/cs6874/TimingWheels.ppt">here</a>.
 *
 * @author mindwind
 * @version 1.0, Sep 20, 2012
 */
public class TimingWheel<E>
{

    private final long tickDuration;
    private final int ticksPerWheel;
    private volatile int currentTickIndex = 0;

    private final CopyOnWriteArrayList<ExpirationListener<E>> expirationListeners = new CopyOnWriteArrayList<ExpirationListener<E>>();
    private final ArrayList<Slot<E>> wheel;
    private final Map<E, Slot<E>> indicator = new ConcurrentHashMap<E, Slot<E>>();

    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private Thread workerThread;

    // ~ -------------------------------------------------------------------------------------------------------------

    /**
     * Construct a timing wheel.
     *
     * @param tickDuration
     *            tick duration with specified time unit.
     * @param ticksPerWheel
     * @param timeUnit
     */
    public TimingWheel(int tickDuration, int ticksPerWheel, TimeUnit timeUnit)
    {
        if (timeUnit == null)
        {
            throw new NullPointerException("unit");
        }
        if (tickDuration <= 0)
        {
            throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
        }
        if (ticksPerWheel <= 0)
        {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }

        this.wheel = new ArrayList<Slot<E>>();
        this.tickDuration = TimeUnit.MILLISECONDS.convert(tickDuration, timeUnit);
        this.ticksPerWheel = ticksPerWheel + 1;

        for (int i = 0; i < this.ticksPerWheel; i++)
        {
            this.wheel.add(new Slot<E>(i));
        }
        this.wheel.trimToSize();

        this.workerThread = new Thread(new TickWorker(), "Timing-Wheel");
    }

    // ~ -------------------------------------------------------------------------------------------------------------

    public void start()
    {
        if (this.shutdown.get())
        {
            throw new IllegalStateException("Cannot be started once stopped");
        }

        if (!this.workerThread.isAlive())
        {
            this.workerThread.start();
        }
    }

    public boolean stop()
    {
        if (!this.shutdown.compareAndSet(false, true))
        {
            return false;
        }

        boolean interrupted = false;
        while (this.workerThread.isAlive())
        {
            this.workerThread.interrupt();
            try
            {
                this.workerThread.join(100);
            }
            catch (InterruptedException e)
            {
                interrupted = true;
            }
        }
        if (interrupted)
        {
            Thread.currentThread().interrupt();
        }

        return true;
    }

    public void addExpirationListener(ExpirationListener<E> listener)
    {
        this.expirationListeners.add(listener);
    }

    public void removeExpirationListener(ExpirationListener<E> listener)
    {
        this.expirationListeners.remove(listener);
    }

    /**
     * Add a element to {@link TimingWheel} and start to count down its life-time.
     *
     * @param e
     * @return remain time to be expired in millisecond.
     */
    public long add(E e)
    {
        synchronized (e)
        {
            this.checkAdd(e);

            int previousTickIndex = this.getPreviousTickIndex();
            Slot<E> slot = this.wheel.get(previousTickIndex);
            slot.add(e);
            this.indicator.put(e, slot);

            return (this.ticksPerWheel - 1) * this.tickDuration;
        }
    }

    private void checkAdd(E e)
    {
        Slot<E> slot = this.indicator.get(e);
        if (slot != null)
        {
            slot.remove(e);
        }
    }

    private int getPreviousTickIndex()
    {
        this.lock.readLock().lock();
        try
        {
            int cti = this.currentTickIndex;
            if (cti == 0)
            {
                return this.ticksPerWheel - 1;
            }

            return cti - 1;
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Removes the specified element from timing wheel.
     *
     * @param e
     * @return <tt>true</tt> if this timing wheel contained the specified
     *         element
     */
    public boolean remove(E e)
    {
        synchronized (e)
        {
            Slot<E> slot = this.indicator.get(e);
            if (slot == null)
            {
                return false;
            }

            this.indicator.remove(e);
            return slot.remove(e) != null;
        }
    }

    private void notifyExpired(int idx)
    {
        Slot<E> slot = this.wheel.get(idx);
        Set<E> elements = slot.elements();
        for (E e : elements)
        {
            slot.remove(e);
            synchronized (e)
            {
                Slot<E> latestSlot = this.indicator.get(e);
                if (latestSlot.equals(slot))
                {
                    this.indicator.remove(e);
                }
            }
            for (ExpirationListener<E> listener : this.expirationListeners)
            {
                listener.expired(e);
            }
        }
    }

    // ~ -------------------------------------------------------------------------------------------------------------

    private class TickWorker implements Runnable
    {

        private long startTime;
        private long tick;

        @Override
        public void run()
        {
            this.startTime = System.currentTimeMillis();
            this.tick = 1;

            for (int i = 0; !TimingWheel.this.shutdown.get(); i++)
            {
                if (i == TimingWheel.this.wheel.size())
                {
                    i = 0;
                }
                TimingWheel.this.lock.writeLock().lock();
                try
                {
                    TimingWheel.this.currentTickIndex = i;
                }
                finally
                {
                    TimingWheel.this.lock.writeLock().unlock();
                }
                TimingWheel.this.notifyExpired(TimingWheel.this.currentTickIndex);
                this.waitForNextTick();
            }
        }

        private void waitForNextTick()
        {
            for (;;)
            {
                long currentTime = System.currentTimeMillis();
                long sleepTime = TimingWheel.this.tickDuration * this.tick - (currentTime - this.startTime);

                if (sleepTime <= 0)
                {
                    break;
                }

                try
                {
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException e)
                {
                    return;
                }
            }

            this.tick++;
        }
    }

    private static class Slot<E>
    {

        private int id;
        private Map<E, E> elements = new ConcurrentHashMap<E, E>();

        public Slot(int id)
        {
            this.id = id;
        }

        public void add(E e)
        {
            this.elements.put(e, e);
        }

        public E remove(E e)
        {
            return this.elements.remove(e);
        }

        public Set<E> elements()
        {
            return this.elements.keySet();
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.id;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (this.getClass() != obj.getClass())
            {
                return false;
            }
            @SuppressWarnings("rawtypes")
            Slot other = (Slot) obj;
            if (this.id != other.id)
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Slot [id=" + this.id + ", elements=" + this.elements + "]";
        }

    }

}
