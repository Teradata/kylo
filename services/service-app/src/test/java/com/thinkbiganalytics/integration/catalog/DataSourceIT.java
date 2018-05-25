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
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;

import org.hamcrest.CoreMatchers;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DataSourceIT extends IntegrationTestBase {

    @Value("${fs.s3a.access.key:#{null}}")
    String awsAccessKeyId;

    @Value("${fs.s3a.secret.key:#{null}}")
    String awsSecretAccessKey;

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

    /**
     * Verify listing files from the Amazon S3 connector.
     */
    @Test
    public void testListFilesS3() {
        Assume.assumeNotNull(awsAccessKeyId, awsSecretAccessKey);

        // Create an S3 data source
        final Connector connector = new Connector();
        connector.setId("amazon-s3");

        final DefaultDataSetTemplate template = new DefaultDataSetTemplate();
        template.setOptions(new HashMap<>());
        template.getOptions().put("spark.hadoop.fs.s3a.access.key", awsAccessKeyId);
        template.getOptions().put("spark.hadoop.fs.s3a.secret.key", awsSecretAccessKey);

        final DataSource request = new DataSource();
        request.setConnector(connector);
        request.setTemplate(template);
        request.setTitle("test list files s3");

        final DataSource dataSource = given(DataSourceController.BASE)
            .when().body(request).post()
            .then().statusCode(200)
            .extract().as(DataSource.class);

        // Test listing buckets
        final List<DataSetFile> buckets = given(DataSourceController.BASE)
            .when().pathParam("id", dataSource.getId()).queryParam("path", "s3a:/").get("{id}/files")
            .then().statusCode(200)
            .extract().as(DataSetFileList.class);
        Assert.assertThat(buckets, CoreMatchers.hasItem(new CustomMatcher<DataSetFile>("DataSetFile name=thinkbig.greg directory=true") {
            @Override
            public boolean matches(final Object item) {
                return (item instanceof DataSetFile)
                       && Objects.equals("thinkbig.greg", ((DataSetFile) item).getName())
                       && Objects.equals("s3a://thinkbig.greg/", ((DataSetFile) item).getPath())
                       && ((DataSetFile) item).isDirectory();
            }
        }));

        // Test listing files
        final List<DataSetFile> files = given(DataSourceController.BASE)
            .when().pathParam("id", dataSource.getId()).queryParam("path", "s3a://thinkbig.greg/").get("{id}/files")
            .then().statusCode(200)
            .extract().as(DataSetFileList.class);
        Assert.assertThat(files, CoreMatchers.hasItem(new CustomMatcher<DataSetFile>("DataSetFile name=userdata1.csv directory=false") {
            @Override
            public boolean matches(Object item) {
                return (item instanceof DataSetFile)
                       && Objects.equals("userdata1.csv", ((DataSetFile) item).getName())
                       && Objects.equals("s3a://thinkbig.greg/userdata1.csv", ((DataSetFile) item).getPath())
                       && !((DataSetFile) item).isDirectory();
            }
        }));
    }

    @Override
    protected void cleanup() {
        deleteExistingDatasources();
    }

    /**
     * File listing for browsing data sources.
     */
    @SuppressWarnings("serial")
    public static class DataSetFileList extends ArrayList<DataSetFile> {

    }

    /**
     * Search result containing data sources.
     */
    public static class DataSourceSearchResult extends SearchResultImpl<DataSource> {

    }
}
