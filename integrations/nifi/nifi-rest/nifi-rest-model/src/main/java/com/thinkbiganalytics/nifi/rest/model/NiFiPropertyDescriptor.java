package com.thinkbiganalytics.nifi.rest.model;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

import java.util.List;

/**
 * A description of a property.
 */
public class NiFiPropertyDescriptor {

    private String name;
    private String displayName;
    private String description;
    private String defaultValue;
    private List<NiFiAllowableValue> allowableValues;
    private Boolean required;
    private Boolean sensitive;
    private Boolean dynamic;
    private Boolean supportsEl;
    private String identifiesControllerService;

    /**
     * @return set of allowable values for this property
     */
    public List<NiFiAllowableValue> getAllowableValues() {
        return allowableValues;
    }

    public void setAllowableValues(List<NiFiAllowableValue> allowableValues) {
        this.allowableValues = allowableValues;
    }

    /**
     * @return default value for this property
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return an explanation of the meaning of the given property
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return property name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return human-readable name to display to users
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return whether the property is required for this processor
     */
    public Boolean isRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @return indicates that the value for this property should be considered sensitive and protected whenever stored or represented
     */
    public Boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(Boolean sensitive) {
        this.sensitive = sensitive;
    }

    /**
     * @return indicates whether this property is dynamic
     */
    public Boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**
     * @return specifies whether or not this property supports the expression language
     */
    public Boolean getSupportsEl() {
        return supportsEl;
    }

    public void setSupportsEl(Boolean supportsEl) {
        this.supportsEl = supportsEl;
    }

    /**
     * @return the fully qualified type if this property identifies a controller service
     */
    public String getIdentifiesControllerService() {
        return identifiesControllerService;
    }

    public void setIdentifiesControllerService(String identifiesControllerService) {
        this.identifiesControllerService = identifiesControllerService;
    }
}
