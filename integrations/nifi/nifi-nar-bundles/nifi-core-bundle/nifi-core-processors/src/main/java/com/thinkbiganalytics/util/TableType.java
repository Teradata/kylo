package com.thinkbiganalytics.util;

/*-
 * #%L
 * thinkbig-nifi-core-processors
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.hive.util.HiveUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/*
Specifications for managed Hive tables
 */
public enum TableType {

    FEED("feed", true, false, true, false, true),
    VALID("valid", true, true, false, false, false),
    INVALID("invalid", true, true, true, true, false),
    MASTER("", false, true, false, false, false, true),
    PROFILE("profile", true, true, true, false, false);

    //private String tableLocation;
    //private String partitionLocation;
    private String tableSuffix;
    private boolean useTargetStorageSpec;
    private boolean strings;
    private boolean feedPartition;
    private boolean addReasonCode;
    private boolean external;
    private boolean appendProcessingDttmField;

    TableType(String suffix, boolean feedPartition, boolean useTargetStorageSpec, boolean strings, boolean addReasonCode, boolean external, boolean appendProcessingDttmField) {
        this.tableSuffix = suffix;
        this.feedPartition = feedPartition;
        this.useTargetStorageSpec = useTargetStorageSpec;
        this.strings = strings;
        this.addReasonCode = addReasonCode;
        this.external = external;
        this.appendProcessingDttmField = appendProcessingDttmField;
    }


    TableType(String suffix, boolean feedPartition, boolean useTargetStorageSpec, boolean strings, boolean addReasonCode, boolean external) {
        this(suffix, feedPartition, useTargetStorageSpec, strings, addReasonCode, external, false);
    }

    public String deriveTablename(String entity) {
        return entity + (!StringUtils.isEmpty(tableSuffix) ? "_" + tableSuffix : "");
    }

    public String deriveQualifiedName(String source, String entity) {
        return HiveUtils.quoteIdentifier(source.trim(), deriveTablename(entity.trim()));
    }

    public String deriveLocationSpecification(Path tableLocation, String source, String entity) {

        Validate.notNull(tableLocation, "tableLocation expected");
        Validate.notNull(source, "source expected");
        Validate.notNull(entity, "entity expected");

        Path path = tableLocation.resolve(source).resolve(entity).resolve(tableSuffix);
        String location = path.toString().replace(":/", "://");

        StringBuffer sb = new StringBuffer();
        sb.append(" LOCATION '");

        sb.append(location).append("'");

        return sb.toString();
    }

    public boolean isStrings(String feedFormatOptions) {
        boolean allStrings = strings;
        // Hack for now. Need a better way to identify if this is text file (no schema enforced or schema enforced)
        if (allStrings && feedFormatOptions != null) {
            String urawFormatOptions = feedFormatOptions.toUpperCase();
            if (urawFormatOptions.contains(" PARQUET") || urawFormatOptions.contains(" ORC") || urawFormatOptions.contains(" AVRO") || urawFormatOptions.contains("JSON")) {
                // Structured file so we will use native
                allStrings = false;
            }
        }
        return allStrings;
    }

    public String deriveColumnSpecification(ColumnSpec[] columns, ColumnSpec[] partitionColumns, String feedFormatOptions) {
        boolean allStrings = isStrings(feedFormatOptions);
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
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append(spec.toCreateSQL(allStrings));
            }
        }
        // Handle the special case for writing error reason in invalid table
        if (addReasonCode) {
            sb.append(", dlp_reject_reason string ");
        }
        if (appendProcessingDttmField) {
            sb.append(", processing_dttm string");
        }
        return sb.toString();
    }

    /**
     * Derive the STORED AS clause for the table
     *
     * @param rawSpecification    the clause for the raw specification
     * @param targetSpecification the target specification
     */
    public String deriveFormatSpecification(String rawSpecification, String targetSpecification) {
        StringBuffer sb = new StringBuffer();
        if (isUseTargetStorageSpec()) {
            sb.append(targetSpecification);
        } else {
            sb.append(rawSpecification);
        }
        return sb.toString();
    }

    public boolean isUseTargetStorageSpec() {
        return useTargetStorageSpec;
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


    public String deriveTableProperties(String targetTableProperties) {
        if (isUseTargetStorageSpec()) {
            return targetTableProperties;
        }
        return "";
    }

    public boolean isExternal() {
        return external;
    }
}
