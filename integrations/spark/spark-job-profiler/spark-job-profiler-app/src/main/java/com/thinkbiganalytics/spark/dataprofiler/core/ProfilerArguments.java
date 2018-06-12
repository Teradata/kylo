/**
 * 
 */
package com.thinkbiganalytics.spark.dataprofiler.core;

/*-
 * #%L
 * kylo-spark-job-profiler-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.spark.policy.FieldPolicyLoader;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ProfilerArguments {
    
    private static final Logger log = LoggerFactory.getLogger(ProfilerArguments.class);

    private final boolean tableBased;
    private final String sql;
    private final String table;
    private final Integer numberOfTopNValues;
    private final String profileOutputTable;
    private final String fieldPolicyJsonPath;
    private final String inputAndOutputTablePartitionKey;
    private final List<String> profiledColumns = new ArrayList<>();
    
    /**
     * 
     */
    public ProfilerArguments(final String[] args, FieldPolicyLoader loader) {
        if (log.isInfoEnabled()) {
            log.info("Running Spark Profiler with the following command line {} args (comma separated): {}", args.length, StringUtils.join(args, ","));
        }

        if (args.length < 5) {
            log.error("Invalid number of command line arguments ({})", args.length);
            throw new IllegalArgumentException("Invalid number of command line arguments (" + args.length + ")");
        }

        this.numberOfTopNValues = Integer.valueOf(args[2]);
        this.profileOutputTable = args[3];
        this.fieldPolicyJsonPath = args[4];
        
        if (args.length >= 6) {
            inputAndOutputTablePartitionKey = args[5];
        } else {
            this.inputAndOutputTablePartitionKey = "ALL";
        }
        
        final String profileObjectType = args[0];
        final String descr = args[1];
        
        switch (profileObjectType) {
            case "table":
                this.tableBased = true;
                this.sql = null;
                
                // Quote source table
                final String[] tableRef = descr.split("\\.", 2);
                this.table = tableRef.length == 1 ? HiveUtils.quoteIdentifier(tableRef[0]) : HiveUtils.quoteIdentifier(tableRef[0], tableRef[1]);
                
                for (FieldPolicy fieldPolicy : loader.loadFieldPolicy(fieldPolicyJsonPath).values()) {
                    if (fieldPolicy.isProfile()) {
                        profiledColumns.add(HiveUtils.quoteIdentifier(fieldPolicy.getField().toLowerCase()));
                    }
                }
                break;
            case "query":
                this.tableBased = false;
                this.sql = descr;
                this.table = null;
                break;
            default:
                log.error("Illegal command line argument for object type ({})", profileObjectType);
                throw new IllegalArgumentException("Illegal command line argument for object type (" + profileObjectType + ")");
        }

        if (this.numberOfTopNValues <= 0) {
            log.error("Illegal command line argument for n for top_n values ({})", this.numberOfTopNValues);
            throw new IllegalArgumentException("Illegal command line argument for n for top_n values (" + this.numberOfTopNValues + ")");
        }
    }

    public boolean isTableBased() {
        return tableBased;
    }
    
    public String getSql() {
        return sql;
    }
    
    public String getTable() {
        return table;
    }

    public Integer getNumberOfTopNValues() {
        return numberOfTopNValues;
    }

    public String getProfileOutputTable() {
        return profileOutputTable;
    }

    public String getFieldPolicyJsonPath() {
        return fieldPolicyJsonPath;
    }

    public String getInputAndOutputTablePartitionKey() {
        return inputAndOutputTablePartitionKey;
    }

    public List<String> getProfiledColumns() {
        return profiledColumns;
    }
}
