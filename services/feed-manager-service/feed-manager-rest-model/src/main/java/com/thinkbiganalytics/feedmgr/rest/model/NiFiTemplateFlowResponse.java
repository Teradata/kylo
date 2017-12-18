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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.KyloProcessorFlowType;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class NiFiTemplateFlowResponse {

    /**
     * Returned set of processors
     */
    List<RegisteredTemplate.FlowProcessor> processors;

    private List<KyloProcessorFlowTypeOption> processorFlowTypes;
    private boolean userDefinedFailureProcessors;
    private NiFiTemplateFlowRequest request;
    private List<TemplateProcessorDatasourceDefinition> templateProcessorDatasourceDefinitions;


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
