package com.thinkbiganalytics.jobrepo.query.model;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface CheckDataJob extends ExecutedJob {
    boolean isValid();

    void setIsValid(boolean isValid);

    String getValidationMessage();

    void setValidationMessage(String validationMessage);


}
