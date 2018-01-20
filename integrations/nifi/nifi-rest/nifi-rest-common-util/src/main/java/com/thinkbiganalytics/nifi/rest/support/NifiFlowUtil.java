package com.thinkbiganalytics.nifi.rest.support;
/*-
 * #%L
 * thinkbig-nifi-rest-common-util
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
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.flow.FlowDTO;

import java.util.stream.Collectors;

/**
 * Created by sr186054 on 12/9/17.
 */
public class NifiFlowUtil {

    /**
     * Converts a FlowDTO to a FlowSnippet DTO,
     *
     * NOTE the Controller Services in the FlowSnippet will be lost as they are not part of a FlowDTO
     * @param flowDTO
     * @return
     */
    public static FlowSnippetDTO flowToFlowSnippet(FlowDTO flowDTO){
        FlowSnippetDTO flowSnippetDTO = new FlowSnippetDTO();
        flowSnippetDTO.setConnections(flowDTO.getConnections().stream().map(connectionEntity -> connectionEntity.getComponent()).collect(Collectors.toSet()));
        flowSnippetDTO.setFunnels(flowDTO.getFunnels().stream().map(funnelEntity -> funnelEntity.getComponent()).collect(Collectors.toSet()));
        flowSnippetDTO.setInputPorts(flowDTO.getInputPorts().stream().map(portEntity -> portEntity.getComponent()).collect(Collectors.toSet()));
        flowSnippetDTO.setOutputPorts(flowDTO.getOutputPorts().stream().map(portEntity -> portEntity.getComponent()).collect(Collectors.toSet()));
        flowSnippetDTO.setProcessors(flowDTO.getProcessors().stream().map(processorEntity -> processorEntity.getComponent()).collect(Collectors.toSet()));
        flowSnippetDTO.setProcessGroups(flowDTO.getProcessGroups().stream().map(processGroupEntity -> processGroupEntity.getComponent()).collect(Collectors.toSet()));
        flowSnippetDTO.setRemoteProcessGroups(flowDTO.getRemoteProcessGroups().stream().map(remoteProcessGroupEntity -> remoteProcessGroupEntity.getComponent()).collect(Collectors.toSet()));
        return flowSnippetDTO;
    }


}
