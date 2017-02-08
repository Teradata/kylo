package com.thinkbiganalytics.jira.domain;

/*-
 * #%L
 * thinkbig-jira-rest-client
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

/**
 */

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
                       "baseUrl",
                       "version",
                       "versionNumbers",
                       "buildNumber",
                       "buildDate",
                       "serverTime",
                       "scmInfo",
                       "serverTitle"
                   })
public class ServerInfo {

    @JsonProperty("baseUrl")
    private String baseUrl;
    @JsonProperty("version")
    private String version;
    @JsonProperty("versionNumbers")
    private List<Integer> versionNumbers = new ArrayList<Integer>();
    @JsonProperty("buildNumber")
    private Integer buildNumber;
    @JsonProperty("buildDate")
    private String buildDate;
    @JsonProperty("serverTime")
    private String serverTime;
    @JsonProperty("scmInfo")
    private String scmInfo;
    @JsonProperty("serverTitle")
    private String serverTitle;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The baseUrl
     */
    @JsonProperty("baseUrl")
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param baseUrl The baseUrl
     */
    @JsonProperty("baseUrl")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return The version
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return The versionNumbers
     */
    @JsonProperty("versionNumbers")
    public List<Integer> getVersionNumbers() {
        return versionNumbers;
    }

    /**
     * @param versionNumbers The versionNumbers
     */
    @JsonProperty("versionNumbers")
    public void setVersionNumbers(List<Integer> versionNumbers) {
        this.versionNumbers = versionNumbers;
    }

    /**
     * @return The buildNumber
     */
    @JsonProperty("buildNumber")
    public Integer getBuildNumber() {
        return buildNumber;
    }

    /**
     * @param buildNumber The buildNumber
     */
    @JsonProperty("buildNumber")
    public void setBuildNumber(Integer buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * @return The buildDate
     */
    @JsonProperty("buildDate")
    public String getBuildDate() {
        return buildDate;
    }

    /**
     * @param buildDate The buildDate
     */
    @JsonProperty("buildDate")
    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    /**
     * @return The serverTime
     */
    @JsonProperty("serverTime")
    public String getServerTime() {
        return serverTime;
    }

    /**
     * @param serverTime The serverTime
     */
    @JsonProperty("serverTime")
    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    /**
     * @return The scmInfo
     */
    @JsonProperty("scmInfo")
    public String getScmInfo() {
        return scmInfo;
    }

    /**
     * @param scmInfo The scmInfo
     */
    @JsonProperty("scmInfo")
    public void setScmInfo(String scmInfo) {
        this.scmInfo = scmInfo;
    }

    /**
     * @return The serverTitle
     */
    @JsonProperty("serverTitle")
    public String getServerTitle() {
        return serverTitle;
    }

    /**
     * @param serverTitle The serverTitle
     */
    @JsonProperty("serverTitle")
    public void setServerTitle(String serverTitle) {
        this.serverTitle = serverTitle;
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
