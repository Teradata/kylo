package com.thinkbiganalytics.nifi.v2.sqoop.enums;

/**
 * List of supported strategies for handling case where target HDFS directory exists
 * @author jagrut sharma
 */
public enum TargetHdfsDirExistsStrategy {
    DELETE_DIR_AND_IMPORT,
    FAIL_IMPORT;

    @Override
    public String toString() {
        switch(this) {
            case DELETE_DIR_AND_IMPORT:
                return "DELETE_DIR_AND_IMPORT";
            case FAIL_IMPORT:
                return "FAIL_IMPORT";
        }
        return "";
    }
}
