package com.thinkbiganalytics.servicemonitor.rest.model.ambari;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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