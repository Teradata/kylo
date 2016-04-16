package com.thinkbiganalytics.jobrepo.query.support;

/**
 * Created by sr186054 on 9/16/15.
 */
public class DatabaseUnsupportedException extends RuntimeException {


    private String message;

    public DatabaseUnsupportedException() {
        initMessage();
    }

    public DatabaseUnsupportedException(Throwable cause) {
        super(cause);
        initMessage();
    }

    private void initMessage() {
        message = "Your database is not supported by Pipeline Controller";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return message;
    }
}
