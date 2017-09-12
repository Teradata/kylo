package com.thinkbiganalytics.metadata.rest.model.jcr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class JcrIndexDefinition {

    private String indexName;
    private String indexKind;
    private String nodeType;
    private String description;
    private String propertyName;
    private int propertyType;

    private String propertyTypes;

    public JcrIndexDefinition(){
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public int getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(int propertyType) {
        this.propertyType = propertyType;
    }

    public String getIndexKind() {
        return indexKind;
    }

    public void setIndexKind(String indexKind) {
        this.indexKind = indexKind;
    }

    public String getDescription() {
        if(description == null){
            description = indexName+" description";
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPropertyTypes() {
        return propertyTypes;
    }

    public void setPropertyTypes(String propertyTypes) {
        this.propertyTypes = propertyTypes;
    }
}
