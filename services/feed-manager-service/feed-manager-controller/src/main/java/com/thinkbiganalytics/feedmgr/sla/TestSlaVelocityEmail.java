package com.thinkbiganalytics.feedmgr.sla;

import com.thinkbiganalytics.common.velocity.model.VelocityEmailTemplate;

/**
 * Created by sr186054 on 10/22/17.
 */
public class TestSlaVelocityEmail extends VelocityEmailTemplate {

    private String emailAddress;

    private boolean success;

    private String exceptionMessage;
    public TestSlaVelocityEmail(){

    }

    public TestSlaVelocityEmail(String subject, String body, String emailAddress) {
        super(subject, body);
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
