package com.thinkbiganalytics.servicemonitor.rest.model.ambari;

/**
 * Created by sr186054 on 10/12/15.
 */

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "href",
        "Clusters"
})
public class ClusterItem {

    @JsonProperty("href")
    private String href;
    @JsonProperty("Clusters")
    private Cluster cluster;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The href
     */
    @JsonProperty("href")
    public String getHref() {
        return href;
    }

    /**
     * @param href The href
     */
    @JsonProperty("href")
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @return The Clusters
     */
    @JsonProperty("Clusters")
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @param Clusters The Clusters
     */
    @JsonProperty("Clusters")
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
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