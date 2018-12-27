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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class NifiControllerServiceUtil {


    public Collection<ControllerServiceDTO> services;


    private Map<String, ControllerServiceDTO> serviceIdMap = new HashMap<>();

    private Map<String, List<ControllerServiceDTO>> serviceNameMap = new HashMap<>();

    private Map<String, List<ControllerServiceDTO>> enabledServiceNameMap = new HashMap<>();

    public NifiControllerServiceUtil(Collection<ControllerServiceDTO> services) {
        this.services = services;
        this.init();
    }

    public void init() {
        //Create a map of the Controller Service Name to list of matching services

        this.serviceIdMap = services.stream().collect(Collectors.toMap(service -> service.getId(),service -> service));

        this.serviceNameMap = services.stream()
            .collect(Collectors.groupingBy(cs -> cs.getName()));

        this.enabledServiceNameMap = services.stream()
            .filter(cs -> NifiProcessUtil.SERVICE_STATE.ENABLED.name().equalsIgnoreCase(cs.getState()))
            .collect(Collectors.groupingBy(cs -> cs.getName()));
    }

    private boolean hasMatchingService(Map<String, List<ControllerServiceDTO>> nameMap, String name) {
        return nameMap.containsKey(name) && !nameMap.get(name).isEmpty();
    }

    @Nullable
    public String getValidControllerServiceId(String name) {
        String
            validControllerServiceId = hasMatchingService(enabledServiceNameMap, name) ? enabledServiceNameMap.get(name).get(0).getId()
                                                                                       : hasMatchingService(serviceNameMap, name) ? serviceNameMap.get(name).get(0).getId()
                                                                                                                                  : null;
        return validControllerServiceId;
    }

    @Nullable
    public ControllerServiceDTO getControllerServiceForId(String id) {
        return serviceIdMap.get(id);
    }

    public boolean hasMatchingService(String id){
        return serviceIdMap.containsKey(id);
    }

    @Nullable
    public String getValidControllerServiceId(String serviceId, String serviceName){
        if (this.hasMatchingService(serviceId)) {
            return serviceIdMap.get(serviceId).getId();
        }
        else {
            return getValidControllerServiceId(serviceName);
        }
    }
}
