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
public class DefaultDataSetTemplate implements DataSetTemplate {

    private List<String> files;
    private String format;
    private List<String> jars;
    private Map<String, String> options;
    private List<String> paths;

    public DefaultDataSetTemplate() {
    }

    public DefaultDataSetTemplate(@Nonnull final DataSetTemplate other) {
        files = (other.getFiles() != null) ? new ArrayList<>(other.getFiles()) : null;
        format = other.getFormat();
        jars = (other.getJars() != null) ? new ArrayList<>(other.getJars()) : null;
        options = (other.getOptions() != null) ? new HashMap<>(other.getOptions()) : null;
        paths = (other.getPaths() != null) ? new ArrayList<>(other.getPaths()) : null;
    }

    @Override
    public List<String> getFiles() {
        return files;
    }

    @SuppressWarnings("squid:S1161")
    public void setFiles(List<String> files) {
        this.files = files;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @SuppressWarnings("squid:S1161")
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public List<String> getJars() {
        return jars;
    }

    @SuppressWarnings("squid:S1161")
    public void setJars(List<String> jars) {
        this.jars = jars;
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @SuppressWarnings("squid:S1161")
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public List<String> getPaths() {
        return paths;
    }

    @SuppressWarnings("squid:S1161")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultDataSetTemplate)) {
            return false;
        }

        DefaultDataSetTemplate that = (DefaultDataSetTemplate) o;

        if (files != null ? !files.equals(that.files) : that.files != null) {
            return false;
        }
        if (format != null ? !format.equals(that.format) : that.format != null) {
            return false;
        }
        if (jars != null ? !jars.equals(that.jars) : that.jars != null) {
            return false;
        }
        if (options != null ? !options.equals(that.options) : that.options != null) {
            return false;
        }
        if (paths != null ? !paths.equals(that.paths) : that.paths != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = files != null ? files.hashCode() : 0;
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (jars != null ? jars.hashCode() : 0);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        result = 31 * result + (paths != null ? paths.hashCode() : 0);
        return result;
    }
}
