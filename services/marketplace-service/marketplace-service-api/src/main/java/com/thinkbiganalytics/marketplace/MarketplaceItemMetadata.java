package com.thinkbiganalytics.marketplace;

/*-
 * #%L
 * marketplace-service-api
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

public class MarketplaceItemMetadata {

    private String templateName;
    private String description;
    private String type;
    private String fileName;
    private boolean installed;

    public MarketplaceItemMetadata() {
    }

    public MarketplaceItemMetadata(String templateName, String fileName, String description, boolean installed){
        this.templateName = templateName;
        this.fileName = fileName;
        this.description = description;
        this.installed = installed;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "MarketplaceItemMetadata{" +
               "templateName='" + templateName + '\'' +
               ", description='" + description + '\'' +
               ", type='" + type + '\'' +
               ", fileName='" + fileName + '\'' +
               ", installed=" + installed +
               '}';
    }
}
