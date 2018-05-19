package com.thinkbiganalytics.kylo.catalog.rest.model;

/*-
 * #%L
 * kylo-catalog-model
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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
import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Template for creating a new data set
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSetTemplate {

    /**
     * Files to be placed in the working directory of each executor
     */
    private List<String> files;

    /**
     * Short name or class name of the Spark data source used by this connector
     */
    private String format;

    /**
     * Jar files to include on the driver and executor classpaths
     */
    private List<String> jars;

    /**
     * Input or output options for the data source
     */
    private Map<String, String> options;

    /**
     * File paths (and URIs) for input data
     */
    private List<String> paths;

    public DataSetTemplate() {
    }

    public DataSetTemplate(@Nonnull final DataSetTemplate other) {
        files = (other.files != null) ? new ArrayList<>(other.files) : null;
        format = other.format;
        jars = (other.jars != null) ? new ArrayList<>(other.jars) : null;
        options = (other.options != null) ? new HashMap<>(other.options) : null;
        paths = (other.paths != null) ? new ArrayList<>(other.paths) : null;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getJars() {
        return jars;
    }

    public void setJars(List<String> jars) {
        this.jars = jars;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("files", files)
            .add("format", format)
            .add("jars", jars)
            .add("options", options)
            .add("paths", paths)
            .omitNullValues()
            .toString();
    }
}
