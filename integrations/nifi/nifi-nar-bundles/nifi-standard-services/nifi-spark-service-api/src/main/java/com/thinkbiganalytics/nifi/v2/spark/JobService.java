package com.thinkbiganalytics.nifi.v2.spark;

/*-
 * #%L
 * thinkbig-nifi-hadoop-service-api
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

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

@Tags({"thinkbig", "spark"})
@CapabilityDescription("Provides long running spark contexts and shared RDDs")
public interface JobService extends ControllerService {

    /**
     * Checks to see if a context exists
     *
     * @return the true or false
     */
    Boolean checkIfContextExists(String ContextName);

    /**
     * Creates a new Spark Context
     *
     * @return the true or false
     */
    Boolean createContext(String ContextName, String NumExecutors, String MemPerNode, String NumCPUCores, SparkContextType ContextType);

    /**
     * Deletes a Spark Context
     *
     * @return the true or false
     */
    Boolean deleteContext(String ContextName);

    /**
     * Uploads a Spark Job Jar
     *
     * @return the true or false
     */
    Boolean uploadJar(String JarPath, String AppName);

    /**
     * Executes a Spark Job
     *
     * @return true or false
     */
    Boolean executeSparkContextJob(String AppName, String ClassPath, String ContextName, String Args);
}