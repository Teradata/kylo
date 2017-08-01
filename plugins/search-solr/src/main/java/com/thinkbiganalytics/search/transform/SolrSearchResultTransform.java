package com.thinkbiganalytics.search.transform;

/*-
 * #%L
 * kylo-search-solr
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

import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.search.api.SearchIndex;
import com.thinkbiganalytics.search.rest.model.HiveColumn;
import com.thinkbiganalytics.search.rest.model.Pair;
import com.thinkbiganalytics.search.rest.model.SchemaSearchResultData;
import com.thinkbiganalytics.search.rest.model.SearchResult;
import com.thinkbiganalytics.search.rest.model.SearchResultData;
import com.thinkbiganalytics.search.rest.model.SearchResultType;
import com.thinkbiganalytics.search.rest.model.TableSearchResultData;
import com.thinkbiganalytics.search.rest.model.UnknownTypeSearchResultData;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Transform Solr result to REST model
 */
public class SolrSearchResultTransform {

    private Long tableDataTypeResultCount = 0L;
    private Long schemaTypeResultCount = 0L;
    private Long unknownTypeResultCount = 0L;

    public SearchResult transformResult(String query, int size, int start, QueryResponse solrResponse) {
        final String KYLO_COLLECTION = "kylo_collection";
        final String KYLO_DATA = "kylo-data";
        final String SOLR = "Solr";

        List<SearchResultData> searchResultData = new ArrayList<>();
        for (SolrDocument solrDocument : solrResponse.getResults()) {
            //Have to do this way since solrDocument.getFieldValueMap() does not work as expected.
            Map<String, Object> solrDocumentFieldValueMap = new HashMap<>();
            for (String fieldName : solrDocument.getFieldNames()) {
                solrDocumentFieldValueMap.put(fieldName, solrDocument.getFieldValue(fieldName));
            }

            if ((solrDocumentFieldValueMap.containsKey(KYLO_COLLECTION)) && (solrDocumentFieldValueMap.get(KYLO_COLLECTION).toString().equals(KYLO_DATA))) {
                searchResultData.add(getTableSearchResultData(solrDocumentFieldValueMap, solrResponse));
            }
            //collection creator needs to set up the collection to have an additional field called 'kylo_collection' that is set to the collection's name
            else if (Objects.equals(solrDocumentFieldValueMap.get(KYLO_COLLECTION), SearchIndex.DATASOURCES)) {
                searchResultData.add(getSchemaSearchResultData(solrDocumentFieldValueMap, solrResponse));
            } else {
                searchResultData.add(getUnknownTypeSearchResultData(solrDocumentFieldValueMap));
            }
        }

        SearchResult solrSearchResult = new SearchResult();
        solrSearchResult.setQuery(query);
        solrSearchResult.setTotalHits(solrResponse.getResults().getNumFound());
        solrSearchResult.setFrom((long) (start + 1));
        solrSearchResult.setTo((long) (start + size));

        if (solrResponse.getResults().getNumFound() == 0) {
            solrSearchResult.setFrom(0L);
        }

        if (solrResponse.getResults().getNumFound() < (start + size)) {
            solrSearchResult.setTo(solrResponse.getResults().getNumFound());
        }

        solrSearchResult.setTookInMillis((long) solrResponse.getQTime());
        solrSearchResult.setEngine(SOLR);
        solrSearchResult.setSearchResults(searchResultData);

        solrSearchResult.setSearchResultsSummary(getSearchResultSummary());

        return solrSearchResult;
    }


    private TableSearchResultData getTableSearchResultData(Map<String, Object> solrDocumentFieldValueMap, QueryResponse solrResponse) {
        TableSearchResultData tableSearchResultData = new TableSearchResultData();
        tableSearchResultData.setSchemaName(solrDocumentFieldValueMap.get("kylo_schema").toString());
        tableSearchResultData.setTableName(solrDocumentFieldValueMap.get("kylo_table").toString());
        List<Pair> columnNamesAndValues = new ArrayList<>();
        List<Pair> highlightsList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : solrDocumentFieldValueMap.entrySet()) {
            String key = entry.getKey();
            if (!(key.equals("kylo_collection") || key.equals("kylo_schema") || key.equals("kylo_table") || key.equals("_version_") || key.equals("id"))) {
                columnNamesAndValues.add(new Pair(key, entry.getValue()));
            }
        }

