package com.thinkbiganalytics.integration.catalog;

/*-
 * #%L
 * kylo-service-app
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

import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.kylo.catalog.rest.controller.DataSourceController;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;

import org.hamcrest.CoreMatchers;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

public class DataSourceIT extends IntegrationTestBase {

    /**
     * Verify retrieving a single data source.
     */
    @Test
    public void testFind() {
        final DataSource dataSource = given(DataSourceController.BASE)
            .when().get("hive")
            .then().statusCode(200)
            .extract().as(DataSource.class);
        Assert.assertEquals("hive", dataSource.getId());
        Assert.assertEquals("Hive", dataSource.getTitle());
        Assert.assertEquals("hive", dataSource.getConnector().getId());
    }

    /**
     * Verifying retrieving all data sources.
     */
    @Test
    public void testFindAll() {
        // Create a feed data source
        final JdbcDatasource jdbcDatasourceRequest = new JdbcDatasource();
        jdbcDatasourceRequest.setName("My Test SQL");
        jdbcDatasourceRequest.setDatabaseConnectionUrl("jdbc:mysql://localhost:3306/kylo");
        jdbcDatasourceRequest.setDatabaseDriverClassName("org.mariadb.jdbc.Driver");
        jdbcDatasourceRequest.setDatabaseUser("root");
        jdbcDatasourceRequest.setPassword("secret");
        jdbcDatasourceRequest.setType("mysql");
        final JdbcDatasource jdbcDatasource = createDatasource(jdbcDatasourceRequest);

        // Find all data sources
        final SearchResult<DataSource> searchResult = given(DataSourceController.BASE)
            .when().get()
            .then().statusCode(200)
            .extract().as(DataSourceSearchResult.class);

        final Matcher<DataSource> isHive = new CustomMatcher<DataSource>("is hive data source") {
            @Override
            public boolean matches(final Object item) {
                return (item instanceof DataSource && "hive".equals(((DataSource) item).getId()) && "Hive".equals(((DataSource) item).getTitle()));
            }
        };
        final Matcher<DataSource> isJdbc = new CustomMatcher<DataSource>("is jdbc data source") {
            @Override
            public boolean matches(final Object item) {
                final DataSource dataSource = (item instanceof DataSource) ? (DataSource) item : null;
                return (dataSource != null && jdbcDatasource.getId().equals(dataSource.getId()) && jdbcDatasource.getName().equals(dataSource.getTitle()));
            }
        };
        Assert.assertThat(searchResult.getData(), CoreMatchers.hasItem(isHive));
        Assert.assertThat(searchResult.getData(), CoreMatchers.hasItem(isJdbc));
        Assert.assertEquals(searchResult.getData().size(), searchResult.getRecordsTotal().longValue());
    }

    @Override
    protected void cleanup() {
        deleteExistingDatasources();
    }
}

/**
 * Search result containing data sources.
 */
class DataSourceSearchResult extends SearchResultImpl<DataSource> {

}
