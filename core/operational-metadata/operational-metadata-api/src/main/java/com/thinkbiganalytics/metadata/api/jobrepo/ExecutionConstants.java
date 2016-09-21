package com.thinkbiganalytics.metadata.api.jobrepo;

/**
 * Created by sr186054 on 9/1/16.
 */
public interface ExecutionConstants {


    enum ParamType {
        STRING, DOUBLE, DATE, LONG
    }


    enum ExitCode {
        COMPLETED,
        STOPPED,
        FAILED,
        ABANDONED,
        EXECUTING,
        NOOP,
        UNKNOWN;
    }
}
