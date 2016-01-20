/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/*
Specifications for managed Hive tables
 */
public enum TableType {

    FEED("/model.db/", "/etl/", "feed", true, false, true),
    VALID("/model.db/", "/etl/", "valid", true, false, false),
    INVALID("/model.db/", "/etl", "invalid", true, false, true),
    PROTOTYPE("/model.db/", "/etl/", "proto"),
    MASTER("/app/warehouse/", "/app/warehouse/", "", false, true, false);

    private String tableRoot;
    private String dataRoot;
    private String suffix;
    private boolean orc;
    private boolean strings;
    private boolean feedPartition;

    TableType(String tableRoot, String dataRoot, String suffix) {
        this.tableRoot = tableRoot;
        this.dataRoot = dataRoot;
        this.suffix = suffix;
    }

    TableType(String tableRoot, String dataRoot, String suffix, boolean feedPartition) {
        this.tableRoot = tableRoot;
        this.dataRoot = dataRoot;
        this.suffix = suffix;
        this.feedPartition = feedPartition;
    }

    TableType(String tableRoot, String dataRoot, String suffix, boolean feedPartition, boolean orc, boolean strings) {
        this.tableRoot = tableRoot;
        this.dataRoot = dataRoot;
        this.suffix = suffix;
        this.feedPartition = feedPartition;
        this.orc = orc;
        this.strings = strings;
    }

    public String deriveTablename(String entity) {
        return entity + (!StringUtils.isEmpty(suffix) ? "_"+suffix : "");
    }

    public String deriveQualifiedName(String source, String entity) {
        return source + "." + deriveTablename(entity);
    }

    public String deriveLocationSpecification(String source, String entity) {
        StringBuffer sb = new StringBuffer();
        sb.append(" LOCATION '")
        .append(tableRoot)
        .append(source).append("/")
        .append(entity).append("/")
        .append(suffix).append("'");
        return sb.toString();
    }

    public String deriveColumnSpecification(ColumnSpec[] columns, ColumnSpec[] partitionColumns) {
        Set<String> partitionSet = new HashSet<>();
        if (!feedPartition && partitionColumns != null && partitionColumns.length > 0) {
            for (ColumnSpec partition : partitionColumns) {
                partitionSet.add(partition.getName());
            }
        }
        StringBuffer sb = new StringBuffer();
        int i = 0;
        for (ColumnSpec spec : columns) {
            if (!partitionSet.contains(spec.getName())) {
                if (i++ > 0) sb.append(", ");
                sb.append(spec.toCreateSQL(isStrings()));
            }
        }
        return sb.toString();
    }

    public String deriveFormatSpecification(String specification) {
        StringBuffer sb = new StringBuffer();
        if (isOrc()) {
            sb.append("STORED AS ORC ");
        } else {
            sb.append(specification);
        }
        return sb.toString();
    }

    public boolean isOrc() {
        return orc;
    }

    public boolean isStrings() {
        return strings;
    }

    public boolean isFeedPartition() {
        return feedPartition;
    }

    public String derivePartitionSpecification(ColumnSpec[] partitions) {

        StringBuffer sb = new StringBuffer();
        if (feedPartition) {
            sb.append(" PARTITIONED BY (`processing_dttm` string) ");
        } else {
            if (partitions != null && partitions.length > 0) {
                sb.append(" PARTITIONED BY (");
                int i = partitions.length;
                for (ColumnSpec partition : partitions) {
                    sb.append(partition.toPartitionSQL());
                    if (i-- > 1) {
                        sb.append(", ");
                    }
                }
                sb.append(") ");
            }
        }

        return sb.toString();
    }

/*
    public String buildFeedPartitionValue(String source, String entity, String value) {
        StringBuffer sb = new StringBuffer();
        sb.append(" PARTITION (processing_dttm='");
        sb.append(value);
        sb.append("') LOCATION ");
        return sb.toString();
    }
*/
}
