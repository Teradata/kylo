package com.thinkbiganalytics.integration.search.es;

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

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.ImportFeed;
import com.thinkbiganalytics.integration.EsRestConfig;
import com.thinkbiganalytics.integration.IntegrationTestBase;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Integration test base class for searching Elasticsearch
 */
public class SearchEsIntegrationTestBase extends IntegrationTestBase {

    protected static final String
        INDEX_LIST_AS_COMMA_SEP_STRING =
        "kylo-data,kylo-datasources,kylo-categories-metadata,kylo-categories-default,kylo-feeds-metadata,kylo-feeds-default,kylo-internal-fd1-default,kylo-internal-fd1-metadata";
    protected static final String DELETE_KYLO_INDEXES_SCRIPT = "/opt/kylo/bin/delete-kylo-indexes-es.sh";
    protected static final String CREATE_KYLO_INDEXES_SCRIPT = "/opt/kylo/bin/create-kylo-indexes-es.sh";
    protected static final String NUM_SHARDS = "1";
    protected static final String NUM_REPLICAS = "1";
    protected static final List<String> kyloIndexList = new ArrayList<>();
    protected static final String WORKSPACE = "default";
    protected static final String CATEGORIES_DOCTYPE = "default";
    protected static final String CATEGORIES_INDEX_PREFIX = "kylo-categories";
    protected static final String CATEGORIES_SEARCH_RESULT_SCHEMA_JSON_PATH = "com/thinkbiganalytics/integration/search/category-search-result-schema.json";

    protected static final String FEEDS_DOCTYPE = "default";
    protected static final String FEEDS_INDEX_PREFIX = "kylo-feeds";
    protected static final String FEEDS_SEARCH_RESULT_SCHEMA_JSON_PATH = "com/thinkbiganalytics/integration/search/feed-search-result-schema.json";
    protected static final String FEEDS_SEARCH_SAMPLE_FEED_PATH = "com/thinkbiganalytics/integration/search/sample_feed_for_property_search.feed.zip";

    protected static final int CATEGORIES_USER_PROPERTIES_COUNT = 2;
    protected static final int CATEGORIES_INDEXED_FIELD_COUNT = 36;
    protected static final String CATEGORY_VALID_PART_OF_USER_PROPERTY_VALUE = "ad92";

    protected static final long FOUR_SECONDS_IN_MILLIS = 4000;
    protected static final long FIVE_SECONDS_IN_MILLIS = 5000;
    protected static final long TEN_SECONDS_IN_MILLIS = 10000;
    protected static final long TWENTY_SECONDS_IN_MILLIS = 20000;


    protected static int stepNumber;

    @Inject
    private EsRestConfig esRestConfig;

    private static final Logger LOG = LoggerFactory.getLogger(SearchEsIntegrationTestBase.class);

    protected boolean populateKyloIndexes() {
        try {
            kyloIndexList.addAll(Arrays.asList(StringUtils.split(INDEX_LIST_AS_COMMA_SEP_STRING, ",")));
            return true;
        } catch (Exception e) {
            LOG.warn("Unable to populate list of Kylo indexes", e);
            return false;
        }
    }

    protected boolean deleteEsIndexes() {
        try {
            runCommandOnRemoteSystem(DELETE_KYLO_INDEXES_SCRIPT
                                     + " "
                                     + esRestConfig.getEsHost()
                                     + " "
                                     + esRestConfig.getEsRestPort(), IntegrationTestBase.APP_KYLO_SERVICES
            );
            return true;
        } catch (Exception e) {
            LOG.warn("Unable to delete ES indexes to start with a clean environment", e);
            return false;
        }
    }

    protected boolean createEsIndexes() {
        try {
            runCommandOnRemoteSystem(CREATE_KYLO_INDEXES_SCRIPT
                                     + " "
                                     + esRestConfig.getEsHost()
                                     + " "
                                     + esRestConfig.getEsRestPort()
                                     + " "
                                     + NUM_SHARDS
                                     + " "
                                     + NUM_REPLICAS, IntegrationTestBase.APP_KYLO_SERVICES);
            return true;
        } catch (Exception e) {
            LOG.warn("Unable to create ES indexes to start with a clean environment", e);
            return false;
        }
    }

    public boolean verifyEsIndexes() {
        try {
            kyloIndexList.forEach(this::verifyElasticSearchIndex);
            return true;
        } catch (Exception e) {
            LOG.warn("Unable to verify ES indexes to start with a clean environment", e);
            return false;
        }
    }

    private void verifyElasticSearchIndex(String index) {
        RestAssured.when()
            .get(getBaseEsUrl() + index)
            .then()
            .statusCode(HTTP_OK);

        LOG.debug("ES Index '{}' is available", new Object[]{index});
    }

    public String getBaseEsUrl() {
        return ("http://"
                + esRestConfig.getEsHost()
                + ":" + esRestConfig.getEsRestPort()
                + "/");
    }

    protected FeedCategory createKyloCategoryForSearch(String name, String description, boolean includeUserProperties) {
        DateTime currentTime = DateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");

        Set<UserProperty> userProperties = new HashSet<>();

        if (includeUserProperties) {
            UserProperty property1 = new UserProperty();
            property1.setSystemName("test type");
            property1.setValue("T100 ad92");
            userProperties.add(property1);

            UserProperty property2 = new UserProperty();
            property2.setSystemName("run mode");
            property2.setValue("AUTO i56 daily");
            userProperties.add(property2);
        }

        return createCategory(name,
                              description + " - " + dateTimeFormatter.print(currentTime),
                              true,
                              userProperties);
    }

    protected ImportFeed importSampleFeed() {
        String pathToKyloFeedZip = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(FEEDS_SEARCH_SAMPLE_FEED_PATH)).getPath();
        ImportFeed sampleImportFeed = importFeed(pathToKyloFeedZip);
        LOG.info("Imported Feed ID: {}", sampleImportFeed.getNifiFeed().getFeedMetadata().getFeedId());
        LOG.info("Imported Category.Feed Display Name: {}", sampleImportFeed.getNifiFeed().getFeedMetadata().getCategoryAndFeedDisplayName());
        return sampleImportFeed;
    }

    protected void verifyFeedImport(String feedId) {
        Response response = getFeedById(feedId);
        response.then().statusCode(HTTP_OK);
    }

    protected void waitForSomeTime(long periodInMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(periodInMillis);
        } catch (InterruptedException e) {
        }
    }

    protected String getStepNumber() {
        return ++stepNumber + ": ";
    }

    protected static void resetStepNumbers() {
        stepNumber = 0;
    }

    protected void deleteKyloCategory(String categoryId) {
        deleteCategory(categoryId);
    }

    protected void deleteKyloFeed(String feedId) {
        deleteFeed(feedId);
    }
}
