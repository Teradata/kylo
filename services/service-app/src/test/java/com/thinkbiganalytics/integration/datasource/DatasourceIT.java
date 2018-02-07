package com.thinkbiganalytics.integration.datasource;

/*-
 * #%L
 * kylo-service-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;

import org.junit.Assert;
import org.junit.Test;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Creates and updates a data source
 */
public class DatasourceIT extends IntegrationTestBase {

    @Test
    public void testCreateAndUpdateDatasource() throws Exception {
        JdbcDatasource[] initialDatasources = getDatasources();

        JdbcDatasource ds = new JdbcDatasource();
        ds.setName("ds");
        ds.setDescription("created by integration tests");
        ds.setDatabaseConnectionUrl("jdbc:mysql://localhost:3306/kylo");
        ds.setDatabaseDriverClassName("org.mariadb.jdbc.Driver");
        ds.setDatabaseDriverLocation("/opt/nifi/mysql/mariadb-java-client-1.5.7.jar");
        ds.setDatabaseUser("root");
        ds.setPassword("secret");
        ds.setType("mysql");

        JdbcDatasource response = createDatasource(ds);
        Assert.assertEquals(ds.getName(), response.getName());
        Assert.assertEquals(ds.getDescription(), response.getDescription());
        Assert.assertEquals(ds.getDatabaseConnectionUrl(), response.getDatabaseConnectionUrl());
        Assert.assertEquals(ds.getDatabaseDriverClassName(), response.getDatabaseDriverClassName());
        Assert.assertEquals(ds.getDatabaseDriverLocation(), response.getDatabaseDriverLocation());
        Assert.assertEquals(ds.getDatabaseUser(), response.getDatabaseUser());
        Assert.assertEquals(null, response.getPassword());
        Assert.assertEquals(null, response.getIcon());
        Assert.assertEquals(null, response.getIconColor());


        //assert new datasource was added
        JdbcDatasource[] currentDatasources = getDatasources();
        Assert.assertEquals(initialDatasources.length + 1, currentDatasources.length);

        ds = getDatasource(response.getId());
        ds.setName("ds with updated name");
        ds.setDescription("updated by integration tests");
        ds.setDatabaseConnectionUrl("jdbc:mysql://localhost:3306/kylo");
        ds.setIcon("stars");
        ds.setIconColor("green");

        JdbcDatasource updated = createDatasource(ds);
        Assert.assertEquals(ds.getName(), updated.getName());
        Assert.assertEquals(ds.getDescription(), updated.getDescription());
        Assert.assertEquals(ds.getDatabaseConnectionUrl(), updated.getDatabaseConnectionUrl());
        Assert.assertEquals(ds.getDatabaseDriverClassName(), updated.getDatabaseDriverClassName());
        Assert.assertEquals(ds.getDatabaseDriverLocation(), updated.getDatabaseDriverLocation());
        Assert.assertEquals(ds.getDatabaseUser(), updated.getDatabaseUser());
        Assert.assertEquals(null, updated.getPassword());
        Assert.assertEquals("stars", updated.getIcon());
        Assert.assertEquals("green", updated.getIconColor());

        //assert datasource was updated, rather than added
        currentDatasources = getDatasources();
        Assert.assertEquals(initialDatasources.length + 1, currentDatasources.length);

        //delete datasource
        deleteDatasource(ds.getId());
        currentDatasources = getDatasources();
        Assert.assertEquals(initialDatasources.length , currentDatasources.length);

        getDatasourceExpectingStatus(ds.getId(), HTTP_NOT_FOUND);
    }
}
