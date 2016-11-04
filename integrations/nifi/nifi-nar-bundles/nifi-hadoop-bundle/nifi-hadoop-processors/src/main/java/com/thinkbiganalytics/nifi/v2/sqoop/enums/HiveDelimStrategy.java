package com.thinkbiganalytics.nifi.v2.sqoop.enums;

/**
 * @author jagrut sharma
 */
/*
List of Hive delimiter handling strategies
 */
public enum HiveDelimStrategy {
    DROP,
    KEEP,
    REPLACE;

    @Override
    public String toString() {
        switch (this) {
            case DROP:
                return "DROP";
            case KEEP:
                return "KEEP";
            case REPLACE:
                return "REPLACE";
        }
        return "";
    }
}
