package com.thinkbiganalytics.jira;

/**
 * Created by sr186054 on 10/16/15.
 */
public class JiraException extends Exception {

    public JiraException() {
    }

    public JiraException(String message) {
        super(message);
    }

    public JiraException(String message, Throwable cause) {
        super(message, cause);
    }

    public JiraException(Throwable cause) {
        super(cause);
    }

    public JiraException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
