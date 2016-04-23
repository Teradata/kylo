package com.thinkbiganalytics.policy.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.rest.model.LabelValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 2/11/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldRuleProperty {
    //{name:"Date Format",value:"",placeholder:"",type:"string",hint:'Format Example: MM/DD/YYYY'}

    private String name;
    private String displayName;
    private String value;
    private String placeholder;
    private String type;
    private String hint;
    private String objectProperty;
    private List<LabelValue> selectableValues;

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
}
