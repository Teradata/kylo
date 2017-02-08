package com.thinkbiganalytics.servicemonitor.rest.model.ambari;

/*-
 * #%L
 * thinkbig-service-monitor-ambari
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
                       "category",
                       "cluster_name",
                       "component_name",
                       "installed_count",
                       "service_name",
                       "started_count",
                       "total_count"
                   })
public class ServiceComponentInfo {

    @JsonProperty("category")
    private String category;
    @JsonProperty("cluster_name")
    private String clusterName;
    @JsonProperty("component_name")
    private String componentName;
    @JsonProperty("installed_count")
    private Integer installedCount;
    @JsonProperty("service_name")
    private String serviceName;
    @JsonProperty("started_count")
    private Integer startedCount;
    @JsonProperty("state")
    private String state;
    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The category
     */
    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    /**
     * @param category The category
     */
    @JsonProperty("category")
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return The clusterName
     */
    @JsonProperty("cluster_name")
    public String getClusterName() {
        return clusterName;
    }

    /**
     * @param clusterName The cluster_name
     */
    @JsonProperty("cluster_name")
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * @return The componentName
     */
    @JsonProperty("component_name")
    public String getComponentName() {
        return componentName;
    }

    /**
     * @param componentName The component_name
     */
    @JsonProperty("component_name")
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * @return The installedCount
     */
    @JsonProperty("installed_count")
    public Integer getInstalledCount() {
        return installedCount;
    }

    /**
     * @param installedCount The installed_count
     */
    @JsonProperty("installed_count")
    public void setInstalledCount(Integer installedCount) {
        this.installedCount = installedCount;
    }

    /**
     * @return The serviceName
     */
    @JsonProperty("service_name")
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName The service_name
     */
    @JsonProperty("service_name")
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return The startedCount
     */
    @JsonProperty("started_count")
    public Integer getStartedCount() {
        return startedCount;
    }

    /**
     * @param startedCount The started_count
     */
    @JsonProperty("started_count")
    public void setStartedCount(Integer startedCount) {
        this.startedCount = startedCount;
    }

    /**
     * @return The state.. TODO Revisit if clustered with component on Multiple Hosts...
     */
    @JsonProperty("state")
    public String getState() {
        return this.state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return The totalCount
     */
    @JsonProperty("total_count")
    public Integer getTotalCount() {
        return totalCount;
    }

    /**
     * @param totalCount The total_count
     */
    @JsonProperty("total_count")
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
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
