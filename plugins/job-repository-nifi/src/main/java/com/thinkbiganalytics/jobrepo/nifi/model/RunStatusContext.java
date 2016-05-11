package com.thinkbiganalytics.jobrepo.nifi.model;

import com.google.common.base.MoreObjects;
import com.thinkbiganalytics.jobrepo.nifi.support.DateTimeUtil;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sr186054 on 2/26/16.
 */
public class RunStatusContext implements RunStatus, Serializable {
    public enum RUN_STATUS {
        INITIAL, RUNNING, COMPLETED, FAILED
    }

    private RUN_STATUS runStatus = RUN_STATUS.INITIAL;
    private Date startTime;
    private Date endTime;

    public RunStatusContext() {

    }

    public RunStatusContext(RunStatusContext other) {
        this.runStatus = other.runStatus;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
    }


    public RunStatusContext(RUN_STATUS runStatus, Date startTime, Date endTime) {
        this.runStatus = runStatus;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void markInitial() {
        this.runStatus = RUN_STATUS.INITIAL;
    }


    @Override
    public boolean markRunning() {
        return markRunning(null);
    }

    @Override
    public boolean markCompleted() {
        return markCompleted(null);
    }

    @Override
    public boolean markFailed() {
        return markFailed(null);
    }

    @Override
    public boolean markRunning(DateTime dateTime) {
        if (!isRunning()) {
            this.runStatus = RUN_STATUS.RUNNING;
            this.startTime = dateTime != null ? dateTime.toDate() : new Date();
            return true;
        }
        return false;
    }

    @Override
    public boolean markCompleted(DateTime dateTime) {
        if (isRunning()) {
            this.runStatus = RUN_STATUS.COMPLETED;
            this.endTime = dateTime != null ? dateTime.toDate() : new Date();
            return true;
        }
        return false;
    }

    @Override
    public boolean markFailed(DateTime dateTime) {
        if (isRunning()) {
            this.runStatus = RUN_STATUS.FAILED;
            this.endTime = dateTime != null ? dateTime.toDate() : new Date();
            return true;
        }
        return false;
    }

    public boolean isInitial() {
        return RUN_STATUS.INITIAL.equals(getRunStatus());
    }

    @Override
    public boolean isRunning() {
        return RUN_STATUS.RUNNING.equals(getRunStatus());
    }

    public boolean isComplete() {
        return RUN_STATUS.COMPLETED.equals(getRunStatus());
    }

    @Override
    public RUN_STATUS getRunStatus() {
        return runStatus;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public Date getUTCStartTime() {

        if (startTime != null) {
            return DateTimeUtil.convertToUTC(startTime);
        }
        return null;
    }

    @Override
    public Date getUTCEndTime() {
        if (endTime != null) {
            return DateTimeUtil.convertToUTC(endTime);
        }
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("runStatus", runStatus)
                .add("startTime", startTime)
                .add("endTime", endTime)
                .toString();
    }
}
