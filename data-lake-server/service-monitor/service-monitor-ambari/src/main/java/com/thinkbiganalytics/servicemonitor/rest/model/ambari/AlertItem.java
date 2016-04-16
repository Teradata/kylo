package com.thinkbiganalytics.servicemonitor.rest.model.ambari;

/**
 * Created by sr186054 on 10/2/15.
 */

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "Alert"
})
@JsonIgnoreProperties(ignoreUnknown=true)
public class AlertItem {

    @JsonProperty("Alert")
    private Alert Alert;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The Alert
     */
    @JsonProperty("Alert")
    public Alert getAlert() {
        return Alert;
    }

    /**
     *
     * @param Alert
     * The Alert
     */
    @JsonProperty("Alert")
    public void setAlert(Alert Alert) {
        this.Alert = Alert;
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