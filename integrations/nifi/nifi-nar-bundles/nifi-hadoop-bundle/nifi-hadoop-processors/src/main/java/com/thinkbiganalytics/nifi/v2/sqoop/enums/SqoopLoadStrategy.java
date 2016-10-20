package com.thinkbiganalytics.nifi.v2.sqoop.enums;

/**
 * @author jagrut sharma
 */
/*
List of sqoop load strategies
 */
public enum SqoopLoadStrategy {
    FULL_LOAD,
    INCREMENTAL_LASTMODIFIED,
    INCREMENTAL_APPEND;

    @Override
    public String toString() {
        switch (this) {
            case FULL_LOAD:
                return "FULL_LOAD";
            case INCREMENTAL_LASTMODIFIED:
                return "INCREMENTAL_LASTMODIFIED";
            case INCREMENTAL_APPEND:
                return "INCREMENTAL_APPEND";
        }
        return null;
    }
}
