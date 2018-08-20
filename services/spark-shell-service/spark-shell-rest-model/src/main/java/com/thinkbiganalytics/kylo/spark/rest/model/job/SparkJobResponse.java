package com.thinkbiganalytics.kylo.spark.rest.model.job;

/*-
 * #%L
 * Spark Shell Service REST Model
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thinkbiganalytics.spark.rest.model.SimpleResponse;

/**
 * The status and result of a Spark job.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SparkJobResponse extends SimpleResponse {

    private String id;
    private SparkJobResult result;

    /**
     * Gets the identifier of the job.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the result of the job.
     */
    public SparkJobResult getResult() {
        return result;
    }

    public void setResult(SparkJobResult result) {
        this.result = result;
    }
}
