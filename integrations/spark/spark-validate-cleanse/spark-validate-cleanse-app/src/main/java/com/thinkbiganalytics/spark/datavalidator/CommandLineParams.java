package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * thinkbig-spark-validate-cleanse-app
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

import com.beust.jcommander.Parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines acceptable command line parameters for Validator application.
 */
public class CommandLineParams implements Serializable {

    @Parameter(names = {"-h", "--hiveConf"}, description = "Hive configuration parameters", converter = ParameterConverter.class)
    private List<Param> hiveParams;

    @Parameter(names = "--storageLevel", description = "Storage for RDD persistance")
    private String storageLevel = "MEMORY_AND_DISK";

    public List<Param> getHiveParams() {
        return hiveParams == null ? new ArrayList<Param>(0) : hiveParams;
    }

    public String getStorageLevel() {
        return storageLevel;
    }
}
