package com.thinkbiganalytics.metadata.rest.model.data;

/*-
 * #%L
 * kylo-metadata-rest-model
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

/**
 * Defines a connection to a JDBC data source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JdbcDatasource extends UserDatasource {

    /**
     * Id of the NiFi DBCPConnectionPool controller service
     */
    private String controllerServiceId;

    /**
     * A database URL of the form jdbc:<i>subprotocol:subname</i>
     */
    private String databaseConnectionUrl;

    /**
     * Database driver class name
     */
    private String databaseDriverClassName;

    /**
     * Comma-separated list of files/folders and/or URLs containing the driver JAR and its dependencies (if any)
     */
    private String databaseDriverLocation;

    /**
     * Database user name
     */
    private String databaseUser;

    /**
     * Password to use when connecting to this data source
     */
    private String password;

    public String getControllerServiceId() {
        return controllerServiceId;
    }

    public void setControllerServiceId(String controllerServiceId) {
        this.controllerServiceId = controllerServiceId;
    }

    public String getDatabaseConnectionUrl() {
        return databaseConnectionUrl;
    }

    public void setDatabaseConnectionUrl(String databaseConnectionUrl) {
        this.databaseConnectionUrl = databaseConnectionUrl;
    }

    public String getDatabaseDriverClassName() {
        return databaseDriverClassName;
    }

    public void setDatabaseDriverClassName(String databaseDriverClassName) {
        this.databaseDriverClassName = databaseDriverClassName;
    }

    public String getDatabaseDriverLocation() {
        return databaseDriverLocation;
    }

    public void setDatabaseDriverLocation(String databaseDriverLocation) {
        this.databaseDriverLocation = databaseDriverLocation;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
