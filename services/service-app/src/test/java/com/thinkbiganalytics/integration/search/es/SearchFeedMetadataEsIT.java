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
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.thinkbiganalytics.feedmgr.rest.controller.SearchRestController;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.ImportFeed;
import com.thinkbiganalytics.integration.KyloConfig;
import com.thinkbiganalytics.search.rest.model.FeedMetadataSearchResultData;
import com.thinkbiganalytics.search.rest.model.SearchResult;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Integration test for searching feed metadata indexed in Elasticsearch
 */

public class SearchFeedMetadataEsIT extends SearchEsIntegrationTestBase implements ISearchEsEntityMetadata {

    private static final Logger LOG = LoggerFactory.getLogger(SearchFeedMetadataEsIT.class);

    @Inject
    private KyloConfig kyloConfig;

    @BeforeClass
    public static void init() {
        resetStepNumbers();
    }

    @Test
    public void testSearchingFeedPropertiesEs() {
        String searchTerm;

        LOG.info("=== Starting Integration Test: " + this.getClass().getName());

        LOG.info(getStepNumber() + "Getting list of kylo indexes...");
        populateKyloIndexes();

        LOG.info(getStepNumber() + "Deleting existing kylo indexes to set up a clean env...");
        deleteEsIndexes();

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Creating kylo indexes to set up a clean env...");
        createEsIndexes();

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Verifying created kylo indexes...");
        verifyEsIndexes();

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Importing a sample feed...");
        ImportFeed importedFeed = importSampleFeed();
        String importedFeedId = importedFeed.getNifiFeed().getFeedMetadata().getFeedId();
        String categoryIdForImportedFeed = importedFeed.getNifiFeed().getFeedMetadata().getCategoryId();
        LOG.info("Category ID for Imported Feed: " + categoryIdForImportedFeed);

        waitForSomeTime(FIVE_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Verify feed is imported in Kylo ...");
        verifyFeedImport(importedFeedId);

        waitForSomeTime(TEN_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Executing full search on feed metadata and verifying index doc completeness ...");
        verifyIndexDocCompleteness(importedFeedId);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "Bb2000y";
        LOG.info(getStepNumber() + "Executing search 1 (user properties) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnUserProperties1(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "nothing-should-match";
        LOG.info(getStepNumber() + "Executing search 2 (user properties) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnUserProperties2(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "or30 organization=Corporate Z5000";
        LOG.info(getStepNumber() + "Executing search 3 (user properties) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnUserProperties3(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "or30 organization=Corporate";
        LOG.info(getStepNumber() + "Executing search 4 (user properties) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnUserProperties4(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "or3";
        LOG.info(getStepNumber() + "Executing search 5 (user properties) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnUserProperties5(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "Integration Test";
        LOG.info(getStepNumber() + "Executing search 1 (description) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnDescription1(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "global";
        LOG.info(getStepNumber() + "Executing search 1 (tags) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnTags1(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "Sample-Feed-For-Property-Search";
        LOG.info(getStepNumber() + "Executing search 1 (title i.e display name) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnTitle1(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "sample_feed_for_property_search";
        LOG.info(getStepNumber() + "Executing search 1 (system name) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnSystemName1(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        if (allowsIndexControl()) {

            LOG.info(getStepNumber() + "Disabling indexing of feed metadata ...");
            disableMetadataIndexingForFeedAndThenSearchAgain(importedFeedId);

            waitForSomeTime(TEN_SECONDS_IN_MILLIS);

            LOG.info(getStepNumber() + "Re-enabling indexing of feed metadata ...");
            enableMetadataIndexingForFeedAndThenSearchAgain(importedFeedId);

            waitForSomeTime(TEN_SECONDS_IN_MILLIS);

            LOG.info(getStepNumber() + "Executing full search on feed metadata (after disabling and re-enabling metadata indexing) and verifying index doc completeness ...");
            verifyIndexDocCompleteness(importedFeedId);

            waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        }

        searchTerm = "UPDATEDDESCRIPTION";
        LOG.info(getStepNumber() + "Executing search 2 (description) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnDescription2(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Editing feed description and saving feed ...");
        editFeedDescriptionAndSaveFeed(importedFeedId);

        waitForSomeTime(TEN_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Executing search 3 (description) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnDescription3(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Executing full search on feed metadata (after editing feed description) and verifying index doc completeness ...");
        verifyIndexDocCompleteness(importedFeedId);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);
        LOG.info(getStepNumber() + "Reverting edit of feed description and saving feed ...");
        revertFeedDescriptionAndSaveFeed(importedFeedId);

        waitForSomeTime(TEN_SECONDS_IN_MILLIS);

        searchTerm = "Integration Test";
        LOG.info(getStepNumber() + "Executing search 4 (description) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnDescription4(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);
        LOG.info(getStepNumber() + "Deleting feed with id: " + importedFeedId + " ...");
        deleteKyloFeed(importedFeedId);

        waitForSomeTime(TWENTY_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Deleting the category for the deleted feed (category has id: " + categoryIdForImportedFeed + ") ...");
        deleteKyloCategory(categoryIdForImportedFeed);

        waitForSomeTime(TEN_SECONDS_IN_MILLIS);

        verifySearchResultAfterDeletion(searchTerm);

        LOG.info("=== Finished Integration Test: " + this.getClass().getName());
    }

    private void verifySearchResultOnUserProperties1(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("1", searchResult.getTotalHits().toString());
        Assert.assertEquals("KYLO_FEEDS", searchResult.getSearchResults().get(0).getType().toString());
        Assert.assertEquals("pk10A common=Aa1000x\tpv20B utility=Bb2000y S99\tor30 organization=Corporate Z5000",
                            ((FeedMetadataSearchResultData) searchResult.getSearchResults().get(0)).getUserProperties());
        Assert.assertEquals(1, searchResult.getSearchResults().get(0).getHighlights().size());
        Assert.assertEquals("User properties", searchResult.getSearchResults().get(0).getHighlights().get(0).getKey());

        String expected = "<table style='border:1px solid black; border-collapse: collapse;'>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>name</font></td>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>value</font></td>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "pk10A common</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "Aa1000x</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "pv20B utility</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>Bb2000y</font> S99</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "or30 organization</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "Corporate Z5000</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "</table>";
        Assert.assertEquals(expected,
                            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());
    }

    private void verifySearchResultOnUserProperties2(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("0", searchResult.getTotalHits().toString());
    }

    private void verifySearchResultOnUserProperties3(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("1", searchResult.getTotalHits().toString());

        Assert.assertEquals("User properties", searchResult.getSearchResults().get(0).getHighlights().get(0).getKey());

        String expected = "<table style='border:1px solid black; border-collapse: collapse;'>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>name</font></td>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>value</font></td>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "pk10A common</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "Aa1000x</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>or30</font> <font style='font-weight:bold'>organization</font></td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>Corporate</font> <font style='font-weight:bold'>Z5000</font></td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "pv20B utility</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "Bb2000y S99</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "</table>";
        Assert.assertEquals(
            expected,
            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());
    }

    private void verifySearchResultOnUserProperties4(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("1", searchResult.getTotalHits().toString());

        Assert.assertEquals("User properties", searchResult.getSearchResults().get(0).getHighlights().get(0).getKey());

        String expected = "<table style='border:1px solid black; border-collapse: collapse;'>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>name</font></td>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>value</font></td>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "pk10A common</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "Aa1000x</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>or30</font> <font style='font-weight:bold'>organization</font></td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>Corporate</font> Z5000</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "pv20B utility</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "Bb2000y S99</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "</table>";
        Assert.assertEquals(
            expected,
            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());

    }

    private void verifySearchResultOnUserProperties5(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("0", searchResult.getTotalHits().toString());
    }

    private void verifySearchResultOnDescription1(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("1", searchResult.getTotalHits().toString());

        Assert.assertEquals("Description", searchResult.getSearchResults().get(0).getHighlights().get(0).getKey());
        Assert.assertEquals(
            "Sample feed for property search - <font style='font-weight:bold'>Integration</font> <font style='font-weight:bold'>Test</font>",
            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());
    }

    private void verifySearchResultOnDescription4(String term) {
        verifySearchResultOnDescription1(term);
    }

    private void verifySearchResultOnTags1(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("1", searchResult.getTotalHits().toString());

        Assert.assertEquals("Tags", searchResult.getSearchResults().get(0).getHighlights().get(0).getKey());
        Assert.assertEquals(
            "search <font style='font-weight:bold'>global</font> search properties",
            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());
    }

    private void verifySearchResultOnTitle1(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("1", searchResult.getTotalHits().toString());

        Assert.assertEquals("Description", searchResult.getSearchResults().get(0).getHighlights().get(0).getKey());
        Assert.assertEquals("Tags", searchResult.getSearchResults().get(0).getHighlights().get(1).getKey());
        Assert.assertEquals("Title", searchResult.getSearchResults().get(0).getHighlights().get(2).getKey());

        Assert.assertEquals(
            "<font style='font-weight:bold'>Sample</font> <font style='font-weight:bold'>feed</font> <font style='font-weight:bold'>for</font> <font style='font-weight:bold'>property</font> <font style='font-weight:bold'>search</font> - Integration Test",
            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());

        Assert.assertEquals(
            "<font style='font-weight:bold'>search</font> global <font style='font-weight:bold'>search</font> properties",
            searchResult.getSearchResults().get(0).getHighlights().get(1).getValue());

        Assert.assertEquals(
            "<font style='font-weight:bold'>Sample</font>-<font style='font-weight:bold'>Feed</font>-<font style='font-weight:bold'>For</font>-<font style='font-weight:bold'>Property</font>-<font style='font-weight:bold'>Search</font>",
            searchResult.getSearchResults().get(0).getHighlights().get(2).getValue());
    }

    private void verifySearchResultOnSystemName1(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("1", searchResult.getTotalHits().toString());

        Assert.assertEquals("System name (Kylo)", searchResult.getSearchResults().get(0).getHighlights().get(0).getKey());

        Assert.assertEquals(
            "<font style='font-weight:bold'>sample_feed_for_property_search</font>",
            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());
    }

    private void verifyIndexDocCompleteness(String feedId) {

        String docId = getDocIdForIndexedFeedMetadata(feedId);

        Response response = RestAssured.get(getBaseEsUrl() + FEEDS_INDEX_PREFIX + "-" + WORKSPACE + "/" + FEEDS_DOCTYPE + "/" + docId);

        response.then()
            .assertThat()
            .body(matchesJsonSchemaInClasspath(FEEDS_SEARCH_RESULT_SCHEMA_JSON_PATH));

        String responseJson = response.asString();
        JsonPath responseJsonPath = new JsonPath(responseJson);

        getIndexedFieldsWithJsonPathForEntity().forEach(field -> Assert.assertNotNull(responseJsonPath.get(field)));
    }

    private String getDocIdForIndexedFeedMetadata(String feedId) {
        Response response = RestAssured
            .given()
            .auth()
            .basic("dladmin", "thinkbig")
            .get(kyloConfig.getProtocol() + kyloConfig.getHost() + ":" + kyloConfig.getPort() + "/proxy/v1/metadata/debug/jcr?id=" + feedId);
        Assert.assertNotNull(response);
        String responseAsString = response.getBody().asString();

        final String RESPONSE_LINE_TERMINATION_CHARACTER = "\n";
        final String TBA_SUMMARY_NODE_IDENTIFIER = "tba:summary";
        final String TBA_SUMMARY_UUID_IDENTIFIER = "jcr:uuid=";

        int startLine = responseAsString.indexOf(TBA_SUMMARY_NODE_IDENTIFIER);
        int endLine = responseAsString.indexOf(RESPONSE_LINE_TERMINATION_CHARACTER, responseAsString.indexOf(TBA_SUMMARY_NODE_IDENTIFIER));

        int startUuid = responseAsString.indexOf(TBA_SUMMARY_UUID_IDENTIFIER, startLine);
        String tbaFeedSummaryNodeId = responseAsString.substring(startUuid + TBA_SUMMARY_UUID_IDENTIFIER.length(), endLine);
        LOG.info("Doc id of indexed feed metadata: {}", tbaFeedSummaryNodeId);
        return tbaFeedSummaryNodeId;
    }

    private void disableMetadataIndexingForFeedAndThenSearchAgain(String feedId) {
        Response response = getFeedById(feedId);
        FeedMetadata feedMetadata = response.as(FeedMetadata.class);
        Assert.assertTrue(feedMetadata.isAllowIndexing());
        feedMetadata.setAllowIndexing(false);
        createFeed(feedMetadata);

        String term = "Bb2000y";
        Response searchResponse = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        searchResponse.then().statusCode(HTTP_OK);
        SearchResult searchResult = searchResponse.as(SearchResult.class);
        Assert.assertEquals("0", searchResult.getTotalHits().toString());
    }

    private void enableMetadataIndexingForFeedAndThenSearchAgain(String feedId) {
        Response response = getFeedById(feedId);
        FeedMetadata feedMetadata = response.as(FeedMetadata.class);
        Assert.assertFalse(feedMetadata.isAllowIndexing());
        feedMetadata.setAllowIndexing(true);
        createFeed(feedMetadata);

        String term = "Bb2000y";
        Response searchReponse = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        searchReponse.then().statusCode(HTTP_OK);
        SearchResult searchResult = searchReponse.as(SearchResult.class);
        Assert.assertEquals("1", searchResult.getTotalHits().toString());
        Assert.assertEquals("KYLO_FEEDS", searchResult.getSearchResults().get(0).getType().toString());
        Assert.assertEquals("pk10A common=Aa1000x\tpv20B utility=Bb2000y S99\tor30 organization=Corporate Z5000",
                            ((FeedMetadataSearchResultData) searchResult.getSearchResults().get(0)).getUserProperties());
        Assert.assertEquals(1, searchResult.getSearchResults().get(0).getHighlights().size());
        Assert.assertEquals("User properties", searchResult.getSearchResults().get(0).getHighlights().get(0).getKey());

        String expected = "<table style='border:1px solid black; border-collapse: collapse;'>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>name</font></td>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>value</font></td>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "pk10A common</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "Aa1000x</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "pv20B utility</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>Bb2000y</font> S99</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "or30 organization</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "Corporate Z5000</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "</table>";
        Assert.assertEquals(expected,
                            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());
    }

    private void verifySearchResultOnDescription2(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("0", searchResult.getTotalHits().toString());
    }

    private void editFeedDescriptionAndSaveFeed(String feedId) {
        Response response = getFeedById(feedId);
        FeedMetadata feedMetadata = response.as(FeedMetadata.class);
        String currentFeedDescription = feedMetadata.getDescription();
        feedMetadata.setDescription(currentFeedDescription + " - UPDATEDDESCRIPTION");
        createFeed(feedMetadata);
    }

    private void revertFeedDescriptionAndSaveFeed(String feedId) {
        Response response = getFeedById(feedId);
        FeedMetadata feedMetadata = response.as(FeedMetadata.class);
        String currentFeedDescription = feedMetadata.getDescription();
        feedMetadata.setDescription(StringUtils.replace(currentFeedDescription, " - UPDATEDDESCRIPTION", ""));
        createFeed(feedMetadata);
    }

    private void verifySearchResultOnDescription3(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("1", searchResult.getTotalHits().toString());

        Assert.assertEquals("Description", searchResult.getSearchResults().get(0).getHighlights().get(0).getKey());
        Assert.assertEquals(
            "Sample feed for property search - Integration Test - <font style='font-weight:bold'>UPDATEDDESCRIPTION</font>",
            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());
    }

    private void verifySearchResultAfterDeletion(String term) {
        Response searchResponse = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);
        searchResponse.then().statusCode(HTTP_OK);
        SearchResult searchResult = searchResponse.as(SearchResult.class);
        Assert.assertEquals("0", searchResult.getTotalHits().toString());
    }

    public List<String> getIndexedFieldsWithJsonPathForEntity() {
        List<String> indexedFields = new ArrayList<>();
        indexedFields.add("_source.'jcr:description'");
        indexedFields.add("_source.'jcr:title'");
        indexedFields.add("_source.'length_jcr:description'");
        indexedFields.add("_source.'length_jcr:title'");
        indexedFields.add("_source.'length_tba:allowIndexing'");
        indexedFields.add("_source.'length_tba:systemName'");
        indexedFields.add("_source.'length_tba:tags'");
        indexedFields.add("_source.'length_usr:properties'");
        indexedFields.add("_source.'lowercase_jcr:description'");
        indexedFields.add("_source.'lowercase_jcr:title'");
        indexedFields.add("_source.'lowercase_tba:allowIndexing'");
        indexedFields.add("_source.'lowercase_tba:systemName'");
        indexedFields.add("_source.'lowercase_tba:tags'");
        indexedFields.add("_source.'lowercase_usr:properties'");
        indexedFields.add("_source.'tba:allowIndexing'");
        indexedFields.add("_source.'tba:systemName'");
        indexedFields.add("_source.'tba:tags'");
        indexedFields.add("_source.'uppercase_jcr:description'");
        indexedFields.add("_source.'uppercase_jcr:title'");
        indexedFields.add("_source.'uppercase_tba:allowIndexing'");
        indexedFields.add("_source.'uppercase_tba:systemName'");
        indexedFields.add("_source.'uppercase_tba:tags'");
        indexedFields.add("_source.'uppercase_usr:properties'");
        indexedFields.add("_source.'usr:properties'");
        return indexedFields;
    }

    public boolean allowsIndexControl() {
        return true;
    }
}
