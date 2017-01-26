package com.thinkbiganalytics.jobrepo.common.constants;

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
 * Common constants to identify properties for data confidence/check data jobs
 */
public interface CheckDataStepConstants {

    /**
     * Property in the Step Execution context that identifies if the job is valid or not.
     * The Value for this property should be a boolean string either "true" or "false"
     */
    String VALIDATION_KEY = "checkData.validationCheck";

    /**
     * The property in the StepExecutionContext that identifies a message describing the validation job results
     **/
    String VALIDATION_MESSAGE_KEY = "checkData.validationMessage";
}
