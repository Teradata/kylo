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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TemplateMetadataWrapper {

    private String templateName;
    private String description;
    private String fileName;
    private String checksum;
    private boolean installed;
    private boolean stream;

    private boolean updateAvailable = false;
    private TemplateRepository repository;

    public TemplateMetadataWrapper(TemplateMetadata m){

        this.templateName = m.getTemplateName();
        this.description = m.getDescription();
        this.fileName = m.getFileName();
        this.checksum = m.getChecksum();
        this.stream = m.isStream();
        this.updateAvailable = m.isUpdateAvailable();
    }

    public TemplateRepository getRepository() {
        return repository;
    }

    public void setRepository(TemplateRepository repository) {
        this.repository = repository;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDescription() {
        return description;
    }

    public String getFileName() {
        return fileName;
    }

    public String getChecksum() {
        return checksum;
    }

    public boolean isInstalled() {
        return installed;
    }

    public boolean isStream() {
        return stream;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
}
