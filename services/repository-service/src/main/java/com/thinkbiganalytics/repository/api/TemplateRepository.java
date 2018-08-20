package com.thinkbiganalytics.repository.api;

/*-
 * #%L
 * kylo-repository-service
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import org.hibernate.validator.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateRepository {

    @NotEmpty
    private String name;
    @NotEmpty
    private String location;
    @NotEmpty
    private RepositoryType type;
    private String icon;
    private boolean readOnly = false;

    public TemplateRepository(String name, String location, RepositoryType type) {
        this.name = name;
        this.location = location;
        this.type = type;
    }

    @JsonCreator
    public TemplateRepository(@JsonProperty("name") String name,
                              @JsonProperty("location") String location,
                              @JsonProperty("icon") String icon,
                              @JsonProperty("type") RepositoryType type,
                              @JsonProperty("readOnly") boolean readOnly) {
        this.name = name;
        this.location = location;
        this.icon = icon;
        this.type = type;
        this.readOnly = readOnly;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public RepositoryType getType() {
        return type;
    }

    public void setType(RepositoryType type) {
        this.type = type;
    }

    public static enum RepositoryType {
        FILESYSTEM("FileSystem");

        private String key;

        RepositoryType(String key){
            this.key = key;
        }

        @JsonValue
        public String getKey() { return key; }
    }
}
