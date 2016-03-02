/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 *
 * @author Sean Felten
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DatasourceUpdatedSinceMetric.class),
    }
)
public abstract class DependentDatasetMetric extends Metric {
    
    private String datasourceName;

    public DependentDatasetMetric(String datasetName) {
        super();
        this.datasourceName = datasetName;
    }

    public String getDatasourceName() {
        return datasourceName;
    }
}
