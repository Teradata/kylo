package com.thinkbiganalytics.metadata.rest.model.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Created by sr186054 on 11/15/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DerivedDatasource extends Datasource {

    public Map<String, Object> properties;

    private String datasourceType;

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public void setDatasourceType(String datasourceType) {
        this.datasourceType = datasourceType;
    }

}
