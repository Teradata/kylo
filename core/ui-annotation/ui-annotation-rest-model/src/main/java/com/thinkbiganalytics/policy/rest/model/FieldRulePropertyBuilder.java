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


import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.rest.model.LabelValue;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for the {@link FieldRuleProperty}
 */
public class FieldRulePropertyBuilder {


    private String name;
    private String displayName;
    private String value;
    private String placeholder;
    private String type;
    private String hint;
    private String objectProperty;
    private List<LabelValue> selectableValues;
    private boolean required;
    private String group;
    private Integer groupOrder;
    private String layout;
    private boolean hidden;
    private String pattern;
    private String patternInvalidMessage;

    public FieldRulePropertyBuilder(String name) {
        this.name = name;
        this.displayName = name;
        this.type = PolicyPropertyTypes.PROPERTY_TYPE.string.name();
        this.placeholder = "";
        this.hint = "";

    }

    public FieldRulePropertyBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public FieldRulePropertyBuilder value(String value) {
        this.value = value;
        return this;
    }

    public FieldRulePropertyBuilder placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public FieldRulePropertyBuilder pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public FieldRulePropertyBuilder patternInvalidMessage(String patternInvalidMessage) {
        this.patternInvalidMessage = patternInvalidMessage;
        return this;
    }

    public FieldRulePropertyBuilder type(PolicyPropertyTypes.PROPERTY_TYPE type) {
        this.type = type.name();
        return this;
    }

    public FieldRulePropertyBuilder hint(String hint) {
        this.hint = hint;
        return this;
    }

    public FieldRulePropertyBuilder objectProperty(String objectProperty) {
        this.objectProperty = objectProperty;
        return this;
    }

    public FieldRulePropertyBuilder addSelectableValues(List<LabelValue> labelValues) {
        if (selectableValues == null) {
            selectableValues = new ArrayList<>();
        }
        if (labelValues != null) {
            selectableValues.addAll(labelValues);
        }
        return this;
    }

    public FieldRulePropertyBuilder required(boolean required) {
        this.required = required;
        return this;
    }

    public FieldRulePropertyBuilder group(String group) {
        this.group = group;
        return this;
    }

    public FieldRulePropertyBuilder groupOrder(Integer order) {
        this.groupOrder = order;
        return this;
    }

    public FieldRulePropertyBuilder hidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public FieldRulePropertyBuilder layout(String layout) {
        this.layout = layout;
        return this;
    }

    public FieldRulePropertyBuilder addSelectableValue(LabelValue labelValue) {
        if (selectableValues == null) {
            selectableValues = new ArrayList<>();
        }
        if (labelValue != null) {
            selectableValues.add(labelValue);
        }
        return this;
    }

    public FieldRulePropertyBuilder addSelectableValue(String label, String value) {
        if (selectableValues == null) {
            selectableValues = new ArrayList<>();
        }
        selectableValues.add(new LabelValue(label, value));
        return this;
    }


    public FieldRuleProperty build() {
        FieldRuleProperty property = new FieldRuleProperty();
        property.setName(this.name);
        property.setDisplayName(this.displayName);
        property.setType(this.type);
        property.setHint(this.hint);
        property.setPlaceholder(this.placeholder);
        property.setSelectableValues(this.selectableValues);
        property.setObjectProperty(this.objectProperty);
        property.setValue(this.value);
        property.setRequired(this.required);
        property.setGroup(this.group);
        property.setGroupOrder(this.groupOrder);
        property.setHidden(this.hidden);
        property.setPattern(pattern);
        String invalidPatternMessage = StringUtils.isNotBlank(patternInvalidMessage) ? patternInvalidMessage : "Invalid Input";
        property.setPatternInvalidMessage(invalidPatternMessage);
        return property;
    }
}
