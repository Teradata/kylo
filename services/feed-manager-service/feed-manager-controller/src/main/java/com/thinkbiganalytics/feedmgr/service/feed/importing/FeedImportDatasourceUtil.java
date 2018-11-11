package com.thinkbiganalytics.feedmgr.service.feed.importing;
/*-
 * #%L
 * thinkbig-feed-manager-controller
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
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.ImportProperty;
import com.thinkbiganalytics.feedmgr.rest.model.ImportPropertyBuilder;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.LegacyDatasource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FeedImportDatasourceUtil {

    private static final Logger log = LoggerFactory.getLogger(FeedImportDatasourceUtil.class);

    /**
     * key that will help the importer decide to show datasets
     */
    public static final String LEGACY_TABLE_DATA_SOURCE_KEY = "legacyTableDataSource";
    /**
     * key that will help the importer decide to show catalog datasources
     */
    public static final String LEGACY_QUERY_DATA_SOURCE_KEY = "legacyQueryDataSource";


    public static final String CATALOG_DATASOURCE_KEY = "catalogDataSource";

    public static void replaceMap(Map<String,Object> map, String lookFor, String replace) {
        for(String key :map.keySet()) {
            Object value = map.get(key);
            if (value instanceof String && ((String) value).equalsIgnoreCase(lookFor)) {
                //REPLACE IT
                map.put(key,replace);
                log.info("Replaced {} with {} in Map ", lookFor,replace);
            } else if (value instanceof List) {
                List<Object> copiedList = FeedImportDatasourceUtil.replaceList((List<Object>)value, lookFor, replace);
                map.put(key,copiedList);
            } else if (value instanceof Map) {
                FeedImportDatasourceUtil.replaceMap((Map<String,Object>) value, lookFor, replace);
            }
            else {
                map.put(key,value);
            }
        }
    }

    public static List<Object> replaceList(List<Object> list,String lookFor, String replace) {
        List<Object> copy = new ArrayList<>();

        for (Object value : list) {
            if (value instanceof String && ((String) value).equalsIgnoreCase(lookFor)) {
                //REPLACE IT
                log.info("Replaced {} with {} in List ", lookFor,replace);
                copy.add(replace);
            } else if (value instanceof List) {
                List<Object> copiedList = FeedImportDatasourceUtil.replaceList((List<Object>) value, lookFor, replace);
                copy.add(copiedList);

            } else if (value instanceof Map) {
                copy.add(value);
                FeedImportDatasourceUtil.replaceMap((Map<String, Object>) value, lookFor, replace);
            } else {
                copy.add(value);
            }

        }
        return copy;
    }

    public static void replaceChartModelReferences(FeedMetadata metadata, Map<String,String>replacements){

        if(metadata.getDataTransformation() != null && StringUtils.isNotBlank(metadata.getDataTransformation().getDataTransformScript()) && metadata.getDataTransformation().getChartViewModel() != null && replacements != null && replacements.size() >0) {

            List<Map<String, Object>> nodes = (List<Map<String, Object>>) metadata.getDataTransformation().getChartViewModel().get("nodes");
            if (nodes != null) {
                nodes.stream().forEach((nodeMap) -> {
                    replacements.entrySet().stream().forEach(entry -> replaceMap(nodeMap, entry.getKey(), entry.getValue()));
                });
            }

        }
        if(metadata.getDataTransformation() != null && metadata.getDataTransformation().getCatalogDataSourceIds() != null) {
            //update the catalogDataSourceIds and the metadata.datasourceids with the new ids
            List<String> newIds = metadata.getDataTransformation().getCatalogDataSourceIds().stream().map(id -> replacements.containsKey(id) ? replacements.get(id) : id).collect(Collectors.toList());
            metadata.getDataTransformation().setCatalogDataSourceIds(newIds);
        }
        if(metadata.getDataTransformation() != null && metadata.getDataTransformation().getDatasourceIds() != null) {
            List<String>
                updatedDatasourceIds =
                metadata.getDataTransformation().getDatasourceIds().stream().map(id -> replacements.containsKey(id) ? replacements.get(id) : id).collect(Collectors.toList());
            metadata.getDataTransformation().setDatasourceIds(updatedDatasourceIds);
        }

    }

    public static void populateFeedDatasourceIdsProperty(FeedMetadata metadata){
        //fill the datasourceIds array will catalog datasets and datasources for backwards compatability
        List<String> catalogDataSources =metadata.getDataTransformation() != null ? metadata.getDataTransformation().getCatalogDataSourceIds() : null;
        List<DataSet> catalogDataSets = metadata.getSourceDataSets();
        List<String> datasourceIds = metadata.getDataTransformation() != null ? metadata.getDataTransformation().getDatasourceIds() : null;

        final  List<String> ids  = datasourceIds != null ? datasourceIds : new ArrayList<>();
        if(catalogDataSets != null){
            catalogDataSets.stream().filter(dataSet -> !ids.contains(dataSet.getId())).forEach(dataSet -> ids.add(dataSet.getId()));
        }

        if(catalogDataSources != null){
            catalogDataSources.stream().filter(datasource -> !ids.contains(datasource)).forEach(datasource -> ids.add(datasource));
        }
        metadata.getDataTransformation().setDatasourceIds(ids);

    }

    public static void fixDatasetMatchesUserDataSource(FeedMetadata metadata, String datasourceId, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet dataSet){

        if(metadata.getDataTransformation() != null && StringUtils.isNotBlank(metadata.getDataTransformation().getDataTransformScript()) && metadata.getDataTransformation().getChartViewModel() != null) {

            List<Map<String, Object>> nodes = (List<Map<String, Object>>) metadata.getDataTransformation().getChartViewModel().get("nodes");
            if (nodes != null) {
                nodes.stream().filter(nodeMap -> nodeMap.containsKey("datasourceId") && ((String)nodeMap.get("datasourceId")).equalsIgnoreCase(datasourceId))
                    .forEach((nodeMap) -> {
                        nodeMap.put("datasetMatchesUserDataSource", false);
                        if(!nodeMap.containsKey("dataset")){
                            Map<String,Object> map = new HashMap<>();
                            map.put("id",dataSet.getId());
                            nodeMap.put("dataset",map);
                        }
                    });
            }
        }
    }

    public static void replaceLegacyDataSourceScript(FeedMetadata metadata,String table, String datasourceId, DataSet dataSet){

        String find = "datasourceProvider.getTableFromDatasource(\""+table+"\", \""+datasourceId+"\", sqlContext)";
        String replace = "catalogDataSetProvider.read(\""+dataSet.getId()+"\")";
        String replaced = metadata.getDataTransformation().getDataTransformScript().replace(find,replace);
        metadata.getDataTransformation().setDataTransformScript(replaced);
        if(metadata.getSourceDataSets() == null){
            metadata.setSourceDataSets(new ArrayList<>());
        }
        metadata.getSourceDataSets().add(dataSet);
        //remove the legacy user datasource id
        metadata.getDataTransformation().getDatasourceIds().remove(datasourceId);
        FeedImportDatasourceUtil.fixDatasetMatchesUserDataSource(metadata, datasourceId, dataSet);
    }

    public static void replaceLegacyQueryDataSourceScript(FeedMetadata metadata, String datasourceId, DataSource dataSource){


        if(metadata.getDataTransformation() != null && StringUtils.isNotBlank(metadata.getDataTransformation().getDataTransformScript())) {
            String script = metadata.getDataTransformation().getDataTransformScript();
            Pattern pattern = Pattern.compile("datasourceProvider.getTableFromDatasource\\((.*) AS KYLO_SPARK_QUERY\", \""+datasourceId+"\", sqlContext\\)");
            Matcher matcher = pattern.matcher(script);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String query = matcher.group(1).replaceAll("\"", "");
                String catalogDataSourceId = dataSource.getId();
                matcher.appendReplacement(sb,matcher.group(0).replaceFirst(datasourceId,catalogDataSourceId));
            }
            matcher.appendTail(sb);
            metadata.getDataTransformation().setDataTransformScript(sb.toString());
            //remove the legacy user datasource id
            metadata.getDataTransformation().getDatasourceIds().remove(datasourceId);

            //populate the new Catalog Datasource ids
            if(metadata.getDataTransformation().getCatalogDataSourceIds() == null){
                metadata.getDataTransformation().setCatalogDataSourceIds(new ArrayList<>());
            }
            if(!metadata.getDataTransformation().getCatalogDataSourceIds().contains(dataSource.getId())){
                metadata.getDataTransformation().getCatalogDataSourceIds().add(dataSource.getId());
                metadata.getDataTransformation().updateDataSourceIdsString();
            }
        }
    }

    /**
     * Make sure the connections attributes that link source and dest nodes are correct.
     * it appears some kylo instances have the joinKeys that list the (sourceKey and destKey)mapping to the wrong node.
     * this will fix the join keys so the sourceKey maps to the source node and the destKey maps to the destNode
     * 
     * @param metadata
     */
    public static void ensureConnectionKeysMatch(FeedMetadata metadata){

        if(metadata.getDataTransformation().getChartViewModel() != null ) {
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) metadata.getDataTransformation().getChartViewModel().get("nodes");

            Map<String, Map<String, Object>> nodeById = nodes.stream().collect(Collectors.toMap(node -> node.get("id").toString(), node -> node));
            List<Map<String, Map<String, Object>>> connections = (List<Map<String, Map<String, Object>>>) metadata.getDataTransformation().getChartViewModel().get("connections");
            if (connections != null) {

                connections.stream().forEach(connection -> {
                    Map<String, Object> joinKeys = (Map<String, Object>) connection.get("joinKeys");
                    String sourceKey = joinKeys.get("sourceKey").toString();
                    String destKey = joinKeys.get("destKey").toString();

                    Map<String, Object> sourceNode = nodeById.get(connection.get("source").get("nodeID") + "");
                    Map<String, Object> destNode = nodeById.get(connection.get("dest").get("nodeID") + "");

                    Map<String, Map<String, Object>> srcNodeAttrs = ((Map<String, Map<String, Object>>) sourceNode.get("nodeAttributes"));
                    List<Map<String, String>> srcAttrs = (List<Map<String, String>>) srcNodeAttrs.get("attributes");
                    List<String> sourceFields = srcAttrs.stream().map(attr -> attr.get("name")).collect(Collectors.toList());

                    Map<String, Map<String, Object>> destNodeAttrs = ((Map<String, Map<String, Object>>) destNode.get("nodeAttributes"));
                    List<Map<String, String>> destAttrs = (List<Map<String, String>>) destNodeAttrs.get("attributes");
                    List<String> destFields = destAttrs.stream().map(attr -> attr.get("name")).collect(Collectors.toList());

                    boolean validSource = sourceFields.contains(sourceKey);
                    boolean validDest = destFields.contains(destKey);
                    if (!validDest && !validSource) {
                        //try flipping them
                        validSource = sourceFields.contains(destKey);
                        validDest = destFields.contains(sourceKey);
                        if (validDest && validSource) {
                            //flip them
                            joinKeys.put("sourceKey", destKey);
                            joinKeys.put("destKey", sourceKey);
                            log.info("Fixed source and dest Keys for feed {} ", metadata.getCategoryAndFeedName());
                        }
                    }
                });

            }
        }

    }


    /**
     * Parse the script for all legacy datasources and return a map of datasourceId, list of datasources
     * @param metadata
     * @return return a map of datasourceId, list of datasources
     */
    public static Map<String,List<LegacyDatasource>> parseLegacyDatasourcesById(FeedMetadata metadata){
        Map<String,List<LegacyDatasource>> map = new HashMap<>();
        if(metadata.getDataTransformation() != null && StringUtils.isNotBlank(metadata.getDataTransformation().getDataTransformScript())) {
            String script = metadata.getDataTransformation().getDataTransformScript();
            Pattern pattern = Pattern.compile("datasourceProvider.getTableFromDatasource\\((.*), (.*), sqlContext\\)");
            Matcher matcher = pattern.matcher(script);
            StringBuffer sb = new StringBuffer();
            // check all occurance
            while (matcher.find()) {
                String tableOrQuery = matcher.group(1).replaceAll("\"", "");
                String datasourceId = matcher.group(2).replaceAll("\"", "");
                LegacyDatasource legacyDatasource = null;
                if(tableOrQuery.contains("KYLO_SPARK_QUERY")){
                    tableOrQuery = tableOrQuery.replaceAll("AS KYLO_SPARK_QUERY","");
                    legacyDatasource = LegacyDatasource.newQueryDatasource(tableOrQuery,datasourceId);
                }
                else {
                    legacyDatasource = LegacyDatasource.newTableDatasource(tableOrQuery,datasourceId);
                }
                map.computeIfAbsent(datasourceId, dsId -> new ArrayList<LegacyDatasource>()).add(legacyDatasource);
            }
        }
        return map;

    }

    /**
     * For the script if there is no matching userdatasource, build the import property for the user to supply the matching dataset
     * @param metadata
     * @param availableDatasources
     * @return
     */
    public static List<ImportProperty> buildDatasourceAssignmentProperties(FeedMetadata metadata, Set<String> availableDatasources){
        Map<String,List<LegacyDatasource>> legacyDatasources = FeedImportDatasourceUtil.parseLegacyDatasourcesById(metadata);

        final List<Datasource> providedDatasources = Optional.ofNullable(metadata.getUserDatasources()).orElse(Collections.emptyList());
        Map<String,Datasource>providedDatasourcesMap = providedDatasources.stream()
            .collect(Collectors.toMap(ds->ds.getId(), ds1->ds1));

       return  providedDatasources.stream()
            .filter(datasource -> (availableDatasources == null || !availableDatasources.contains(datasource.getId())))
            .flatMap(datasource -> legacyDatasources.get(datasource.getId()).stream())
            .map(legacyDataSource -> {
                Datasource legacySource = providedDatasourcesMap.get(legacyDataSource.getDatasourceId());
                if(legacyDataSource.isDataSet()) {
                    return ImportPropertyBuilder.anImportProperty()
                        .withComponentName(legacySource.getName())
                        .withDisplayName(legacyDataSource.getTable())
                        .withComponentId(legacyDataSource.getKey())
                        .withPropertyKey(legacyDataSource.getKey().replace(".", "-").replace(" ", "_"))
                        .putAdditionalProperty("table", legacyDataSource.getTable())
                        .putAdditionalProperty("datasourceId", legacyDataSource.getDatasourceId())
                        .putAdditionalProperty(LEGACY_TABLE_DATA_SOURCE_KEY, "true").build();
                }
                else {
                    return ImportPropertyBuilder.anImportProperty()
                        .withComponentName(legacySource.getName())
                        .withDisplayName(legacyDataSource.getQuery())
                        .withComponentId(legacyDataSource.getKey())
                        .withPropertyKey(legacyDataSource.getKey().replace(".", "-").replace(" ", "_"))
                        .putAdditionalProperty("query", legacyDataSource.getQuery())
                        .putAdditionalProperty("datasourceId", legacyDataSource.getDatasourceId())
                        .putAdditionalProperty(LEGACY_QUERY_DATA_SOURCE_KEY, "true").build();
                }
            })
            .collect(Collectors.toList());

    }

    public static List<ImportProperty> buildCatalogDatasourceAssignmentProperties(FeedMetadata metadata, Set<String> availableDatasources){

        if(metadata.getDataTransformation().getCatalogDataSourceIds() != null && !metadata.getDataTransformation().getCatalogDataSourceIds().isEmpty()){

         return   metadata.getDataTransformation().getCatalogDataSourceIds().stream().filter(id -> availableDatasources == null || !availableDatasources.contains(id)).map(id -> {
                return ImportPropertyBuilder.anImportProperty()
                    .withComponentName("Catalog Source")
                    .withDisplayName( metadata.getDataTransformation().getSql())
                    .withComponentId(id)
                    .withPropertyKey(id.replace(".", "-").replace(" ", "_"))
                    .putAdditionalProperty("datasourceId", id)
                    .putAdditionalProperty("query", metadata.getDataTransformation().getSql())
                    .putAdditionalProperty(CATALOG_DATASOURCE_KEY, "true").build();
            }).collect(Collectors.toList());

        }
        else {
            return Collections.emptyList();
        }

    }

}
