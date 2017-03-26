package com.thinkbiganalytics.feedmgr.rest.model;



import com.fasterxml.jackson.annotation.JsonIgnore;

import org.joda.time.DateTime;

import java.util.UUID;

/**
 * Created by sr186054 on 3/24/17.
 */
public class UploadProgressMessage {

    private String messageKey;

    private String message;

    private DateTime dateTime;

    private boolean complete;

    private boolean success;


    public UploadProgressMessage() {
        this.messageKey = UUID.randomUUID().toString();

    }
    public UploadProgressMessage(String message) {
        this();
        this.message = message;
        this.dateTime = DateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void update(String message, boolean success){
        update(message);
        complete(success);
    }

    public void update(String message){
        setMessage(message);
        setDateTime(DateTime.now());
    }

    @JsonIgnore
    public void complete(boolean success){
        setComplete(true);
        setSuccess(success);
    }
}
