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

import javax.annotation.Nonnull;

/**
 * Reference to a specific data set in a data source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSet extends DataSetTemplate {

    /**
     * Parent data source
     */
    private DataSource dataSource;

    /**
     * Unique identifier
     */
    private String id;

    public DataSet() {
    }

    public DataSet(@Nonnull final DataSet other) {
        super(other);
        dataSource = (other.dataSource != null) ? new DataSource(other.dataSource) : null;
        id = other.id;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "DataSet{id=" + id + ", dataSource=" + dataSource + '}';
    }
}
