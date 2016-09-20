package com.thinkbiganalytics.metadata.api.jobrepo.job;

import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchJobExecutionParameter {

    BatchJobExecution getJobExecution();

    ExecutionConstants.ParamType getTypeCode();

    String getKeyName();

    String getStringVal();

    void setStringVal(String val);

    DateTime getDateVal();

    Long getLongVal();

    Double getDoubleVal();
}
