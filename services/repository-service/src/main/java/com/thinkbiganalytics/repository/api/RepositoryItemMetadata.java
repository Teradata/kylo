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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryItemMetadata {

    private String templateName;
    private String description;
    private String fileName;
    private boolean stream;
    private boolean installed;

    public RepositoryItemMetadata() {
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isStream() {
        return stream;
    }

    @Override
    public String toString() {
        return "RepositoryItemMetadata{" +
               "templateName='" + templateName + '\'' +
               ", description='" + description + '\'' +
               ", fileName='" + fileName + '\'' +
               ", stream=" + stream +
               '}';
    }

    public RepositoryItemMetadata(String templateName, String description, String fileName, boolean stream) {
        this.templateName = templateName;
        this.description = description;
        this.fileName = fileName;
        this.stream = stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
