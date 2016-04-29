/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/*
Specifications for managed Hive tables
 */
public enum TableType {

    FEED("/model.db/", "/etl/", "feed", true, false, true, false),
    VALID("/model.db/", "/etl/", "valid", true, true, false, false),
    INVALID("/model.db/", "/etl", "invalid", true, true, true, true),
    MASTER("/app/warehouse/", "/app/warehouse/", "", false, true, false, false),
    PROFILE("/model.db/", "/etl/", "profile", true, true, true, false);

    private String tableLocation;
    private String partitionLocation;
    private String tableSuffix;
    private boolean orc;
    private boolean strings;
    private boolean feedPartition;
    private boolean addReasonCode;


    TableType(String tableRoot, String dataRoot, String suffix, boolean feedPartition, boolean orc, boolean strings, boolean addReasonCode) {
        this.tableLocation = tableRoot;
        this.partitionLocation = dataRoot;
        this.tableSuffix = suffix;
        this.feedPartition = feedPartition;
        this.orc = orc;
        this.strings = strings;
        this.addReasonCode = addReasonCode;
    }

    public String deriveTablename(String entity) {
        return entity + (!StringUtils.isEmpty(tableSuffix) ? "_" + tableSuffix : "");
    }

    public String deriveQualifiedName(String source, String entity) {
        return source + "." + deriveTablename(entity);
    }

    public String deriveLocationSpecification(String source, String entity) {
        StringBuffer sb = new StringBuffer();
        sb.append(" LOCATION '")
                .append(tableLocation)
                .append(source).append("/")
                .append(entity).append("/")
                .append(tableSuffix).append("'");
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
        // Handle the special case for writing error reason in invalid table
        if (addReasonCode) {
            sb.append(", dlp_reject_reason string ");
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
