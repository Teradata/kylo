package com.thinkbiganalytics.nifi.feedmgr;
/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 9/14/17.
 */
public class TemplateInstance {

    Map<ControllerServiceDTO, ControllerServiceDTO> scopeToRootMap;
    Map<String, String> scopeIdToRootIdMap = new HashMap<>();
    Map<String, String> rootIdToScopeIdMap = new HashMap<>();

    Map<String,ControllerServiceDTO>deletedServicesToMatchingRootService = new HashMap<>();

    FlowSnippetDTO flowSnippetDTO;

    public TemplateInstance(FlowSnippetDTO flowSnippetDTO){
        this.flowSnippetDTO = flowSnippetDTO;
    }

    public Set<ControllerServiceDTO> getDeletedScopedServices() {
       return getScopeToRootMap().keySet();
    }

    public Set<ControllerServiceDTO> getCreatedServices() {
        return new HashSet<>(getScopeToRootMap().values());
    }



    public void movedScopedControllerService(ControllerServiceDTO scopedService, ControllerServiceDTO rootService){
        getScopeToRootMap().put(scopedService,rootService);
        scopeIdToRootIdMap.put(scopedService.getId(),rootService.getId());
        rootIdToScopeIdMap.put(rootService.getId(),scopedService.getId());
    }

    public FlowSnippetDTO getFlowSnippetDTO() {
        return flowSnippetDTO;
    }

    public Map<ControllerServiceDTO, ControllerServiceDTO> getScopeToRootMap() {
        if(scopeToRootMap == null){
            scopeToRootMap  = new HashMap<>();
        }
        return scopeToRootMap;
    }

    public void addDeletedServiceMapping(String deletedServiceId, List<ControllerServiceDTO> rootServices ) {
        if(rootServices != null && !rootServices.isEmpty()) {
          ControllerServiceDTO rootService =  rootServices.stream().filter(cs -> NifiProcessUtil.SERVICE_STATE.ENABLED.name().equalsIgnoreCase(cs.getState())).findFirst().orElse(rootServices.get(0));
            deletedServicesToMatchingRootService.put(deletedServiceId,rootService);
            if(scopeIdToRootIdMap.containsKey(deletedServiceId)){
                deletedServicesToMatchingRootService.put(scopeIdToRootIdMap.get(deletedServiceId),rootService);
            }
            if(rootIdToScopeIdMap.containsKey(deletedServiceId)){
                deletedServicesToMatchingRootService.put(rootIdToScopeIdMap.get(deletedServiceId),rootService);
            }
        }
    }

    public boolean isDeletedRootService(String serviceId){
        return deletedServicesToMatchingRootService.containsKey(serviceId);
    }

    /**
     * Find the map of original CS to New Root Level CS for all those CS that were created for this template instance
     * @return Map of original CS to newly created root level cs
     */
    public Map<ControllerServiceDTO, ControllerServiceDTO> getCreatedScopeToRootMap() {
        if(scopeToRootMap == null){
            scopeToRootMap  = new HashMap<>();
        }
        return scopeToRootMap.entrySet().stream().filter(e -> !isDeletedRootService(e.getKey().getId())).collect(Collectors.toMap(e->e.getKey(),e->e.getValue()));
    }

    public ControllerServiceDTO findMatchingControllerService(String serviceId) {
        return deletedServicesToMatchingRootService.get(serviceId);
    }
}
