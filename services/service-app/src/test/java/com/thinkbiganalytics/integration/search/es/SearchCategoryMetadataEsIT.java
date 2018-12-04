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
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.search.rest.model.CategoryMetadataSearchResultData;
import com.thinkbiganalytics.search.rest.model.SearchResult;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.net.HttpURLConnection.HTTP_OK;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Integration test for searching category metadata indexed in Elasticsearch
 */

public class SearchCategoryMetadataEsIT extends SearchEsIntegrationTestBase implements ISearchEsEntityMetadata {

    private static final Logger LOG = LoggerFactory.getLogger(SearchCategoryMetadataEsIT.class);

    @BeforeClass
    public static void init() {
        resetStepNumbers();
    }

    @Test
    public void testSearchingCategoryPropertiesEs() {
        //Allow longer wait (ten seconds) for indexing requirement detection and completion
        String searchTerm;
        LOG.info("=== Starting Integration Test: " + this.getClass().getName());

        LOG.info(getStepNumber() + "Getting list of kylo indexes ...");
        if (!populateKyloIndexes()) {
            Assert.fail("Error getting list of kylo indexes");
        }
        LOG.info("[total number of indexes: " + kyloIndexList.size() + "]");

        LOG.info(getStepNumber() + "Deleting existing kylo indexes to set up a clean env ...");
        if (!deleteEsIndexes()) {
            Assert.fail("Error deleting existing kylo indexes");
        }

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Creating kylo indexes to set up a clean env ...");
        if (!createEsIndexes()) {
            Assert.fail("Error creating kylo indexes");
        }

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Verifying created kylo indexes ...");
        if (!verifyEsIndexes()) {
            Assert.fail("Error verifying created kylo indexes");
        }

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Creating a test category ...");
        FeedCategory createdCategory = createKyloCategoryForSearch("Cat Search Prop ES IT",
                                                                   "Kylo Category Created By Search Properties (Category) ES Integration Test",
                                                                   true);

        if (createdCategory == null) {
            Assert.fail("Error creating test category");
        }
        LOG.info("[Created test category with id: " + createdCategory.getId() + "]");

        waitForSomeTime(TEN_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Verifying test category was created and has user-defined properties ...");
        FeedCategory resultCategory = verifyCreatedCategoryAndUserProperties(createdCategory);
        Assert.assertEquals(CATEGORIES_USER_PROPERTIES_COUNT, resultCategory.getUserProperties().size());
        LOG.info("[Total user-defined properties: " + resultCategory.getUserProperties().size() + "]");

        LOG.info(getStepNumber() + "Executing full search on category metadata and verifying index doc completeness ...");
        int countOfVerifiedFields = verifyIndexDocCompleteness(createdCategory.getId());
        Assert.assertEquals(CATEGORIES_INDEXED_FIELD_COUNT, countOfVerifiedFields);
        LOG.info("[Total fields verified in index doc: " + countOfVerifiedFields + "]");

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = CATEGORY_VALID_PART_OF_USER_PROPERTY_VALUE;    //ad92, will be reused
        LOG.info(getStepNumber() + "Executing search 1 (user properties) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnUserProperties1(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "nothing-should-match";
        LOG.info(getStepNumber() + "Executing search 2 (user properties) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnUserProperties2(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "run mode=AUTO i56 daily";
        LOG.info(getStepNumber() + "Executing search 3 (user properties) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnUserProperties3(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "run mode=AUTO i5";
        LOG.info(getStepNumber() + "Executing search 4 (user properties) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnUserProperties4(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "Kylo Category Created";
        LOG.info(getStepNumber() + "Executing search 1 (description) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnDescription1(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "Search Prop";
        LOG.info(getStepNumber() + "Executing search 1 (title i.e display name) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnTitle1(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        searchTerm = "cat_search_prop_es_it";
        LOG.info(getStepNumber() + "Executing search 1 (system name) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnSystemName1(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        if (allowsIndexControl()) {

            LOG.info(getStepNumber() + "Disabling indexing of category metadata ...");
            disableMetadataIndexingForFeedAndThenSearchAgain(createdCategory.getId(), CATEGORY_VALID_PART_OF_USER_PROPERTY_VALUE);

            waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

            LOG.info(getStepNumber() + "Re-enabling indexing of category metadata ...");
            enableMetadataIndexingForFeedAndThenSearchAgain(createdCategory.getId(), CATEGORY_VALID_PART_OF_USER_PROPERTY_VALUE);

            waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

            LOG.info(getStepNumber() + "Executing full search on category metadata and verifying index doc completeness...");
            countOfVerifiedFields = verifyIndexDocCompleteness(createdCategory.getId());
            Assert.assertEquals(CATEGORIES_INDEXED_FIELD_COUNT, countOfVerifiedFields);

            waitForSomeTime(FOUR_SECONDS_IN_MILLIS);
        }

        searchTerm = "REVISEDCATDESCRIPTION";
        LOG.info(getStepNumber() + "Executing search 2 (description) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnDescription2(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Editing category description and saving category ...");
        editCategoryDescriptionAndSaveCategory(createdCategory.getId(), searchTerm);

        waitForSomeTime(TEN_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Executing search 3 (description) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnDescription3(searchTerm);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Executing full search on category metadata and verifying index doc completeness...");
        countOfVerifiedFields = verifyIndexDocCompleteness(createdCategory.getId());
        Assert.assertEquals(CATEGORIES_INDEXED_FIELD_COUNT, countOfVerifiedFields);

        waitForSomeTime(FOUR_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Deleting category with id: " + createdCategory.getId() + " ...");
        deleteKyloCategory(createdCategory.getId());

        waitForSomeTime(TEN_SECONDS_IN_MILLIS);

        LOG.info(getStepNumber() + "Executing search 4 (description) and verifying results with term: " + searchTerm + " ...");
        verifySearchResultOnDescription4(searchTerm);

        LOG.info("=== Finished Integration Test: " + this.getClass().getName());
    }

    private FeedCategory verifyCreatedCategoryAndUserProperties(FeedCategory createdCategory) {
        Response response = getCategoryByName(createdCategory.getSystemName());
        response.then().statusCode(HTTP_OK);
        return response.as(FeedCategory.class);
    }

    private void verifyCommonPortionOfSearchResultOnCategory(SearchResult searchResult, Set highlightKeySet) {
        Assert.assertEquals("1", searchResult.getTotalHits().toString());
        Assert.assertEquals("KYLO_CATEGORIES", searchResult.getSearchResults().get(0).getType().toString());
        Assert.assertEquals("Cat Search Prop ES IT", ((CategoryMetadataSearchResultData) searchResult.getSearchResults().get(0)).getCategoryTitle());
        Assert.assertEquals("cat_search_prop_es_it", ((CategoryMetadataSearchResultData) searchResult.getSearchResults().get(0)).getCategorySystemName());
        Assert.assertTrue(((CategoryMetadataSearchResultData) searchResult.getSearchResults().get(0)).getCategoryDescription()
                              .startsWith("Kylo Category Created By Search Properties (Category) ES Integration Test -"));
        Assert.assertEquals("test type=T100 ad92\trun mode=AUTO i56 daily",
                            ((CategoryMetadataSearchResultData) searchResult.getSearchResults().get(0)).getUserProperties());

        Assert.assertEquals(highlightKeySet.size(), searchResult.getSearchResults().get(0).getHighlights().size());

        for (int i = 0; i < searchResult.getSearchResults().get(0).getHighlights().size(); i++) {
            String key = searchResult.getSearchResults().get(0).getHighlights().get(i).getKey();
            Assert.assertTrue(highlightKeySet.contains(key));
        }
    }

    private void verifySearchResultOnUserProperties1(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);

        SearchResult searchResult = response.as(SearchResult.class);
        verifyCommonPortionOfSearchResultOnCategory(searchResult, new HashSet<>(Collections.singletonList("User properties")));

        String expected = "<table style='border:1px solid black; border-collapse: collapse;'>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>name</font></td>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>value</font></td>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "run mode</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "AUTO i56 daily</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "test type</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "T100 <font style='font-weight:bold'>ad92</font></td style='border:1px solid black;padding: 3px;'>\n"
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
        verifyCommonPortionOfSearchResultOnCategory(searchResult, new HashSet<>(Collections.singletonList("User properties")));

        String expected = "<table style='border:1px solid black; border-collapse: collapse;'>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>name</font></td>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>value</font></td>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>run</font> <font style='font-weight:bold'>mode</font></td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>AUTO</font> <font style='font-weight:bold'>i56</font> <font style='font-weight:bold'>daily</font></td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "test type</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "T100 ad92</td style='border:1px solid black;padding: 3px;'>\n"
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
        verifyCommonPortionOfSearchResultOnCategory(searchResult, new HashSet<>(Collections.singletonList("User properties")));

        String expected = "<table style='border:1px solid black; border-collapse: collapse;'>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>name</font></td>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>value</font></td>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>run</font> <font style='font-weight:bold'>mode</font></td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "<font style='font-weight:bold'>AUTO</font> i56 daily</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "test type</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "T100 ad92</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "</table>";
        Assert.assertEquals(expected,
                            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());
    }

    private void verifySearchResultOnDescription1(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);

        SearchResult searchResult = response.as(SearchResult.class);
        verifyCommonPortionOfSearchResultOnCategory(searchResult, new HashSet<>(Collections.singletonList("Description")));
        String
            expectedHighlightSubstring =
            "<font style='font-weight:bold'>Kylo</font> <font style='font-weight:bold'>Category</font> <font style='font-weight:bold'>Created</font> By Search Properties (<font style='font-weight:bold'>Category</font>) ES Integration Test";
        Assert.assertTrue(searchResult.getSearchResults().get(0).getHighlights().get(0).getValue().toString().contains(expectedHighlightSubstring));
    }

    private void verifySearchResultOnTitle1(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);

        SearchResult searchResult = response.as(SearchResult.class);
        verifyCommonPortionOfSearchResultOnCategory(searchResult, new HashSet<>(Arrays.asList("Description", "Title")));
        String expectedDescriptionHighlightSubstring = "Kylo Category Created By <font style='font-weight:bold'>Search</font> Properties (Category) ES Integration Test";
        Assert.assertTrue(searchResult.getSearchResults().get(0).getHighlights().get(0).getValue().toString().contains(expectedDescriptionHighlightSubstring));

        Assert.assertEquals("Cat <font style='font-weight:bold'>Search</font> <font style='font-weight:bold'>Prop</font> ES IT",
                            searchResult.getSearchResults().get(0).getHighlights().get(1).getValue());
    }

    private void verifySearchResultOnSystemName1(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        verifyCommonPortionOfSearchResultOnCategory(searchResult, new HashSet<>(Collections.singletonList("System name (Kylo)")));
        Assert.assertEquals(
            "<font style='font-weight:bold'>cat_search_prop_es_it</font>",
            searchResult.getSearchResults().get(0).getHighlights().get(0).getValue());
    }

    private int verifyIndexDocCompleteness(String id) {
        String url = getBaseEsUrl() + CATEGORIES_INDEX_PREFIX + "-" + WORKSPACE + "/" + CATEGORIES_DOCTYPE + "/" + id;
        Response response = RestAssured.get(url);

        response.then()
            .assertThat()
            .body(matchesJsonSchemaInClasspath(CATEGORIES_SEARCH_RESULT_SCHEMA_JSON_PATH));

        String responseJson = response.asString();
        JsonPath responseJsonPath = new JsonPath(responseJson);

        List<String> indexedFieldsWithJsonPathForCategory = getIndexedFieldsWithJsonPathForEntity();
        indexedFieldsWithJsonPathForCategory.forEach(field -> Assert.assertNotNull(responseJsonPath.get(field)));
        return indexedFieldsWithJsonPathForCategory.size();
    }

    private void disableMetadataIndexingForFeedAndThenSearchAgain(String categoryId, String searchTermThatGivesResultsWhenIndexingIsOn) {
        Response response = getCategoryById(categoryId);
        FeedCategory feedCategory = response.as(FeedCategory.class);
        Assert.assertTrue(feedCategory.isAllowIndexing());
        feedCategory.setAllowIndexing(false);
        createCategory(feedCategory);

        waitForSomeTime(TEN_SECONDS_IN_MILLIS);

        Response searchResponse = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + searchTermThatGivesResultsWhenIndexingIsOn);

        searchResponse.then().statusCode(HTTP_OK);
        SearchResult searchResult = searchResponse.as(SearchResult.class);
        Assert.assertEquals("0", searchResult.getTotalHits().toString());
    }

    private void enableMetadataIndexingForFeedAndThenSearchAgain(String categoryId, String searchTermThatGivesResultsWhenIndexingIsOn) {
        Response response = getCategoryById(categoryId);
        FeedCategory feedCategory = response.as(FeedCategory.class);
        Assert.assertFalse(feedCategory.isAllowIndexing());
        feedCategory.setAllowIndexing(true);
        createCategory(feedCategory);

        waitForSomeTime(TEN_SECONDS_IN_MILLIS);

        Response searchResponse = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + searchTermThatGivesResultsWhenIndexingIsOn);

        searchResponse.then().statusCode(HTTP_OK);

        SearchResult searchResult = searchResponse.as(SearchResult.class);
        verifyCommonPortionOfSearchResultOnCategory(searchResult, new HashSet<>(Collections.singletonList("User properties")));

        String expected = "<table style='border:1px solid black; border-collapse: collapse;'>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>name</font></td>\n"
                          + "<td style='border:1px solid black;padding: 3px;'><font style='font-style:italic'>value</font></td>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "run mode</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "AUTO i56 daily</td style='border:1px solid black;padding: 3px;'>\n"
                          + "</tr>\n"
                          + "<tr>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "test type</td style='border:1px solid black;padding: 3px;'>\n"
                          + "<td style='border:1px solid black;padding: 3px;'>\n"
                          + "T100 <font style='font-weight:bold'>ad92</font></td style='border:1px solid black;padding: 3px;'>\n"
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

    private void editCategoryDescriptionAndSaveCategory(String categoryId, String additionToCurrentDescription) {
        Response response = getCategoryById(categoryId);
        FeedCategory feedCategory = response.as(FeedCategory.class);
        String currentCategoryDescription = feedCategory.getDescription();
        feedCategory.setDescription(currentCategoryDescription + " - " + additionToCurrentDescription);
        createCategory(feedCategory);
    }

    private void verifySearchResultOnDescription3(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);

        SearchResult searchResult = response.as(SearchResult.class);

        verifyCommonPortionOfSearchResultOnCategory(searchResult, new HashSet<>(Collections.singletonList("Description")));
        String expectedHighlightSubstring1 = "Kylo Category Created By Search Properties (Category) ES Integration Test";
        String expectedHighlightSubstring2 = "<font style='font-weight:bold'>REVISEDCATDESCRIPTION</font>";
        Assert.assertTrue(searchResult.getSearchResults().get(0).getHighlights().get(0).getValue().toString().contains(expectedHighlightSubstring1));
        Assert.assertTrue(searchResult.getSearchResults().get(0).getHighlights().get(0).getValue().toString().contains(expectedHighlightSubstring2));
    }

    private void verifySearchResultOnDescription4(String term) {
        Response response = given(SearchRestController.BASE)
            .when()
            .get("/?q=" + term);

        response.then().statusCode(HTTP_OK);
        SearchResult searchResult = response.as(SearchResult.class);
        Assert.assertEquals("0", searchResult.getTotalHits().toString());
    }

    public List<String> getIndexedFieldsWithJsonPathForEntity() {
        List<String> indexedFields = new ArrayList<>();
        indexedFields.add("_source.'jcr:created'");
        indexedFields.add("_source.'jcr:createdBy'");
        indexedFields.add("_source.'jcr:description'");
        indexedFields.add("_source.'jcr:lastModified'");
        indexedFields.add("_source.'jcr:lastModifiedBy'");
        indexedFields.add("_source.'jcr:title'");
        indexedFields.add("_source.'length_jcr:created'");
        indexedFields.add("_source.'length_jcr:createdBy'");
        indexedFields.add("_source.'length_jcr:description'");
        indexedFields.add("_source.'length_jcr:lastModified'");
        indexedFields.add("_source.'length_jcr:lastModifiedBy'");
        indexedFields.add("_source.'length_jcr:title'");
        indexedFields.add("_source.'length_tba:allowIndexing'");
        indexedFields.add("_source.'length_tba:systemName'");
        indexedFields.add("_source.'length_usr:properties'");
        indexedFields.add("_source.'lowercase_jcr:created'");
        indexedFields.add("_source.'lowercase_jcr:createdBy'");
        indexedFields.add("_source.'lowercase_jcr:description'");
        indexedFields.add("_source.'lowercase_jcr:lastModified'");
        indexedFields.add("_source.'lowercase_jcr:lastModifiedBy'");
        indexedFields.add("_source.'lowercase_jcr:title'");
        indexedFields.add("_source.'lowercase_tba:allowIndexing'");
        indexedFields.add("_source.'lowercase_tba:systemName'");
        indexedFields.add("_source.'lowercase_usr:properties'");
        indexedFields.add("_source.'tba:allowIndexing'");
        indexedFields.add("_source.'tba:systemName'");
        indexedFields.add("_source.'uppercase_jcr:created'");
        indexedFields.add("_source.'uppercase_jcr:createdBy'");
        indexedFields.add("_source.'uppercase_jcr:description'");
        indexedFields.add("_source.'uppercase_jcr:lastModified'");
        indexedFields.add("_source.'uppercase_jcr:lastModifiedBy'");
        indexedFields.add("_source.'uppercase_jcr:title'");
        indexedFields.add("_source.'uppercase_tba:allowIndexing'");
        indexedFields.add("_source.'uppercase_tba:systemName'");
        indexedFields.add("_source.'uppercase_usr:properties'");
        indexedFields.add("_source.'usr:properties'");
        return indexedFields;
    }

    public boolean allowsIndexControl() {
        return true;
    }
}
