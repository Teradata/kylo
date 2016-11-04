package com.thinkbiganalytics.nifi.v2.sqoop.enums;

/**
 * @author jagrut sharma
 */
/*
List of compression algorithms
 */
public enum CompressionAlgorithm {
    NONE,
    GZIP,
    SNAPPY,
    BZIP2,
    LZO;

    @Override
    public String toString() {
        switch (this) {
            case NONE:
                return "NONE";
            case GZIP:
                return "GZIP";
            case SNAPPY:
                return "SNAPPY";
            case BZIP2:
                return "BZIP2";
            case LZO:
                return "LZO";
        }
        return "";
    }
}
