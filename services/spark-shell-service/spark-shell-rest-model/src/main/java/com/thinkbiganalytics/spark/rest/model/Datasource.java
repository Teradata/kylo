package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

import javax.annotation.Nonnull;

/**
 * A data source that is accessible from Spark.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
                  @JsonSubTypes.Type(UserDatasource.class),
                  @JsonSubTypes.Type(JdbcDatasource.class)
              })
public class Datasource implements com.thinkbiganalytics.metadata.datasource.Datasource, Serializable {

    private static final long serialVersionUID = -4566480656477287745L;

    private String id;
    private String name;
    private String description;

    /**
     * Constructs a {@code Datasource} with null values.
     */
    @SuppressWarnings("unused")
    public Datasource() {
    }

    /**
     * Constructs a {@code Datasource} by copying another data source.
     *
     * @param other the other data source
     */
    public Datasource(@Nonnull final com.thinkbiganalytics.metadata.datasource.Datasource other) {
        setId(other.getId());
        setName(other.getName());
        setDescription(other.getDescription());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
