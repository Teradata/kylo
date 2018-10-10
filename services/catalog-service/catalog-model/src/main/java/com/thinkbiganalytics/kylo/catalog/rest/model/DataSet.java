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
import com.thinkbiganalytics.security.rest.model.EntityAccessControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Reference to a specific data set in a data source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("unused")
public class DataSet extends EntityAccessControl implements DataSetTemplate {

    private String id;
    private String title;
    private DataSource dataSource;
    private String format;
    private Map<String, String> options;
    private List<String> paths;
    
    public DataSet() {
    }

    public DataSet(DataSource ds, String title) {
        this.dataSource = ds;
    }

    public DataSet(@Nonnull final DataSet other) {
        dataSource = (other.dataSource != null) ? new DataSource(other.dataSource) : null;
        format = other.format;
        id = other.id;
        title = other.title;
        options = (other.options != null) ? new HashMap<>(other.options) : null;
        paths = (other.paths != null) ? new ArrayList<>(other.paths) : null;
    }

    /**
     * Parent data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    @SuppressWarnings("squid:S1168")
    public List<String> getFiles() {
        return null;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @SuppressWarnings("squid:S1161")
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Unique identifier
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    @SuppressWarnings("squid:S1168")
    public List<String> getJars() {
        return null;
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
        return "DataSet{id=" + id + ", dataSource=" + dataSource + '}';
    }
}
