package com.thinkbiganalytics.jobrepo.batch.scheduling;

import javax.inject.Named;

/**
 * A simple cache of the state of the scheduler
 */
@Named
public class SchedulerServiceImpl implements SchedulerService {
    private boolean schedulerActive = true;

    /**
     * Indicates if the global scheduler is considered active.  If you have a scheduled service, you should check this
     * value before scheduling items in order to allow turning off scheduling of additional items
     *
     * @return true if the scheduler is active, otherwise false
     */
    @Override
    public boolean isSchedulerActive() {
        return this.schedulerActive;
    }

    /**
     * Set the current state of the scheduler and if it should be considered active
     *
     * @param active true if items should be scheduled, false otherwise
     */
    @Override
    public void setSchedulerActive(boolean active) {
        this.schedulerActive = active;
    }
}
