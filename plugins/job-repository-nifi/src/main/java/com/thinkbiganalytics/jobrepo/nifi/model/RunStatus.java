package com.thinkbiganalytics.jobrepo.nifi.model;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by sr186054 on 2/26/16.
 */
public interface RunStatus {

    public boolean isInitial();

    boolean isRunning();

    boolean isComplete();

    RunStatusContext.RUN_STATUS getRunStatus();

    Date getStartTime();

    Date getEndTime();

    Date getUTCStartTime();

    Date getUTCEndTime();


    boolean markRunning(DateTime dateTime);

    boolean markCompleted(DateTime dateTime);

   boolean markFailed(DateTime dateTime);

    boolean markRunning();

    boolean markCompleted();

    boolean markFailed();
}
