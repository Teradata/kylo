package com.thinkbiganalytics.jobrepo.batch.scheduling.concurrent;

import com.thinkbiganalytics.jobrepo.batch.scheduling.SchedulerService;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * A Pausable thread pool executor that will look and see if the current state of the system is such that no new threads should be
 * scheduled.  If the scheduler is paused, it will loop and sleep until it detects that the system is no longer paused.
 */
public class PausableThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
    protected ThreadPoolExecutor threadPoolExecutor;
    protected int queueCapacity = Integer.MAX_VALUE;
    protected SchedulerService schedulerService;

    /**
     * Create an instance of a pausing thread pool executor so that it will recognize if the current state of the scheduler
     * is in a pause state
     *
     * @param threadFactory            The thread factory to use for creating threads
     * @param rejectedExecutionHandler The handler for when a thread is rejected
     * @return an instance of the thread pool executor
     */
    @Override
    protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        super.initializeExecutor(threadFactory, rejectedExecutionHandler);
        this.threadPoolExecutor = new PausingThreadPoolExecutor(getCorePoolSize(), getMaxPoolSize(), getKeepAliveSeconds(),
                TimeUnit.SECONDS, createQueue(this.queueCapacity), threadFactory, rejectedExecutionHandler, this.schedulerService);

        return this.threadPoolExecutor;
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
        return this.threadPoolExecutor;
    }

    @Override
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        super.setQueueCapacity(queueCapacity);
    }

    public void setSchedulerService(final SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }
}
