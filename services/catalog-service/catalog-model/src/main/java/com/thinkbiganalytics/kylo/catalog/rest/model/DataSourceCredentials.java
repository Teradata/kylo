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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Credential properties that may be injected into a data source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("unused")
public class DataSourceCredentials {

    private boolean encrypted;
    private Map<String, String> properties;

    public DataSourceCredentials() {
        this.properties = new HashMap<>();
    }

    public DataSourceCredentials(@Nonnull final Map<String, String> creds, final boolean encrypted) {
        this.encrypted = encrypted;
        this.properties = new HashMap<>(creds);
    }
    
    public DataSourceCredentials(@Nonnull final DataSourceCredentials other) {
        this.encrypted = other.encrypted;
        this.properties = new HashMap<>(other.properties);
    }
    
    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> credentials) {
        this.properties = credentials;
    }

    @Override
    public String toString() {
        return "DataSourceCredentials{encrypted=" + this.encrypted + ", creds=" + this.properties + '}';
    }
}
