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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.rest.model.LabelValue;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A user interface object representing a field property
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldRuleProperty {

    private String name;
    private String displayName;
    private String value;
    private List<LabelValue> values;
    private String placeholder;
    private String type;
    private String hint;
    private String objectProperty;
    private List<LabelValue> selectableValues;
    private boolean required;
    private String group;
    private Integer groupOrder;
    private String layout = "column";
    private boolean hidden;
    private String pattern;
    private String patternInvalidMessage;


    public FieldRuleProperty() {

    }


    /**
     * Strip out anything not needed for serialization
     */
    @JsonIgnore
    public void simplifyForSerialization() {
        this.displayName = null;
        this.placeholder = null;
        this.hint = null;
        this.type = null;
        this.selectableValues = null;
        this.group = null;
        this.groupOrder = null;
        this.layout = null;
        this.pattern = null;
        this.patternInvalidMessage = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public List<LabelValue> getSelectableValues() {

        if (selectableValues == null) {
            selectableValues = new ArrayList<>();
        }
        return selectableValues;
    }

    public void setSelectableValues(List<LabelValue> selectableValues) {
        this.selectableValues = selectableValues;
    }

    public void addSelectableValue(LabelValue labelValue) {
        getSelectableValues().add(labelValue);
    }

    public void addSelectableValue(String label, String value) {
        getSelectableValues().add(new LabelValue(label, value));
    }

    public String getObjectProperty() {
        return objectProperty;
    }

    public void setObjectProperty(String objectProperty) {
        this.objectProperty = objectProperty;
    }

    public List<LabelValue> getValues() {
        return values;
    }

    public void setValues(List<LabelValue> values) {
        this.values = values;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @JsonIgnore
    public String getStringValue() {
        if (StringUtils.isBlank(value) && values != null && !values.isEmpty()) {
            //join the values into a string comma separated
            StringBuffer sb = new StringBuffer();
            for (LabelValue lv : getValues()) {
                if (!sb.toString().equalsIgnoreCase("")) {
                    sb.append(",");
                }
                sb.append(lv.getValue());
            }
            return sb.toString();
        } else {
            return value;
        }
    }


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getGroupOrder() {
        return groupOrder;
    }

    public void setGroupOrder(Integer groupOrder) {
        this.groupOrder = groupOrder;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPatternInvalidMessage() {
        return patternInvalidMessage;
    }

    public void setPatternInvalidMessage(String patternInvalidMessage) {
        this.patternInvalidMessage = patternInvalidMessage;
    }
}
