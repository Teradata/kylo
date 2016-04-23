
package com.thinkbiganalytics.jira.domain;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.MoreObjects;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "self",
    "id",
    "key",
    "name",
    "avatarUrls",
    "projectCategory"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    @JsonProperty("self")
    private String self;
    @JsonProperty("id")
    private String id;
    @JsonProperty("key")
    private String key;
    @JsonProperty("name")
    private String name;
    @JsonProperty("avatarUrls")
    private AvatarUrls avatarUrls;
    @JsonProperty("projectCategory")
    private ProjectCategory projectCategory;
    @JsonProperty("issuetypes")
    private List<IssueType> issueTypes;

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
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The key
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     * 
     * @param key
     *     The key
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The avatarUrls
     */
    @JsonProperty("avatarUrls")
    public AvatarUrls getAvatarUrls() {
        return avatarUrls;
    }

    /**
     * 
     * @param avatarUrls
     *     The avatarUrls
     */
    @JsonProperty("avatarUrls")
    public void setAvatarUrls(AvatarUrls avatarUrls) {
        this.avatarUrls = avatarUrls;
    }

    /**
     * 
     * @return
     *     The projectCategory
     */
    @JsonProperty("projectCategory")
    public ProjectCategory getProjectCategory() {
        return projectCategory;
    }


    @JsonProperty("issuetypes")
    public List<IssueType> getIssueTypes() {
        return issueTypes;
    }

    @JsonProperty("issuetypes")
    public void setIssueTypes(List<IssueType> issueTypes) {
        this.issueTypes = issueTypes;
    }

    /**
     * 
     * @param projectCategory
     *     The projectCategory
     */
    @JsonProperty("projectCategory")
    public void setProjectCategory(ProjectCategory projectCategory) {
        this.projectCategory = projectCategory;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


    @Override
    public String toString() {
        return getToStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper getToStringHelper() {
        return MoreObjects.toStringHelper(this).
                add("key", key);
    }
}
