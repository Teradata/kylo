package com.thinkbiganalytics.search.transform;

/*-
 * #%L
 * kylo-search-elasticsearch-rest
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

import com.thinkbiganalytics.search.api.SearchIndex;
import com.thinkbiganalytics.search.rest.model.CategoryMetadataSearchResultData;
import com.thinkbiganalytics.search.rest.model.FeedMetadataSearchResultData;
import com.thinkbiganalytics.search.rest.model.HiveColumn;
import com.thinkbiganalytics.search.rest.model.Pair;
import com.thinkbiganalytics.search.rest.model.SchemaSearchResultData;
import com.thinkbiganalytics.search.rest.model.SearchResult;
import com.thinkbiganalytics.search.rest.model.SearchResultData;
import com.thinkbiganalytics.search.rest.model.SearchResultType;
import com.thinkbiganalytics.search.rest.model.TableSearchResultData;
import com.thinkbiganalytics.search.rest.model.UnknownTypeSearchResultData;
import com.thinkbiganalytics.search.rest.model.es.ElasticSearchRestSearchHit;
import com.thinkbiganalytics.search.rest.model.es.ElasticSearchRestSearchResponse;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transform Elasticsearch result (via rest client) to REST model
 */
public class ElasticSearchRestSearchResultTransform {

    private Long tableDataTypeResultCount = 0L;
    private Long schemaTypeResultCount = 0L;
    private Long feedsMetadataTypeResultCount = 0L;
    private Long categoriesMetadataTypeResultCount = 0L;
    private Long unknownTypeResultCount = 0L;
    private static final String EMPTY_STRING = "";
    private static final String RAW_DATA_KEY = "raw";


    public SearchResult transformRestResult(String query, int size, int start, ElasticSearchRestSearchResponse restSearchResponse) {
        final String KYLO_DATA = "kylo-data";
        final String KYLO_SCHEMA_METADATA = "kylo-schema-metadata";
        final String KYLO_FEEDS = "kylo-feeds";
        final String KYLO_CATEGORIES = "kylo-categories";
        final String ELASTIC_SEARCH_RC = "Elasticsearch (rc)";

        List<SearchResultData> searchResultData = new ArrayList<>();
        for (ElasticSearchRestSearchHit elasticSearchRestSearchHit : restSearchResponse.getElasticSearchRestSearchHits()) {
            if (elasticSearchRestSearchHit.getIndexName().equals(KYLO_DATA)) {
                searchResultData.add(getTableSearchResultData(elasticSearchRestSearchHit));
            } else if (elasticSearchRestSearchHit.getIndexName().equals(SearchIndex.DATASOURCES)) {
                searchResultData.add(getSchemaSearchResultData(elasticSearchRestSearchHit));
            } else if (elasticSearchRestSearchHit.getIndexName().contains(KYLO_FEEDS)) {
                //This is dependent upon ModeShape configuration. The prefix will remain the same.
                // Hence only checking part of index name
                searchResultData.add(getFeedMetadataSearchResultData(elasticSearchRestSearchHit));
            } else if (elasticSearchRestSearchHit.getIndexName().contains(KYLO_CATEGORIES)) {
                //This is dependent upon ModeShape configuration. The prefix will remain the same.
                // Hence only checking part of index name
                searchResultData.add(getCategoryMetadataSearchResultData(elasticSearchRestSearchHit));
            } else {
                searchResultData.add(getUnknownTypeSearchResultData(elasticSearchRestSearchHit));
            }
        }

        SearchResult elasticSearchRestResult = new SearchResult();
        elasticSearchRestResult.setQuery(query);

        Long totalHits = restSearchResponse.getTotalResults();
        elasticSearchRestResult.setTotalHits(totalHits);
        elasticSearchRestResult.setFrom((long) (start + 1));
        elasticSearchRestResult.setTo((long) (start + size));

        if (totalHits < (start + size)) {
            elasticSearchRestResult.setTo(totalHits);
        }

        if (totalHits == 0) {
            elasticSearchRestResult.setFrom(0L);
        }

        elasticSearchRestResult.setTookInMillis(restSearchResponse.getTookInMillis());
        elasticSearchRestResult.setEngine(ELASTIC_SEARCH_RC);
        elasticSearchRestResult.setSearchResults(searchResultData);

        elasticSearchRestResult.setSearchResultsSummary(getSearchResultSummary());

        return elasticSearchRestResult;
    }

