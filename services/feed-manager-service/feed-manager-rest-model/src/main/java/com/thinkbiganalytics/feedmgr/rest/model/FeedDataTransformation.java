package com.thinkbiganalytics.feedmgr.rest.model;

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

    /** Model for the flowchart */
    private Map<String, Object> chartViewModel;

    /** Spark script */
    @MetadataField(description = "The Data Transformation Spark Script")
    private String dataTransformScript;

    /** SQL query */
    private String sql;

    /** List of Spark Shell states */
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
    public Set<String> getTableNamesFromViewModel(){
        Set<String> tables = new HashSet<>();
        if(chartViewModel != null){
            Collection<Map<String,Object>> nodes = (Collection<Map<String,Object>>)chartViewModel.get("nodes");
            if(nodes != null){
                tables = nodes.stream().map(node -> (String)node.get("name")).collect(Collectors.toSet());
            }
        }
        return tables;
    }
}
