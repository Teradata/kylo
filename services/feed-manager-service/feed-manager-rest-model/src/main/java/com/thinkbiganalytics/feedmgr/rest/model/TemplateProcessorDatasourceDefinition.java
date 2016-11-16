package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;

/**
 * Created by sr186054 on 11/15/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateProcessorDatasourceDefinition {

    private String processorType;
    private String processorName;
    private String processorId;
    private DatasourceDefinition datasourceDefinition;

    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public DatasourceDefinition getDatasourceDefinition() {
        return datasourceDefinition;
    }

    public void setDatasourceDefinition(DatasourceDefinition datasourceDefinition) {
        this.datasourceDefinition = datasourceDefinition;
    }
}