    private TableSearchResultData getTableSearchResultData(ElasticSearchRestSearchHit elasticSearchRestSearchHit) {
        final String KYLO_SCHEMA = "kylo_schema";
        final String KYLO_TABLE = "kylo_table";
        final String POST_DATE = "post_date";

        TableSearchResultData tableSearchResultData = new TableSearchResultData();
        tableSearchResultData.setSchemaName(elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(KYLO_SCHEMA, EMPTY_STRING).toString());
        tableSearchResultData.setTableName(elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(KYLO_TABLE, EMPTY_STRING).toString());
        List<Pair> columnNamesAndValues = new ArrayList<>();
        List<Pair> highlightsList = new ArrayList<>();

        for (Pair sourcePairInRestSearchHit : elasticSearchRestSearchHit.getSource()) {
            String key = sourcePairInRestSearchHit.getKey();
            if (!((key.equals(POST_DATE)) || (key.equals(KYLO_SCHEMA)) || (key.equals(KYLO_TABLE)))) {
                columnNamesAndValues.add(sourcePairInRestSearchHit);
            }
        }

        highlightsList.addAll(elasticSearchRestSearchHit.getHighlights());
        tableSearchResultData.setHighlights(highlightsList);
        tableSearchResultData.setColumnNamesAndValues(columnNamesAndValues);

        Map<String, Object> rawData = new HashMap<>();
        rawData.put(RAW_DATA_KEY, elasticSearchRestSearchHit.getRawHit());
        tableSearchResultData.setRawData(rawData);
        tableDataTypeResultCount += 1L;
        return tableSearchResultData;
    }

