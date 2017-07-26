package com.thinkbiganalytics.spark.service;

/*-
 * #%L
 * thinkbig-spark-shell-client-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.util.concurrent.AbstractScheduledService;

import org.joda.time.DateTimeUtils;

import java.util.concurrent.TimeUnit;

/**
 * Monitors for activity and stops when the idle timeout occurs.
 */
public class IdleMonitorService extends AbstractScheduledService {

    /**
     * Runs when this app is expected to be idle.
     */
    private final class IdleScheduler extends AbstractScheduledService.CustomScheduler {

        /**
         * Gets the number of milliseconds since the last activity.
         */
        long getIdleMillis() {
            return DateTimeUtils.currentTimeMillis() - lastActivity;
        }

        /**
         * Indicates if this app is idle.
         */
        boolean isIdle() {
            return idleUnit.toMillis(idleTimeout) <= getIdleMillis();
        }

        @Override
        protected Schedule getNextSchedule() {
            final long delay = Math.max(idleUnit.toMillis(idleTimeout) - getIdleMillis(), 0);
            return new Schedule(delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Time to wait after the last activity for this app to become idle.
     */
    private final long idleTimeout;

    /**
     * Unit for the idle timeout
     */
    private final TimeUnit idleUnit;

    /**
     * Time of the last activity in milliseconds
     */
    private long lastActivity;

    /**
     * Schedule for checking the idle timeout
     */
    private IdleScheduler scheduler = new IdleScheduler();

    /**
     * Constructs an {@code IdleMonitorService} with the specified idle timeout.
     *
     * @param timeout time to wait after the last activity
     * @param unit the time unit
     */
    public IdleMonitorService(final long timeout, final TimeUnit unit) {
        this.idleTimeout = timeout;
        this.idleUnit = unit;
        reset();
    }

    /**
     * Waits uninterruptedly for this app to become idle.
     */
    public void awaitIdleTimeout() {
        final State state = state();
        if ((state == State.RUNNING || state == State.STARTING) && idleTimeout > 0) {
            awaitTerminated();
        }
    }

    /**
     * Resets the last activity to the current time.
     */
    public void reset() {
        lastActivity = DateTimeUtils.currentTimeMillis();
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (scheduler.isIdle()) {
            stopAsync();
        }
    }

    @Override
    protected Scheduler scheduler() {
        return scheduler;
    }
}
