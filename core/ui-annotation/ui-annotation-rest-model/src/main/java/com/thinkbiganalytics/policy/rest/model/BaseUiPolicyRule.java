package com.thinkbiganalytics.policy.rest.model;

/*-
 * #%L
 * thinkbig-ui-annotation-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import org.apache.commons.lang3.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A base user interface policy rule
 */
public class BaseUiPolicyRule {

    private String name;
    private String displayName;
    private String description;
    private String shortDescription;
    private List<FieldRuleProperty> properties;
    private String objectClassType;
    private String objectShortClassType;
    private String propertyValuesDisplayString;
    private Integer sequence;

    public BaseUiPolicyRule() {

    }

    /**
     * strip out everything that is not needed when saving
     */
    @JsonIgnore
    public void simplifyForSerialization() {
        this.displayName = null;
        this.description = null;
        this.shortDescription = null;
        this.propertyValuesDisplayString = null;
        if (getProperties() != null) {
            for (FieldRuleProperty property : getProperties()) {
                property.simplifyForSerialization();
            }
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FieldRuleProperty> getProperties() {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        return properties;
    }

    public void setProperties(List<FieldRuleProperty> properties) {
        this.properties = properties;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getObjectClassType() {
        return objectClassType;
    }

    public void setObjectClassType(String objectClassType) {
        this.objectClassType = objectClassType;
        setObjectShortClassType(ClassUtils.getShortCanonicalName(objectClassType));
    }

    public String getObjectShortClassType() {
        return objectShortClassType;
    }

    public void setObjectShortClassType(String objectShortClassType) {
        this.objectShortClassType = objectShortClassType;
    }

    @JsonIgnore
    public FieldRuleProperty getProperty(final String name) {

        return Iterables.tryFind(getProperties(), new Predicate<FieldRuleProperty>() {
            @Override
            public boolean apply(FieldRuleProperty fieldRuleProperty) {
                return fieldRuleProperty.getName().equalsIgnoreCase(name);
            }
        }).orNull();
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getPropertyValuesDisplayString() {
        return propertyValuesDisplayString;
    }

    public void setPropertyValuesDisplayString(String propertyValuesDisplayString) {
        this.propertyValuesDisplayString = propertyValuesDisplayString;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    @JsonIgnore
    public void buildValueDisplayString() {
        StringBuffer sb = null;

        if (getProperties() != null) {
            for (FieldRuleProperty property : getProperties()) {
                if (!PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name().equalsIgnoreCase(property.getType())) {
                    //get the values
                    String value = property.getStringValue();
                    if (sb == null) {
                        sb = new StringBuffer();
                    } else {
                        sb.append(";");
                    }
                    sb.append(property.getName() + ": " + value);
                }
            }
        }
        if (sb != null) {
            setPropertyValuesDisplayString(sb.toString());
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseUiPolicyRule{");
        sb.append("name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