    private SchemaSearchResultData getSchemaSearchResultData(ElasticSearchRestSearchHit elasticSearchRestSearchHit) {
        final String DATABASE_NAME = "databaseName";
        final String DATABASE_OWNER = "databaseOwner";
        final String TABLE_CREATE_TIME = "tableCreateTime";
        final String TABLE_NAME = "tableName";
        final String TABLE_TYPE = "tableType";
        final String HIVE_COLUMNS = "hiveColumns";
        final String COLUMN_TYPE = "columnType";
        final String COLUMN_NAME = "columnName";
        final String COLUMN_COMMENT = "columnComment";

        final String HIVE_COLUMNS_COLUMN_TYPE = HIVE_COLUMNS + "." + COLUMN_TYPE;
        final String HIVE_COLUMNS_COLUMN_TYPE_NEW_DESCRIPTION = "Column type";
        final String HIVE_COLUMNS_COLUMN_NAME = HIVE_COLUMNS + "." + COLUMN_NAME;
        final String HIVE_COLUMNS_COLUMN_NAME_NEW_DESCRIPTION = "Column name";
        final String HIVE_COLUMNS_COLUMN_COMMENT = HIVE_COLUMNS + "." + COLUMN_COMMENT;
        final String HIVE_COLUMNS_COLUMN_COMMENT_NEW_DESCRIPTION = "Column comment";

        SchemaSearchResultData schemaSearchResultData = new SchemaSearchResultData();
        schemaSearchResultData.setDatabaseName(elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(DATABASE_NAME, EMPTY_STRING).toString());
        schemaSearchResultData.setDatabaseOwner(elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(DATABASE_OWNER, EMPTY_STRING).toString());
        schemaSearchResultData.setTableCreateTime(elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TABLE_CREATE_TIME, EMPTY_STRING).toString());
        schemaSearchResultData.setTableName(elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TABLE_NAME, EMPTY_STRING).toString());
        schemaSearchResultData.setTableType(elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TABLE_TYPE, EMPTY_STRING).toString());

        List<HiveColumn> hiveColumns = new ArrayList<>();
        List<Pair> highlightsList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(elasticSearchRestSearchHit.getHiveColumns())) {
            hiveColumns.addAll(elasticSearchRestSearchHit.getHiveColumns());
        }

        for (Pair highlightPair : elasticSearchRestSearchHit.getHighlights()) {
            String key = highlightPair.getKey();
            switch (key) {
                case HIVE_COLUMNS_COLUMN_TYPE:
                    key = HIVE_COLUMNS_COLUMN_TYPE_NEW_DESCRIPTION;
                    break;
                case HIVE_COLUMNS_COLUMN_NAME:
                    key = HIVE_COLUMNS_COLUMN_NAME_NEW_DESCRIPTION;
                    break;
                case HIVE_COLUMNS_COLUMN_COMMENT:
                    key = HIVE_COLUMNS_COLUMN_COMMENT_NEW_DESCRIPTION;
                    break;
                default:
                    break;
            }
            highlightsList.add(new Pair(key, highlightPair.getValue()));
        }

        schemaSearchResultData.setHighlights(highlightsList);
        schemaSearchResultData.setHiveColumns(hiveColumns);

        Map<String, Object> rawData = new HashMap<>();
        rawData.put(RAW_DATA_KEY, elasticSearchRestSearchHit.getRawHit());
        schemaSearchResultData.setRawData(rawData);
        schemaTypeResultCount += 1L;
        return schemaSearchResultData;
    }

    private FeedMetadataSearchResultData getFeedMetadataSearchResultData(ElasticSearchRestSearchHit elasticSearchRestSearchHit) {
        //jcr has no notion of null-valued property. So, account for the situation when there is no tag present.
        final String TBA_SYSTEM_NAME = "tba:systemName";
        final String JCR_TITLE = "jcr:title";
        final String JCR_DESCRIPTION = "jcr:description";
        final String TBA_CATEGORY = "tba:category";
        final String TBA_TAGS = "tba:tags";

        final String TBA_SYSTEM_NAME_NEW_DESCRIPTION = "System name (Kylo)";
        final String JCR_TITLE_NEW_DESCRIPTION = "Title";
        final String JCR_DESCRIPTION_NEW_DESCRIPTION = "Description";
        final String TBA_CATEGORY_NEW_DESCRIPTION = "Category";
        final String TBA_TAGS_NEW_DESCRIPTION = "Tags";

        final String SPACE_STRING = " ";

        FeedMetadataSearchResultData feedMetadataSearchResultData = new FeedMetadataSearchResultData();
        List<Pair> highlightsList = new ArrayList<>();

        if (!elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TBA_SYSTEM_NAME, EMPTY_STRING).equals(EMPTY_STRING)) {
            feedMetadataSearchResultData.setFeedSystemName(
                elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TBA_SYSTEM_NAME, EMPTY_STRING).toString()
            );
        }

        if (!elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(JCR_TITLE, EMPTY_STRING).equals(EMPTY_STRING)) {
            feedMetadataSearchResultData.setFeedTitle(
                elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(JCR_TITLE, EMPTY_STRING).toString()
            );
        }

        if (!elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(JCR_DESCRIPTION, EMPTY_STRING).equals(EMPTY_STRING)) {
            feedMetadataSearchResultData.setFeedDescription(
                elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(JCR_DESCRIPTION, EMPTY_STRING).toString()
            );
        }

        if (!elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TBA_CATEGORY, EMPTY_STRING).equals(EMPTY_STRING)) {
            feedMetadataSearchResultData.setFeedCategoryId(
                elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TBA_CATEGORY, EMPTY_STRING).toString()
            );
        }

        if (!elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TBA_TAGS, EMPTY_STRING).equals(EMPTY_STRING)) {
            feedMetadataSearchResultData.setFeedTags(
                Arrays.asList(elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TBA_TAGS, EMPTY_STRING).toString()
                                  .split(SPACE_STRING))
            );
        }

        for (Pair highlightPair : elasticSearchRestSearchHit.getHighlights()) {
            String key = highlightPair.getKey();
            Boolean includeHighlight = false;

            switch (key) {
                case JCR_TITLE:
                    key = JCR_TITLE_NEW_DESCRIPTION;
                    includeHighlight = true;
                    break;
                case JCR_DESCRIPTION:
                    key = JCR_DESCRIPTION_NEW_DESCRIPTION;
                    includeHighlight = true;
                    break;
                case TBA_TAGS:
                    key = TBA_TAGS_NEW_DESCRIPTION;
                    includeHighlight = true;
                    break;
                case TBA_CATEGORY:
                    key = TBA_CATEGORY_NEW_DESCRIPTION;
                    includeHighlight = true;
                    break;
                case TBA_SYSTEM_NAME:
                    key = TBA_SYSTEM_NAME_NEW_DESCRIPTION;
                    includeHighlight = true;
                    break;
                default:
                    break;
            }
            if (includeHighlight) {
                highlightsList.add(new Pair(key, highlightPair.getValue()));
            }
        }

        feedMetadataSearchResultData.setHighlights(highlightsList);

        Map<String, Object> rawData = new HashMap<>();
        rawData.put(RAW_DATA_KEY, elasticSearchRestSearchHit.getRawHit());
        feedMetadataSearchResultData.setRawData(rawData);
        feedsMetadataTypeResultCount += 1L;
        return feedMetadataSearchResultData;
    }

    private CategoryMetadataSearchResultData getCategoryMetadataSearchResultData(ElasticSearchRestSearchHit elasticSearchRestSearchHit) {
        //jcr has no notion of null-valued property. So, account for the situation when there is no tag present.
        final String TBA_SYSTEM_NAME = "tba:systemName";
        final String JCR_TITLE = "jcr:title";
        final String JCR_DESCRIPTION = "jcr:description";

        final String TBA_SYSTEM_NAME_NEW_DESCRIPTION = "System name (Kylo)";
        final String JCR_TITLE_NEW_DESCRIPTION = "Title";
        final String JCR_DESCRIPTION_NEW_DESCRIPTION = "Description";

        CategoryMetadataSearchResultData categoryMetadataSearchResultData = new CategoryMetadataSearchResultData();
        List<Pair> highlightsList = new ArrayList<>();

        if (!elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TBA_SYSTEM_NAME, EMPTY_STRING).equals(EMPTY_STRING)) {
            categoryMetadataSearchResultData.setCategorySystemName(
                elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(TBA_SYSTEM_NAME, EMPTY_STRING).toString()
            );
        }

        if (!elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(JCR_TITLE, EMPTY_STRING).equals(EMPTY_STRING)) {
            categoryMetadataSearchResultData.setCategoryTitle(
                elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(JCR_TITLE, EMPTY_STRING).toString()
            );
        }

        if (!elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(JCR_DESCRIPTION, EMPTY_STRING).equals(EMPTY_STRING)) {
            categoryMetadataSearchResultData.setCategoryDescription(
                elasticSearchRestSearchHit.findValueForKeyInSourceWithDefault(JCR_DESCRIPTION, EMPTY_STRING).toString()
            );
        }

        for (Pair highlightPair : elasticSearchRestSearchHit.getHighlights()) {
            String key = highlightPair.getKey();
            Boolean includeHighlight = false;

            switch (key) {
                case JCR_TITLE:
                    key = JCR_TITLE_NEW_DESCRIPTION;
                    includeHighlight = true;
                    break;
                case JCR_DESCRIPTION:
                    key = JCR_DESCRIPTION_NEW_DESCRIPTION;
                    includeHighlight = true;
                    break;
                case TBA_SYSTEM_NAME:
                    key = TBA_SYSTEM_NAME_NEW_DESCRIPTION;
                    includeHighlight = true;
                    break;
                default:
                    break;
            }
            if (includeHighlight) {
                highlightsList.add(new Pair(key, highlightPair.getValue()));
            }
        }

        categoryMetadataSearchResultData.setHighlights(highlightsList);
        Map<String, Object> rawData = new HashMap<>();
        rawData.put(RAW_DATA_KEY, elasticSearchRestSearchHit.getRawHit());
        categoryMetadataSearchResultData.setRawData(rawData);
        categoriesMetadataTypeResultCount += 1L;
        return categoryMetadataSearchResultData;
    }

    private UnknownTypeSearchResultData getUnknownTypeSearchResultData(ElasticSearchRestSearchHit elasticSearchRestSearchHit) {
        UnknownTypeSearchResultData unknownTypeSearchResultData = new UnknownTypeSearchResultData();
        Map<String, Object> rawData = new HashMap<>();
        rawData.put(RAW_DATA_KEY, elasticSearchRestSearchHit.getRawHit());
        unknownTypeSearchResultData.setRawData(rawData);
        unknownTypeResultCount += 1L;
        return unknownTypeSearchResultData;
    }

    private Map<SearchResultType, Long> getSearchResultSummary() {
        Map<SearchResultType, Long> searchResultSummary = new HashMap<>();

        if (tableDataTypeResultCount > 0) {
            searchResultSummary.put(SearchResultType.KYLO_DATA, tableDataTypeResultCount);
        }

        if (schemaTypeResultCount > 0) {
            searchResultSummary.put(SearchResultType.KYLO_SCHEMA, schemaTypeResultCount);
        }

        if (feedsMetadataTypeResultCount > 0) {
            searchResultSummary.put(SearchResultType.KYLO_FEEDS, feedsMetadataTypeResultCount);
        }

        if (categoriesMetadataTypeResultCount > 0) {
            searchResultSummary.put(SearchResultType.KYLO_CATEGORIES, categoriesMetadataTypeResultCount);
        }

        if (unknownTypeResultCount > 0) {
            searchResultSummary.put(SearchResultType.KYLO_UNKNOWN, unknownTypeResultCount);
        }

        return searchResultSummary;
    }
}
