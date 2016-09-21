package com.thinkbiganalytics.metadata.api.jobrepo.nifi;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface NifiRelatedRootFlowFiles {

    NifiEventJobExecution getEventJobExecution();

    NifiEvent getEvent();
}
