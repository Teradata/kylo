package com.thinkbiganalytics.jobrepo.nifi.model;

import java.util.Date;

/**
 * Created by sr186054 on 2/26/16.
 */
public interface RunStatus {

  boolean markRunning();

  boolean markCompleted();

  boolean markFailed();

  public boolean isInitial();

  boolean isRunning();

  boolean isComplete();

  RunStatusContext.RUN_STATUS getRunStatus();

  Date getStartTime();

  Date getEndTime();

  Date getUTCStartTime();

  Date getUTCEndTime();
}
