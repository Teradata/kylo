package com.thinkbiganalytics.metadata.datasource;

/*-
 * #%L
 * Kylo Metadata Service API
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

/**
 * Defines a connection to a JDBC data source.
 */
public interface JdbcDatasource extends UserDatasource {

    /**
     * Gets the id of the NiFi DBCPConnectionPool controller service for this data source.
     *
     * @return the controller service id
     */
    String getControllerServiceId();

    /**
     * Sets the id of the NiFi DBCPConnectionPool controller service for this data source.
     *
     * @param controllerServiceId the controller service id
     */
    void setControllerServiceId(String controllerServiceId);

    /**
     * Gets the database connection URL.
     *
     * @return the database connection URL
     */
    String getDatabaseConnectionUrl();

    /**
     * Sets the database connection URL. Should be in the form jdbc:<i>subprotocol</i>:<i>subname</i>.
     *
     * @param databaseConnectionUrl the database connection URL
     */
    void setDatabaseConnectionUrl(String databaseConnectionUrl);

    /**
     * Gets the database driver class name.
     *
     * @return the database driver class name
     */
    String getDatabaseDriverClassName();

    /**
     * Sets the database driver class name.
     *
     * @param databaseDriverClassName the database driver class name
     */
    void setDatabaseDriverClassName(String databaseDriverClassName);

    /**
     * Gets a comma-separated list of files/folders and or URLs containing the driver JAR and its dependencies (if any).
     *
     * @return the database driver location(s)
     */
    String getDatabaseDriverLocation();

    /**
     * Sets the comma-separated list of files/folders and or URLs containing the driver JAR and its dependencies (if any).
     *
     * @param databaseDriverLocation the database driver location(s)
     */
    void setDatabaseDriverLocation(String databaseDriverLocation);

    /**
     * Gets the database user name.
     *
     * @return the database user
     */
    String getDatabaseUser();

    /**
     * Sets the database user name.
     *
     * @param databaseUser the database user
     */
    void setDatabaseUser(String databaseUser);

    /**
     * Gets the password to use when connecting to this data source.
     *
     * @return the password
     */
    String getPassword();

    /**
     * Sets the password to use when connecting to this data source.
     *
     * @param password the password
     */
    void setPassword(String password);
}