        //Only retrieve the highlights for this search result
        for (Map.Entry<String, List<String>> entry : solrResponse.getHighlighting().get(solrDocumentFieldValueMap.get("id")).entrySet()) {
            if (entry.getValue().size() >= 1) {
                highlightsList.add(new Pair(entry.getKey(), entry.getValue().get(0)));
            }
        }

        tableSearchResultData.setHighlights(highlightsList);
        tableSearchResultData.setColumnNamesAndValues(columnNamesAndValues);
        tableSearchResultData.setRawData(solrDocumentFieldValueMap);
        tableDataTypeResultCount += 1L;
        return tableSearchResultData;
    }

    private SchemaSearchResultData getSchemaSearchResultData(Map<String, Object> solrDocumentFieldValueMap, QueryResponse solrResponse) {
        SchemaSearchResultData schemaSearchResultData = new SchemaSearchResultData();
        schemaSearchResultData.setDatabaseName(toString(solrDocumentFieldValueMap.get("databaseName")));
        schemaSearchResultData.setDatabaseOwner(toString(solrDocumentFieldValueMap.get("databaseOwner")));
        schemaSearchResultData.setTableCreateTime(toString(solrDocumentFieldValueMap.get("tableCreateTime")));
        schemaSearchResultData.setTableName(toString(solrDocumentFieldValueMap.get("tableName")));
        schemaSearchResultData.setTableType(toString(solrDocumentFieldValueMap.get("tableType")));

        List<HiveColumn> hiveColumns = new ArrayList<>();
        List<Pair> highlightsList = new ArrayList<>();

        if (solrDocumentFieldValueMap.containsKey("columnName")) {
            hiveColumns.add(new HiveColumn(toString(solrDocumentFieldValueMap.get("columnName")), toString(solrDocumentFieldValueMap.get("columnType")),
                                           toString(solrDocumentFieldValueMap.get("columnComment"))));
        } else if (solrDocumentFieldValueMap.containsKey("hiveColumns")) {
            @SuppressWarnings("unchecked") final List<Map<String, Object>> columns = ObjectMapperSerializer.deserialize(toString(solrDocumentFieldValueMap.get("hiveColumns")), List.class);
            columns.forEach(column -> {
                hiveColumns.add(new HiveColumn(toString(column.get("columnName")), toString(column.get("columnType")), toString(column.get("columnComment"))));
            });
        }

        for (Map.Entry<String, List<String>> entry : solrResponse.getHighlighting().get(solrDocumentFieldValueMap.get("id")).entrySet()) {
            String key = entry.getKey();
            if (key.equals("columnName")) {
                key = "Column name";
            }
            if (entry.getValue().size() >= 1) {
                highlightsList.add(new Pair(key, entry.getValue().get(0)));
            }
        }

        schemaSearchResultData.setHighlights(highlightsList);
        schemaSearchResultData.setHiveColumns(hiveColumns);
        schemaSearchResultData.setRawData(solrDocumentFieldValueMap);
        schemaTypeResultCount += 1L;
        return schemaSearchResultData;
    }

    private UnknownTypeSearchResultData getUnknownTypeSearchResultData(Map<String, Object> solrDocumentFieldValueMap) {
        UnknownTypeSearchResultData unknownTypeSearchResultData = new UnknownTypeSearchResultData();
        unknownTypeSearchResultData.setRawData(solrDocumentFieldValueMap);
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

        if (unknownTypeResultCount > 0) {
            searchResultSummary.put(SearchResultType.KYLO_UNKNOWN, unknownTypeResultCount);
        }

        return searchResultSummary;
    }

    /**
     * Converts the specified object to a string.
     */
    @Nonnull
    private String toString(@Nullable final Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof List) {
            final List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "";
            }
            if (list.size() == 1) {
                return toString(list.get(0));
            }
            return list.toString();
        }
        return value.toString();
    }
}
