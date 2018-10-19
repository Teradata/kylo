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

        if(metadata.getDataTransformation() != null && StringUtils.isNotBlank(metadata.getDataTransformation().getDataTransformScript())) {

            List<Map<String, Object>> nodes = (List<Map<String, Object>>) metadata.getDataTransformation().getChartViewModel().get("nodes");
            if (nodes != null) {
                nodes.stream().forEach((nodeMap) -> {
                    replacements.entrySet().stream().forEach(entry -> replaceMap(nodeMap, entry.getKey(), entry.getValue()));
                });
            }

        }

    }

    public static void fixDatasetMatchesUserDataSource(FeedMetadata metadata, String datasourceId, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet dataSet){

        if(metadata.getDataTransformation() != null && StringUtils.isNotBlank(metadata.getDataTransformation().getDataTransformScript())) {

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
        //remove the datasource id
        metadata.getDataTransformation().getDatasourceIds().remove(datasourceId);
        FeedImportDatasourceUtil.fixDatasetMatchesUserDataSource(metadata, datasourceId, dataSet);
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
                String table = matcher.group(1).replaceAll("\"", "");
                String datasourceId = matcher.group(2).replaceAll("\"", "");
                map.computeIfAbsent(datasourceId, dsId -> new ArrayList<LegacyDatasource>()).add(new LegacyDatasource(table, datasourceId));
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
            .filter(datasource -> !availableDatasources.contains(datasource.getId()))
            .flatMap(datasource -> legacyDatasources.get(datasource.getId()).stream())
            .map(legacyDataSource -> {
                Datasource legacySource = providedDatasourcesMap.get(legacyDataSource.getDatasourceId());
                return ImportPropertyBuilder.anImportProperty()
                    .withComponentName(legacySource.getName())
                    .withDisplayName(legacyDataSource.getTable())
                    .withComponentId(legacyDataSource.getKey())
                    .withPropertyKey(legacyDataSource.getKey().replace(".","-").replace(" ","_"))
                    .putAdditionalProperty("table",legacyDataSource.getTable())
                    .putAdditionalProperty("datasourceId",legacyDataSource.getDatasourceId())
                    .putAdditionalProperty("legacyDataSource","true").build();
            })
            .collect(Collectors.toList());

    }

}
