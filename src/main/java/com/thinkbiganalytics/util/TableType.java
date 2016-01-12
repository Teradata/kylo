/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

/*
Specifications for managed Hive tables
 */
public enum TableType {

    FEED("/prototypes/", "/etl/", "_feed", true, false, true),
    VALID("/prototypes/", "/etl/", "_valid", true, false, false),
    INVALID("/prototypes/", "/etl", "_invalid", true, false, true),
    PROTOTYPE("/prototypes/", "/etl/", "_proto"),
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
        return entity + suffix;
    }

    public String deriveQualifiedName(String source, String entity) {
        return source + "." + deriveTablename(entity);
    }

    public String deriveDDLTableLocation(String source, String entity) {
        StringBuffer sb = new StringBuffer();
        sb.append(tableRoot);
        sb.append(source).append("/");
        sb.append(entity).append("/");
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

    public String createDDLFeedPartitionPart() {
        StringBuffer sb = new StringBuffer();
        sb.append(" PARTITIONED BY (processing_dttm) ");
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
