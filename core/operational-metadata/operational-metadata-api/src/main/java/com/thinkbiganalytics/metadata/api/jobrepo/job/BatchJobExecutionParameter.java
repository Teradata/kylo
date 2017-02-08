package com.thinkbiganalytics.metadata.api.jobrepo.job;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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

import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;

import org.joda.time.DateTime;

/**
 * Represents a job parameter that refers back to a {@link BatchJobExecution}
 */
public interface BatchJobExecutionParameter {

    /**
     * Return the job execution this parameter is part of
     *
     * @return the job execution for this parameter
     */
    BatchJobExecution getJobExecution();

    /**
     * The data type for this parameter.
     *
     * @return data type for this parameter.
     */
    ExecutionConstants.ParamType getTypeCode();

    /**
     * Return the parameter name
     *
     * @return the parameter name
     */
    String getKeyName();

    /**
     * Return the value of the parameter as a string
     *
     * @return the value of the parameter as a string
     */
    String getStringVal();

    /**
     * set the parameter value
     */
    void setStringVal(String val);


    DateTime getDateVal();

    Long getLongVal();

    Double getDoubleVal();
}
