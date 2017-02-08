package com.thinkbiganalytics.jobrepo.repository.rest.model;

/*-
 * #%L
 * thinkbig-job-repository-rest-model
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

/**
 * Store data for job repository requests to identify what should be returned back in the response
 */
public class JobAction {

    private String action;
    private boolean includeSteps;


    public JobAction() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isIncludeSteps() {
        return includeSteps;
    }

    public void setIncludeSteps(boolean includeSteps) {
        this.includeSteps = includeSteps;
    }
}
