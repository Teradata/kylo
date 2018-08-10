package com.thinkbiganalytics.spark.rest.model.job;

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

public class SparkJobRequest {

    public enum Mode {
        BATCH,
        INTERACTIVE
    }

    private String lang;
    private Mode mode;
    private SparkJobParent parent;
    private SparkJobResources resources;
    private String script;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public SparkJobParent getParent() {
        return parent;
    }

    public void setParent(SparkJobParent parent) {
        this.parent = parent;
    }

    public SparkJobResources getResources() {
        return resources;
    }

    public void setResources(SparkJobResources resources) {
        this.resources = resources;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
