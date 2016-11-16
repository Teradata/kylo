package com.thinkbiganalytics.nifi.v2.sqoop.enums;

/**
 * List of supported formats for landing extracted data on HDFS
 * @author jagrut sharma
 */
public enum ExtractDataFormat {
    TEXT,
    AVRO,
    SEQUENCE_FILE,
    PARQUET;

    @Override
    public String toString() {
        switch (this) {
            case TEXT:
                return "TEXT";
            case AVRO:
                return "AVRO";
            case SEQUENCE_FILE:
                return "SEQUENCE_FILE";
            case PARQUET:
                return "PARQUET";
        }
        return "";
    }
}
