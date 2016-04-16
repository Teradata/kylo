package com.thinkbiganalytics.jobrepo.batch.scheduling;

/**
 * Services related to scheduling
 */
public interface SchedulerService {
    /**
     * Indicates if the global scheduler is considered active.  If you have a scheduled service, you should check this
     * value before scheduling items in order to allow turning off scheduling of additional items
     *
     * @return true if the scheduler is active, otherwise false
     */
    public boolean isSchedulerActive();

    /**
     * Set the current state of the scheduler and if it should be considered active
     *
     * @param active true if items should be scheduled, false otherwise
     */
    public void setSchedulerActive(final boolean active);
}
