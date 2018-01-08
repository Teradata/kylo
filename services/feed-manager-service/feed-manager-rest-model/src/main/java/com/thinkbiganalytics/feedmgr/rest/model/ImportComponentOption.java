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
import java.util.HashSet;
import java.util.List;


public class ImportComponentOption {

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


    public ImportComponentOption() {
    }

    public ImportComponentOption(ImportComponent importComponent, boolean shouldImport) {
        this.importComponent = importComponent;
        this.shouldImport = shouldImport;
    }

    public ImportComponentOption(ImportComponent importComponent, boolean overwrite, boolean shouldImport) {
        this.importComponent = importComponent;
        this.overwrite = overwrite;
        this.shouldImport = shouldImport;
    }

    public ImportComponent getImportComponent() {
        return importComponent;
    }

    public void setImportComponent(ImportComponent importComponent) {
        this.importComponent = importComponent;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isShouldImport() {
        return shouldImport;
    }

    public void setShouldImport(boolean shouldImport) {
        this.shouldImport = shouldImport;
    }

    public boolean isContinueIfExists() {
        return continueIfExists;
    }

    public void setContinueIfExists(boolean continueIfExists) {
        this.continueIfExists = continueIfExists;
    }

    public List<ImportProperty> getProperties() {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        return properties;
    }

    public void setProperties(List<ImportProperty> properties) {
        this.properties = properties;
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    public boolean isUserAcknowledged() {
        return userAcknowledged;
    }

    public void setUserAcknowledged(boolean userAcknowledged) {
        this.userAcknowledged = userAcknowledged;
    }


    public List<String> getErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<>();
        }
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public boolean hasErrorMessages() {
        return getErrorMessages().size() > 0;
    }

    public boolean isValidForImport() {
        return validForImport;
    }

    public void setValidForImport(boolean validForImport) {
        this.validForImport = validForImport;
    }

    public List<ReusableTemplateConnectionInfo> getConnectionInfo() {
        if(connectionInfo == null){
            connectionInfo = new ArrayList<>();
        }
        return connectionInfo;
    }

    private boolean hasReusableConnection(ReusableTemplateConnectionInfo connection){
       return getConnectionInfo().stream().anyMatch(conn -> conn.getFeedOutputPortName().equalsIgnoreCase(connection.getFeedOutputPortName()));
    }

    public void addConnectionInfo(List<ReusableTemplateConnectionInfo> connectionInfo) {
        if(connectionInfo != null){
            connectionInfo.stream().filter(connectionInfo1 -> !hasReusableConnection(connectionInfo1)).forEach(connectionInfo1 -> getConnectionInfo().add(connectionInfo1));
        }
    }
    public void setConnectionInfo(List<ReusableTemplateConnectionInfo> connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}
