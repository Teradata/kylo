/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = DatasourceUpdatedSinceMetric.class)})
public abstract class DependentDatasourceMetric extends Metric {

    private String datasourceId;
    private String datasourceName;

    public String getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(String testedFeed) {
        this.datasourceId = testedFeed;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

}
