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


public class ExecuteSparkContextJob implements Runnable {

    private final JobService jobService;
    private final String appName;
    private final String classPath;
    private final String contextName;
    private final String args;
    private final boolean async;
    private boolean success = false;


    public ExecuteSparkContextJob(String appName, String classPath, String contextName, String args, boolean async, JobService jobService) {
        this.jobService = jobService;
        this.appName = appName;
        this.classPath = classPath;
        this.contextName = contextName;
        this.args = args;
        this.async = async;
    }

    public void run() {
        success = jobService.executeSparkContextJob(appName, classPath, contextName, args, async).success;
    }

    public boolean jobSuccessfull() {
        return success;
    }
}
