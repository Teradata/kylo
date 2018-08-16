package com.thinkbiganalytics.nifi.v2.hdfs;

/*-
 * #%L
 * thinkbig-nifi-hadoop-properties-v1.2
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

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.Validator;

/**
 * Additional properties for Hadoop processors (NiFi version 1.2+)
 */
public class AdditionalProperties {

    public static PropertyDescriptor getHdfsAdditionalClasspathResources() {
        return new PropertyDescriptor.Builder()
            .name("Additional Classpath Resources")
            .description("A comma-separated list of paths to files and/or directories that will be added to the classpath. When specifying a " +
                         "directory, all files with in the directory will be added to the classpath, but further sub-directories will not be included.")
            .required(false)
            .addValidator(Validator.VALID)
            .dynamicallyModifiesClasspath(true)
            .build();
    }

    public static String getNiFiVersion() {
        return "v1.2+";
    }

}
