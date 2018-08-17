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

/**
 * A request to execute a Spark job.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SparkJobRequest {

    /**
     * A mode of execution
     */
    public enum Mode {
        /**
         * Job result should be ignored
         */
        BATCH,

        /**
         * Job results in a table
         */
        INTERACTIVE
    }

    private String lang;
    private Mode mode;
    private SparkJobParent parent;
    private SparkJobResources resources;
    private String script;

    /**
     * Gets the lanugage of the script.
     */
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Gets the mode of execution.
     */
    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Gets the parent job reference.
     */
    public SparkJobParent getParent() {
        return parent;
    }

    public void setParent(SparkJobParent parent) {
        this.parent = parent;
    }

    /**
     * Gets the resources required by this job.
     */
    public SparkJobResources getResources() {
        return resources;
    }

    public void setResources(SparkJobResources resources) {
        this.resources = resources;
    }

    /**
     * Gets the script to execute.
     */
    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
