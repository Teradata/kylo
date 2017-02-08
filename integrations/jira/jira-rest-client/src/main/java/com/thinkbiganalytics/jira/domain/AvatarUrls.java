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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
                       "48x48",
                       "24x24",
                       "16x16",
                       "32x32"
                   })
public class AvatarUrls {

    @JsonProperty("48x48")
    private String _48x48;
    @JsonProperty("24x24")
    private String _24x24;
    @JsonProperty("16x16")
    private String _16x16;
    @JsonProperty("32x32")
    private String _32x32;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The _48x48
     */
    @JsonProperty("48x48")
    public String get48x48() {
        return _48x48;
    }

    /**
     * @param _48x48 The 48x48
     */
    @JsonProperty("48x48")
    public void set48x48(String _48x48) {
        this._48x48 = _48x48;
    }

    /**
     * @return The _24x24
     */
    @JsonProperty("24x24")
    public String get24x24() {
        return _24x24;
    }

    /**
     * @param _24x24 The 24x24
     */
    @JsonProperty("24x24")
    public void set24x24(String _24x24) {
        this._24x24 = _24x24;
    }

    /**
     * @return The _16x16
     */
    @JsonProperty("16x16")
    public String get16x16() {
        return _16x16;
    }

    /**
     * @param _16x16 The 16x16
     */
    @JsonProperty("16x16")
    public void set16x16(String _16x16) {
        this._16x16 = _16x16;
    }

    /**
     * @return The _32x32
     */
    @JsonProperty("32x32")
    public String get32x32() {
        return _32x32;
    }

    /**
     * @param _32x32 The 32x32
     */
    @JsonProperty("32x32")
    public void set32x32(String _32x32) {
        this._32x32 = _32x32;
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
