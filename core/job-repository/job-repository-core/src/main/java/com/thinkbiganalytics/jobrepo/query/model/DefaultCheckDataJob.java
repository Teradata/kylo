package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-core
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
 * Data Confidence job object
 * Built from the transform class
 *
 * @see com.thinkbiganalytics.jobrepo.query.model.transform.JobModelTransform
 */
public class DefaultCheckDataJob extends DefaultExecutedJob implements CheckDataJob {

    private boolean isValid;
    private String validationMessage;

    public DefaultCheckDataJob(ExecutedJob job) {
        super(job);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public String getValidationMessage() {
        return validationMessage;
    }

    @Override
    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }


}
