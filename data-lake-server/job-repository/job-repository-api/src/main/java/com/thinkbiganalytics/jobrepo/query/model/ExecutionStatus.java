package com.thinkbiganalytics.jobrepo.query.model;

/**
 * Created by sr186054 on 4/13/16.
 */
public enum ExecutionStatus {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED,
    FAILED,
    COMPLETED,
    ABANDONED,
    UNKNOWN;

    private ExecutionStatus() {
    }
}
