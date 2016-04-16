package com.thinkbiganalytics.jobrepo.query.model;

/**
 * Created by sr186054 on 8/28/15.
 */
public class DefaultCheckDataJob extends DefaultExecutedJob implements CheckDataJob {

    public DefaultCheckDataJob(ExecutedJob job) {
        super(job);
    }


    private boolean isValid;
    private String validationMessage;

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public String getValidationMessage() {
        return validationMessage;
    }

    @Override
    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }


}
