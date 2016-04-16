package com.thinkbiganalytics.servicemonitor.rest.model.ambari;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "HostRoles"
})
public class HostComponent {

    @JsonProperty("HostRoles")
    private HostRoles HostRoles;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The HostRoles
     */
    @JsonProperty("HostRoles")
    public HostRoles getHostRoles() {
        return HostRoles;
    }

    /**
     *
     * @param HostRoles
     * The HostRoles
     */
    @JsonProperty("HostRoles")
    public void setHostRoles(HostRoles HostRoles) {
        this.HostRoles = HostRoles;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
