package com.thinkbiganalytics.policy.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.rest.model.LabelValue;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 2/11/16.
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

        if(selectableValues == null){
            selectableValues = new ArrayList<>();
        }
        return selectableValues;
    }

    public void setSelectableValues(List<LabelValue> selectableValues) {
        this.selectableValues = selectableValues;
    }

    public void addSelectableValue(LabelValue labelValue){
        getSelectableValues().add(labelValue);
    }

    public void addSelectableValue(String label, String value){
        getSelectableValues().add(new LabelValue(label,value));
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


}
