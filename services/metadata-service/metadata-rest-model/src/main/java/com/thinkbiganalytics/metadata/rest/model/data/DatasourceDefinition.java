package com.thinkbiganalytics.metadata.rest.model.data;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceDefinition {

    private String processorType;
    private ConnectionType connectionType;
    private Set<String> datasourcePropertyKeys;
    private String datasourceType;
    private String identityString;
    private String description;
    private String title;

    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public Set<String> getDatasourcePropertyKeys() {

        if (datasourcePropertyKeys == null) {
            datasourcePropertyKeys = new HashSet<>();
        }
        return datasourcePropertyKeys;
    }

    public void setDatasourcePropertyKeys(Set<String> datasourcePropertyKeys) {
        this.datasourcePropertyKeys = datasourcePropertyKeys;
    }

    public String getDatasourceType() {
        if (StringUtils.isBlank(datasourceType)) {
            datasourceType = "Datasource";
        }
        return datasourceType;
    }

    public void setDatasourceType(String datasourceType) {
        this.datasourceType = datasourceType;
    }

    public String getIdentityString() {
        if (StringUtils.isBlank(identityString)) {
            identityString = getDatasourcePropertyKeys().stream().map(key -> "${" + key + "}").collect(Collectors.joining(","));
        }
        return identityString;
    }

    public void setIdentityString(String identityString) {
        this.identityString = identityString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return StringUtils.isBlank(title) ? getIdentityString() : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public enum ConnectionType {
        SOURCE, DESTINATION;
    }
}
