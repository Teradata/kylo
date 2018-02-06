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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;

import java.util.ArrayList;
import java.util.List;

public class ImportComponentOptionBuilder {


    /**
     * The type of component to import
     */
    private ImportComponent importComponent;


    /**
     * Should we overwrite the component with the import contents if it already exists
     */
    private boolean overwrite;

    /**
     * is this option a candidate for importing
     */
    private boolean shouldImport;

    /**
     * indicates if the system analyzed the component (i.e. processed it and its sensitive properties
     */
    private boolean analyzed;

    /**
     * indicates the user accepted the values of this option
     */
    private boolean userAcknowledged;

    /**
     * user supplied properties to replace/use for the component
     */
    private List<ImportProperty> properties;

    /**
     * If we encounter an existing component, and we don't specify to overwrite it, should we continue on
     */
    private boolean continueIfExists;

    /**
     * list of errors
     */
    private List<String> errorMessages;

    /**
     * connection information describing the output port to input port mapping
     */
    private List<ReusableTemplateConnectionInfo> connectionInfo;


    /**
     * true will import this item
     */
    @JsonIgnore
    private boolean validForImport;

    public ImportComponentOptionBuilder(ImportComponent importComponent) {
        this.importComponent = importComponent;
    }

    public ImportComponentOptionBuilder importComponent(ImportComponent importComponent){
        this.importComponent = importComponent;
        return this;
    }

    public ImportComponentOptionBuilder overwrite(boolean overwrite){
        this.overwrite = overwrite;
        return this;
    }

    public ImportComponentOptionBuilder shouldImport(boolean shouldImport){
        this.shouldImport = shouldImport;
        return this;
    }


    public ImportComponentOptionBuilder analyzed(boolean analyzed){
        this.analyzed = analyzed;
        return this;
    }

    public ImportComponentOptionBuilder userAcknowledged(boolean userAcknowledged){
        this.userAcknowledged = userAcknowledged;
        return this;
    }

    public ImportComponentOptionBuilder importProperty(String processorName, String processorId,String processorType, String propertyKey, String propertyValue){
        ImportProperty importProperty = new ImportProperty(processorName,processorId,propertyKey,propertyValue,processorType);
       if(properties == null){
           properties = new ArrayList<>();
       }
       properties.add(importProperty);
       return this;
    }

    public ImportComponentOptionBuilder continueIfExists(boolean continueIfExists){
        this.continueIfExists = continueIfExists;
        return this;
    }


    public ImportComponentOptionBuilder connectionInfo(ReusableTemplateConnectionInfo reusableTemplateConnectionInfo){
        if(connectionInfo == null){
            connectionInfo = new ArrayList<>();
        }
        connectionInfo.add(reusableTemplateConnectionInfo);
        return this;
    }

    public ImportComponentOptionBuilder alwaysImport() {
        shouldImport(true);
        analyzed(true);
        userAcknowledged(true);
        continueIfExists(true);
        overwrite(true);
        return this;
    }


    public ImportComponentOption build(){
        ImportComponentOption importComponentOption = new ImportComponentOption();
        importComponentOption.setImportComponent(importComponent);
        importComponentOption.setShouldImport(shouldImport);
        importComponentOption.setAnalyzed(analyzed);
        importComponentOption.setConnectionInfo(connectionInfo);
        importComponentOption.setUserAcknowledged(userAcknowledged);
        importComponentOption.setContinueIfExists(continueIfExists);
        importComponentOption.setOverwrite(overwrite);
        return importComponentOption;
    }

}
