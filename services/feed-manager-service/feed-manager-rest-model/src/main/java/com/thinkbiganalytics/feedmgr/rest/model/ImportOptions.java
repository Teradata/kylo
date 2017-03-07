package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.rest.model.LabelValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 */
public class ImportOptions {

    private String categorySystemName;
    private boolean createReusableFlow;
    private boolean overwrite;

    /**
     * Indicates any sensitive properties were set by the end user
     *
     */
    private List<ImportFeedProperty> properties;

    private IMPORT_CONNECTING_FLOW importConnectingFlow;

    /**
     * if true it will not throw the exception, but continue on the import path skipping the import
     * if false it will throw the exception unless the {@link this#overwrite} flag is set to true
     */
    private boolean continueIfExists;

    public String getCategorySystemName() {
        return categorySystemName;
    }

    public void setCategorySystemName(String categorySystemName) {
        this.categorySystemName = categorySystemName;
    }

    public boolean isCreateReusableFlow() {
        return createReusableFlow;
    }

    public void setCreateReusableFlow(boolean createReusableFlow) {
        this.createReusableFlow = createReusableFlow;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public IMPORT_CONNECTING_FLOW getImportConnectingFlow() {
        return importConnectingFlow;
    }

    public void setImportConnectingFlow(IMPORT_CONNECTING_FLOW importConnectingFlow) {
        this.importConnectingFlow = importConnectingFlow;
    }

    public List<ImportFeedProperty> getProperties() {
        if(properties == null){
            properties = new ArrayList<>();
        }
        return properties;
    }

    public void setProperties(List<ImportFeedProperty> properties) {
        this.properties = properties;
    }

    public enum IMPORT_CONNECTING_FLOW {
        YES, NO, NOT_SET
    }

    public boolean isContinueIfExists() {
        return continueIfExists;
    }

    public void setContinueIfExists(boolean continueIfExists) {
        this.continueIfExists = continueIfExists;
    }
}
