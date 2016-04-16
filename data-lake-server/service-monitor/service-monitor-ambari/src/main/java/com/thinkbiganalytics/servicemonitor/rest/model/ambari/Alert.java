package com.thinkbiganalytics.servicemonitor.rest.model.ambari;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "cluster_name",
        "component_name",
        "definition_id",
        "definition_name",
        "host_name",
        "id",
        "instance",
        "label",
        "latest_timestamp",
        "maintenance_state",
        "original_timestamp",
        "scope",
        "service_name",
        "state",
        "text"
})
public class Alert {

    @JsonProperty("cluster_name")
    private String clusterName;
    @JsonProperty("component_name")
    private String componentName;
    @JsonProperty("definition_id")
    private Integer definitionId;
    @JsonProperty("definition_name")
    private String definitionName;
    @JsonProperty("host_name")
    private String hostName;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("instance")
    private Object instance;
    @JsonProperty("label")
    private String label;
    @JsonProperty("latest_timestamp")
    private Long latestTimestamp;
    @JsonProperty("maintenance_state")
    private String maintenanceState;
    @JsonProperty("original_timestamp")
    private Long originalTimestamp;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("service_name")
    private String serviceName;
    @JsonProperty("state")
    private String state;
    @JsonProperty("text")
    private String text;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The clusterName
     */
    @JsonProperty("cluster_name")
    public String getClusterName() {
        return clusterName;
    }

    /**
     *
     * @param clusterName
     * The cluster_name
     */
    @JsonProperty("cluster_name")
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     *
     * @return
     * The componentName
     */
    @JsonProperty("component_name")
    public String getComponentName() {
        return componentName;
    }

    /**
     *
     * @param componentName
     * The component_name
     */
    @JsonProperty("component_name")
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     *
     * @return
     * The definitionId
     */
    @JsonProperty("definition_id")
    public Integer getDefinitionId() {
        return definitionId;
    }

    /**
     *
     * @param definitionId
     * The definition_id
     */
    @JsonProperty("definition_id")
    public void setDefinitionId(Integer definitionId) {
        this.definitionId = definitionId;
    }

    /**
     *
     * @return
     * The definitionName
     */
    @JsonProperty("definition_name")
    public String getDefinitionName() {
        return definitionName;
    }

    /**
     *
     * @param definitionName
     * The definition_name
     */
    @JsonProperty("definition_name")
    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    /**
     *
     * @return
     * The hostName
     */
    @JsonProperty("host_name")
    public String getHostName() {
        return hostName;
    }

    /**
     *
     * @param hostName
     * The host_name
     */
    @JsonProperty("host_name")
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     *
     * @return
     * The id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The instance
     */
    @JsonProperty("instance")
    public Object getInstance() {
        return instance;
    }

    /**
     *
     * @param instance
     * The instance
     */
    @JsonProperty("instance")
    public void setInstance(Object instance) {
        this.instance = instance;
    }

    /**
     *
     * @return
     * The label
     */
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     * The label
     */
    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *
     * @return
     * The latestTimestamp
     */
    @JsonProperty("latest_timestamp")
    public Long getLatestTimestamp() {
        return latestTimestamp;
    }

    /**
     *
     * @param latestTimestamp
     * The latest_timestamp
     */
    @JsonProperty("latest_timestamp")
    public void setLatestTimestamp(Long latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }

    /**
     *
     * @return
     * The maintenanceState
     */
    @JsonProperty("maintenance_state")
    public String getMaintenanceState() {
        return maintenanceState;
    }

    /**
     *
     * @param maintenanceState
     * The maintenance_state
     */
    @JsonProperty("maintenance_state")
    public void setMaintenanceState(String maintenanceState) {
        this.maintenanceState = maintenanceState;
    }

    /**
     *
     * @return
     * The originalTimestamp
     */
    @JsonProperty("original_timestamp")
    public Long getOriginalTimestamp() {
        return originalTimestamp;
    }

    /**
     *
     * @param originalTimestamp
     * The original_timestamp
     */
    @JsonProperty("original_timestamp")
    public void setOriginalTimestamp(Long originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
    }

    /**
     *
     * @return
     * The scope
     */
    @JsonProperty("scope")
    public String getScope() {
        return scope;
    }

    /**
     *
     * @param scope
     * The scope
     */
    @JsonProperty("scope")
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     *
     * @return
     * The serviceName
     */
    @JsonProperty("service_name")
    public String getServiceName() {
        return serviceName;
    }

    /**
     *
     * @param serviceName
     * The service_name
     */
    @JsonProperty("service_name")
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     *
     * @return
     * The state
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     *
     * @param state
     * The state
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    /**
     *
     * @return
     * The text
     */
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    /**
     *
     * @param text
     * The text
     */
    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
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