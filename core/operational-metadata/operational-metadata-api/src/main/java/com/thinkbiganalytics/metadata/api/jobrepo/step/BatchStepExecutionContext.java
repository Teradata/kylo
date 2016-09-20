package com.thinkbiganalytics.metadata.api.jobrepo.step;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchStepExecutionContext {

    Long getStepExecutionId();

    String getShortContext();

    String getSerializedContext();
}
