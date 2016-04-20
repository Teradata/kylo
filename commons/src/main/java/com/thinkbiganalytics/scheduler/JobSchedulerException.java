package com.thinkbiganalytics.scheduler;

/**
 * Created by sr186054 on 9/23/15.
 */
public class JobSchedulerException extends Exception{

    public JobSchedulerException() {
    }

    public JobSchedulerException(String message) {
        super(message);
    }

    public JobSchedulerException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobSchedulerException(Throwable cause) {
        super(cause);
    }

    public JobSchedulerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
