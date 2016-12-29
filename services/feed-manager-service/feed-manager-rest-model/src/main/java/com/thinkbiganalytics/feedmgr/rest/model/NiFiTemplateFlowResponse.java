package com.thinkbiganalytics.feedmgr.rest.model;

import java.util.List;

/**
 * Holds a list of ReusableConnectionInfo passed in as a POST Created by sr186054 on 12/29/16.
 */
public class NiFiTemplateFlowResponse {

    private NiFiTemplateFlowRequest request;

    private List<TemplateProcessorDatasourceDefinition> templateProcessorDatasourceDefinitions;

    /**
     * Returned set of pro
     */
    List<RegisteredTemplate.FlowProcessor> processors;


    public NiFiTemplateFlowResponse() {

    }


    public NiFiTemplateFlowRequest getRequest() {
        return request;
    }

    public void setRequest(NiFiTemplateFlowRequest request) {
        this.request = request;
    }

    public List<TemplateProcessorDatasourceDefinition> getTemplateProcessorDatasourceDefinitions() {
        return templateProcessorDatasourceDefinitions;
    }

    public void setTemplateProcessorDatasourceDefinitions(List<TemplateProcessorDatasourceDefinition> templateProcessorDatasourceDefinitions) {
        this.templateProcessorDatasourceDefinitions = templateProcessorDatasourceDefinitions;
    }

    public List<RegisteredTemplate.FlowProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(List<RegisteredTemplate.FlowProcessor> processors) {
        this.processors = processors;
    }
}
