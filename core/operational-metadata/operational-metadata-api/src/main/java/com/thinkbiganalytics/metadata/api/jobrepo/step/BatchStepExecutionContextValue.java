package com.thinkbiganalytics.metadata.api.jobrepo.step;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchStepExecutionContextValue {

    BatchStepExecution getStepExecution();

    String getKeyName();

    String getId();

    Long getJobExecutionId();

    String getStringVal();
}
