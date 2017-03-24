package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.metadata.MetadataField;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Spark script for transforming data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedDataTransformation {

    /**
     * Model for the flowchart
     */
    private Map<String, Object> chartViewModel;

    /**
     * List of required datasource ids
     */
    private List<String> datasourceIds;

    /**
     * Spark script
     */
    @MetadataField(description = "The Data Transformation Spark Script")
    private String dataTransformScript;

    /**
     * SQL query
     */
    private String sql;

    /**
     * List of Spark Shell states
     */
    private List<Map<String, Object>> states;

    /**
     * Gets the model for the flowchart.
     *
     * @return the model for the flowchart
     */
    public Map<String, Object> getChartViewModel() {
        return chartViewModel;
    }

    /**
     * Sets the model for the flowchart.
     *
     * @param chartViewModel the model for the flowchart
     */
    public void setChartViewModel(Map<String, Object> chartViewModel) {
        this.chartViewModel = chartViewModel;
    }

    public List<String> getDatasourceIds() {
        return datasourceIds;
    }

    public void setDatasourceIds(List<String> datasourceIds) {
        this.datasourceIds = datasourceIds;
    }

    /**
     * Gets the Spark script.
     *
     * @return the Spark script
     */
    public String getDataTransformScript() {
        return dataTransformScript;
    }

    /**
     * Sets the Spark script.
     *
     * @param dataTransformScript the Spark script
     */
    public void setDataTransformScript(String dataTransformScript) {
        this.dataTransformScript = dataTransformScript;
    }

    /**
     * Gets the SQL query.
     *
     * @return the SQL query
     */
    public String getSql() {
        return sql;
    }

    /**
     * Sets the SQL query.
     *
     * @param sql the SQL query
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * Gets the Spark Shell states.
     *
     * @return the Spark Shell states
     */
    public List<Map<String, Object>> getStates() {
        return states;
    }

    /**
     * Sets the Spark Shell states.
     *
     * @param states the Spark Shell states
     */
    public void setStates(List<Map<String, Object>> states) {
        this.states = states;
    }

    @JsonIgnore
    public Set<String> getTableNamesFromViewModel() {
        Set<String> tables = new HashSet<>();
        if (chartViewModel != null) {
            Collection<Map<String, Object>> nodes = (Collection<Map<String, Object>>) chartViewModel.get("nodes");
            if (nodes != null) {
                tables = nodes.stream().map(node -> (String) node.get("name")).collect(Collectors.toSet());
            }
        }
        return tables;
    }
}
