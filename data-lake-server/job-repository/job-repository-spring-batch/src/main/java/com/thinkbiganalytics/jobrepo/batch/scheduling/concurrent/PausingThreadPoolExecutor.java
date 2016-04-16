package com.thinkbiganalytics.jobrepo.batch.scheduling.concurrent;


import com.thinkbiganalytics.jobrepo.batch.scheduling.SchedulerService;

import java.util.concurrent.*;

/**
 * A thread pool executor that looks to see if the scheduling service is paused before scheduling another thread.  If the
 * execution is pause, just sleep and wait until it becomes available then continue
 */
public class PausingThreadPoolExecutor extends ThreadPoolExecutor {
    protected SchedulerService schedulerService;

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even
     *                        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *                        pool
     * @param keepAliveTime   when the number of threads is greater than
     *                        the core, this is the maximum time that excess idle threads
     *                        will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are
     *                        executed.  This queue will hold only the {@code Runnable}
     *                        tasks submitted by the {@code execute} method.
     * @param threadFactory   the factory to use when the executor
     *                        creates a new thread
     * @param handler         the handler to use when execution is blocked
     *                        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException     if {@code workQueue}
     *                                  or {@code threadFactory} or {@code handler} is null
     */
    public PausingThreadPoolExecutor(int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                     BlockingQueue<Runnable> workQueue,
                                     ThreadFactory threadFactory,
                                     RejectedExecutionHandler handler,
                                     SchedulerService schedulerService) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.schedulerService = schedulerService;
    }

    /**
     * Method invoked prior to executing the given Runnable in the
     * given thread.  This method is invoked by thread {@code t} that
     * will execute task {@code r}, and may be used to re-initialize
     * ThreadLocals, or to perform logging.
     * <p>
     * This version additionally will check the scheduler service and determine if the service is
     * active.  If it is active it will immediately continue.  If not, it will sleep until the
     * scheduler service state changes to active.
     * <p>
     * <p>This implementation does nothing, but may be customized in
     * subclasses. Note: To properly nest multiple overridings, subclasses
     * should generally invoke {@code super.beforeExecute} at the end of
     * this method.
     *
     * @param t the thread that will run task {@code r}
     * @param r the task that will be executed
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        try {
            while (this.schedulerService != null &&
                    !this.schedulerService.isSchedulerActive()) {
                Thread.sleep(5000);
            }
            super.beforeExecute(t, r);
        } catch (InterruptedException ignored) {
        }
    }
}
