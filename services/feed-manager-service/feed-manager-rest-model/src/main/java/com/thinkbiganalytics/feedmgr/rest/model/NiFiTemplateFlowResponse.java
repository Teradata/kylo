package com.thinkbiganalytics.feedmgr.rest.model;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds a list of ReusableConnectionInfo passed in as a POST Created by sr186054 on 12/29/16.
 */
public class NiFiTemplateFlowResponse {

    private List<KyloProcessorFlowTypeOption> processorFlowTypes;

    private boolean userDefinedFailureProcessors;

    private NiFiTemplateFlowRequest request;

    private List<TemplateProcessorDatasourceDefinition> templateProcessorDatasourceDefinitions;

    /**
     * Returned set of pro
     */
    List<RegisteredTemplate.FlowProcessor> processors;


    public NiFiTemplateFlowResponse() {
        processorFlowTypes =
            Lists.newArrayList(KyloProcessorFlowType.values()).stream().filter(flowType -> (!flowType.equals(KyloProcessorFlowType.NORMAL_FLOW) && !flowType.isDeprecated()))
                .map(flowType -> new KyloProcessorFlowTypeOption(flowType)).collect(Collectors.toList());
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

    public List<KyloProcessorFlowTypeOption> getProcessorFlowTypes() {
        return processorFlowTypes;
    }

    public void setProcessorFlowTypes(List<KyloProcessorFlowTypeOption> processorFlowTypes) {
        this.processorFlowTypes = processorFlowTypes;
    }

    public boolean isUserDefinedFailureProcessors() {
        return userDefinedFailureProcessors;
    }

    public void setUserDefinedFailureProcessors(boolean userDefinedFailureProcessors) {
        this.userDefinedFailureProcessors = userDefinedFailureProcessors;
    }

    public static class KyloProcessorFlowTypeOption {

        private String flowType;
        private String displayName;
        private String description;

        public KyloProcessorFlowTypeOption(KyloProcessorFlowType flowType) {
            this.flowType = flowType.name();
            this.displayName = flowType.getDisplayName();
            this.description = flowType.getDescription();
        }

        public String getFlowType() {
            return flowType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }


}
