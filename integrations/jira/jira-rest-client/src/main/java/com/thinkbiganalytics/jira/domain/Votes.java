
package com.thinkbiganalytics.jira.domain;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "self",
    "votes",
    "hasVoted"
})
public class Votes {

    @JsonProperty("self")
    private String self;
    @JsonProperty("votes")
    private Integer votes;
    @JsonProperty("hasVoted")
    private Boolean hasVoted;
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
     *     The votes
     */
    @JsonProperty("votes")
    public Integer getVotes() {
        return votes;
    }

    /**
     * 
     * @param votes
     *     The votes
     */
    @JsonProperty("votes")
    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    /**
     * 
     * @return
     *     The hasVoted
     */
    @JsonProperty("hasVoted")
    public Boolean getHasVoted() {
        return hasVoted;
    }

    /**
     * 
     * @param hasVoted
     *     The hasVoted
     */
    @JsonProperty("hasVoted")
    public void setHasVoted(Boolean hasVoted) {
        this.hasVoted = hasVoted;
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
