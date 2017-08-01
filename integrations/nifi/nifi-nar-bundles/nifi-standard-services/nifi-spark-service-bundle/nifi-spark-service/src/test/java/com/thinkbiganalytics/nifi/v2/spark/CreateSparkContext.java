package com.thinkbiganalytics.nifi.v2.spark;

/*-
 * #%L
 * thinkbig-nifi-core-service
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


public class CreateSparkContext implements Runnable {

    private final JobService jobService;
    private final String contextName;
    private final String numExecutors;
    private final String memPerNode;
    private final String numCPUCores;
    private final SparkContextType sparkContextType;
    private final int contextTimeout;
    private final boolean async;


    public void run() {
        jobService.createContext(contextName, numExecutors, memPerNode, numCPUCores, sparkContextType, contextTimeout, async);
    }

    public CreateSparkContext(String contextName, String numExecutors, String memPerNode, String numCPUCores, SparkContextType sparkContextType, int contextTimeout, boolean async,
                              JobService jobService) {
        this.jobService = jobService;
        this.contextName = contextName;
        this.numExecutors = numExecutors;
        this.memPerNode = memPerNode;
        this.numCPUCores = numCPUCores;
        this.sparkContextType = sparkContextType;
        this.contextTimeout = contextTimeout;
        this.async = async;
    }
}
