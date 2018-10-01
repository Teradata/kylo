package com.thinkbiganalytics.kylo.catalog.rest.model;

/*-
 * #%L
 * kylo-catalog-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thinkbiganalytics.security.rest.model.EntityAccessControl;

import javax.annotation.Nonnull;

/**
 * Connection details to a source of data sets.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("unused")
public class DataSource extends EntityAccessControl {

    /**
     * Parent connector
     */
    private Connector connector;

    /**
     * Unique identifier
     */
    private String id;

    /**
     * Reference to a NiFi controller service
     */
    private String nifiControllerServiceId;

    /**
     * Properties to apply to all data sets
     */
    @JsonDeserialize(as = DefaultDataSetTemplate.class)
    @JsonSerialize(as = DefaultDataSetTemplate.class)
    private DataSetTemplate template;

    /**
     * Display name of this connector
     */
    private String title;

    private DataSourceCredentials credentials;

    public DataSource() {
    }
    
    public DataSource(Connector conn, String title) {
        this.connector = conn;
        this.title = title;
    }

    public DataSource(@Nonnull final DataSource other) {
        connector = (other.connector != null) ? new Connector(other.connector) : null;
        id = other.id;
        nifiControllerServiceId = other.nifiControllerServiceId;
        template = (other.template != null) ? new DefaultDataSetTemplate(other.template) : null;
        title = other.title;
        credentials = (other.credentials != null) ? new DataSourceCredentials(other.credentials) : null;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNifiControllerServiceId() {
        return nifiControllerServiceId;
    }

    public void setNifiControllerServiceId(String nifiControllerServiceId) {
        this.nifiControllerServiceId = nifiControllerServiceId;
    }

    public DataSetTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DataSetTemplate template) {
        this.template = template;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DataSourceCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(DataSourceCredentials credentials) {
        this.credentials = credentials;
    }

    @JsonIgnore
    public boolean hasCredentials() {
        return this.credentials != null && this.credentials.getProperties() != null && !this.credentials.getProperties().isEmpty();
    }

    @Override
    public String toString() {
        return "DataSource{id=" + id + ", connector=" + connector + '}';
    }
}
