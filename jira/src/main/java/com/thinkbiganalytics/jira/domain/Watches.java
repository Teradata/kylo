
package com.thinkbiganalytics.jira.domain;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "self",
    "watchCount",
    "isWatching"
})
public class Watches {

    @JsonProperty("self")
    private String self;
    @JsonProperty("watchCount")
    private Integer watchCount;
    @JsonProperty("isWatching")
    private Boolean isWatching;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The self
     */
    @JsonProperty("self")
    public String getSelf() {
        return self;
    }

    /**
     * 
     * @param self
     *     The self
     */
    @JsonProperty("self")
    public void setSelf(String self) {
        this.self = self;
    }

    /**
     * 
     * @return
     *     The watchCount
     */
    @JsonProperty("watchCount")
    public Integer getWatchCount() {
        return watchCount;
    }

    /**
     * 
     * @param watchCount
     *     The watchCount
     */
    @JsonProperty("watchCount")
    public void setWatchCount(Integer watchCount) {
        this.watchCount = watchCount;
    }

    /**
     * 
     * @return
     *     The isWatching
     */
    @JsonProperty("isWatching")
    public Boolean getIsWatching() {
        return isWatching;
    }

    /**
     * 
     * @param isWatching
     *     The isWatching
     */
    @JsonProperty("isWatching")
    public void setIsWatching(Boolean isWatching) {
        this.isWatching = isWatching;
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
