package com.thinkbiganalytics.nifi.feedmgr;

/**
 * Created by sr186054 on 6/15/16.
 */
public class TemplateCreationException extends RuntimeException {


    /**
     * @param message
     */
    public TemplateCreationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public TemplateCreationException(String message, Throwable cause) {
        super(cause);
    }
}
