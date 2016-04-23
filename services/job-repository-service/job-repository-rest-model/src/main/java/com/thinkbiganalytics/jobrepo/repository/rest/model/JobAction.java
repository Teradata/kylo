package com.thinkbiganalytics.jobrepo.repository.rest.model;

/**
 * Created by sr186054 on 12/14/15.
 */
public class JobAction {
    private String action;
    private boolean includeSteps;


    public JobAction() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isIncludeSteps() {
        return includeSteps;
    }

    public void setIncludeSteps(boolean includeSteps) {
        this.includeSteps = includeSteps;
    }
}
