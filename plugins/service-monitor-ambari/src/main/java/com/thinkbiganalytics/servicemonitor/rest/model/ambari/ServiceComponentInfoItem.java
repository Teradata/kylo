package com.thinkbiganalytics.servicemonitor.rest.model.ambari;

/*-
 * #%L
 * thinkbig-service-monitor-ambari
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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
                       "ServiceComponentInfo",
                       "host_components"
                   })
public class ServiceComponentInfoItem {

    @JsonProperty("ServiceComponentInfo")
    private ServiceComponentInfo ServiceComponentInfo;
    @JsonProperty("host_components")
    private List<HostComponent> hostComponents = new ArrayList<HostComponent>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The ServiceComponentInfo
     */
    @JsonProperty("ServiceComponentInfo")
    public ServiceComponentInfo getServiceComponentInfo() {
        return ServiceComponentInfo;
    }

    /**
     * @param ServiceComponentInfo The ServiceComponentInfo
     */
    @JsonProperty("ServiceComponentInfo")
    public void setServiceComponentInfo(ServiceComponentInfo ServiceComponentInfo) {
        this.ServiceComponentInfo = ServiceComponentInfo;
    }

    /**
     * @return The hostComponents
     */
    @JsonProperty("host_components")
    public List<HostComponent> getHostComponents() {
        return hostComponents;
    }

    /**
     * @param hostComponents The host_components
     */
    @JsonProperty("host_components")
    public void setHostComponents(List<HostComponent> hostComponents) {
        this.hostComponents = hostComponents;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


    public void updateServiceComponentInfoState() {
        String state = "UNKNOWN";
        List<HostComponent> hostComponents = getHostComponents();
        if (hostComponents != null && !hostComponents.isEmpty()) {
            for (HostComponent hostComponent : hostComponents) {
                state = hostComponent.getHostRoles().getState();
                break;
            }
        }
        getServiceComponentInfo().setState(state);
    }

}
