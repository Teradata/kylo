
package com.thinkbiganalytics.jira.domain;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "startAt",
    "maxResults",
    "total",
    "worklogs"
})
public class Worklog {

    @JsonProperty("startAt")
    private Integer startAt;
    @JsonProperty("maxResults")
    private Integer maxResults;
    @JsonProperty("total")
    private Integer total;
    @JsonProperty("worklogs")
    private List<Object> worklogs = new ArrayList<Object>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The startAt
     */
    @JsonProperty("startAt")
    public Integer getStartAt() {
        return startAt;
    }

    /**
     * 
     * @param startAt
     *     The startAt
     */
    @JsonProperty("startAt")
    public void setStartAt(Integer startAt) {
        this.startAt = startAt;
    }

    /**
     * 
     * @return
     *     The maxResults
     */
    @JsonProperty("maxResults")
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * 
     * @param maxResults
     *     The maxResults
     */
    @JsonProperty("maxResults")
    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * 
     * @return
     *     The total
     */
    @JsonProperty("total")
    public Integer getTotal() {
        return total;
    }

    /**
     * 
     * @param total
     *     The total
     */
    @JsonProperty("total")
    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     * 
     * @return
     *     The worklogs
     */
    @JsonProperty("worklogs")
    public List<Object> getWorklogs() {
        return worklogs;
    }

    /**
     * 
     * @param worklogs
     *     The worklogs
     */
    @JsonProperty("worklogs")
    public void setWorklogs(List<Object> worklogs) {
        this.worklogs = worklogs;
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
