package com.thinkbiganalytics.metadata.rest.model.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * Created by sr186054 on 11/15/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceDefinition {

    public enum ConnectionType {
        SOURCE, DESTINATION;
    }

    private String processorType;

    private ConnectionType connectionType;


    private Set<String> datasourcePropertyKeys;

    private String datasourceType;

    private String identityString;

    private String description;

    private String title;



    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public Set<String> getDatasourcePropertyKeys() {
        return datasourcePropertyKeys;
    }

    public void setDatasourcePropertyKeys(Set<String> datasourcePropertyKeys) {
        this.datasourcePropertyKeys = datasourcePropertyKeys;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public void setDatasourceType(String datasourceType) {
        this.datasourceType = datasourceType;
    }

    public String getIdentityString() {
        return identityString;
    }

    public void setIdentityString(String identityString) {
        this.identityString = identityString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return StringUtils.isBlank(title) ? identityString : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
