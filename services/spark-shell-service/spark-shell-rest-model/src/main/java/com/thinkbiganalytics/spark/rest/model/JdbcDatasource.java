package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
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

import javax.annotation.Nonnull;

/**
 * A JDBC data source that is accessible from Spark.
 */
public class JdbcDatasource extends UserDatasource implements com.thinkbiganalytics.metadata.datasource.JdbcDatasource {

    private static final long serialVersionUID = 1778772264428622884L;

    private String controllerServiceId;
    private String databaseConnectionUrl;
    private String databaseDriverClassName;
    private String databaseDriverLocation;
    private String databaseUser;
    private String password;

    /**
     * Constructs a {@code JdbcDatasource} with null values.
     */
    @SuppressWarnings("unused")
    public JdbcDatasource() {
    }

    /**
     * Constructs a {@code JdbcDatasource} by copying another data source.
     *
     * @param other the other data source
     */
    public JdbcDatasource(@Nonnull final com.thinkbiganalytics.metadata.datasource.JdbcDatasource other) {
        super(other);
        setControllerServiceId(other.getControllerServiceId());
        setDatabaseConnectionUrl(other.getDatabaseConnectionUrl());
        setDatabaseDriverClassName(other.getDatabaseDriverClassName());
        setDatabaseDriverLocation(other.getDatabaseDriverLocation());
        setDatabaseUser(other.getDatabaseUser());
        setPassword(other.getPassword());
    }

    @Override
    public String getControllerServiceId() {
        return controllerServiceId;
    }

    @Override
    public void setControllerServiceId(String controllerServiceId) {
        this.controllerServiceId = controllerServiceId;
    }

    @Override
    public String getDatabaseConnectionUrl() {
        return databaseConnectionUrl;
    }

    @Override
    public void setDatabaseConnectionUrl(String databaseConnectionUrl) {
        this.databaseConnectionUrl = databaseConnectionUrl;
    }

    @Override
    public String getDatabaseDriverClassName() {
        return databaseDriverClassName;
    }

    @Override
    public void setDatabaseDriverClassName(String databaseDriverClassName) {
        this.databaseDriverClassName = databaseDriverClassName;
    }

    @Override
    public String getDatabaseDriverLocation() {
        return databaseDriverLocation;
    }

    @Override
    public void setDatabaseDriverLocation(String databaseDriverLocation) {
        this.databaseDriverLocation = databaseDriverLocation;
    }

    @Override
    public String getDatabaseUser() {
        return databaseUser;
    }

    @Override
    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }
}
