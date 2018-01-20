package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * kylo-spark-validate-cleanse-app
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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Defines acceptable command line parameters for Validator application.
 */
public class ValidatorConfiguration {

    private static final Integer DEFAULT_NUM_PARTITIONS = -1;
    private static final String DEFAULT_STORAGE_LEVEL = "MEMORY_AND_DISK";

    @Parameter(names = {"-h", "--hiveConf"}, description = "Hive configuration parameters", converter = ParameterConverter.class)
    private List<Param> hiveParams;

    @Parameter(names = "--numPartitions", description = "Number of RDD partitions")
    private Integer numPartitions = DEFAULT_NUM_PARTITIONS;

    @Parameter(names = "--storageLevel", description = "Storage for RDD persistance")
    private String storageLevel = DEFAULT_STORAGE_LEVEL;

    private final String entity;
    private String fieldPolicyJsonPath;
    private String partition;
    private final String targetDatabase;

    public ValidatorConfiguration(@Nonnull final String[] args) {
        targetDatabase = args[0];
        entity = args[1];
        partition = args[2];
        fieldPolicyJsonPath = args[3];

        new JCommander(this).parse(Arrays.copyOfRange(args, 4, args.length));
    }

    public String getFeedTableName() {
        return entity + "_feed";
    }

    public String getFieldPolicyJsonPath() {
        return fieldPolicyJsonPath;
    }

    public List<Param> getHiveParams() {
        return hiveParams == null ? Collections.<Param>emptyList() : hiveParams;
    }

    public String getInvalidTableName() {
        return entity + "_invalid";
    }

    public Integer getNumPartitions() {
        return numPartitions;
    }

    public String getPartition() {
        return partition;
    }

    public String getProfileTableName() {
        return entity + "_profile";
    }

    public String getStorageLevel() {
        return storageLevel;
    }

    public String getTargetDatabase() {
        return targetDatabase;
    }

    public String getValidTableName() {
        return entity + "_valid";
    }
}
