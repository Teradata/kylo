package com.thinkbiganalytics.jira.domain;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 10/16/15.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "expand",
        "projects"
})
public class CreateMeta {

    @JsonProperty("expand")
    private String expand;
    @JsonProperty("projects")
    private List<Project> projects = new ArrayList<Project>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The expand
     */
    @JsonProperty("expand")
    public String getExpand() {
        return expand;
    }

    /**
     *
     * @param expand
     * The expand
     */
    @JsonProperty("expand")
    public void setExpand(String expand) {
        this.expand = expand;
    }

    /**
     *
     * @return
     * The projects
     */
    @JsonProperty("projects")
    public List<Project> getProjects() {
        return projects;
    }

    /**
     *
     * @param projects
     * The projects
     */
    @JsonProperty("projects")
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Project getProject(){
        if(projects != null && !projects.isEmpty()) {
            return projects.get(0);
        }
        return null;
    }

}