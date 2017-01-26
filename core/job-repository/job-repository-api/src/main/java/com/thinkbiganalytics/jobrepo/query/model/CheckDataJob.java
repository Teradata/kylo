package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-api
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
 * Represents the result of a Data Confidence check job
 */
public interface CheckDataJob extends ExecutedJob {

    /**
     * Return a flag if the Job is valid and passes the verification job
     *
     * @return true if job passes, false if not
     */
    boolean isValid();

    /**
     * set the valid flag
     */
    void setIsValid(boolean isValid);

    /**
     * Return the validation message
     *
     * @return the validation message
     */
    String getValidationMessage();

    /**
     * set the validation message
     */
    void setValidationMessage(String validationMessage);


}
