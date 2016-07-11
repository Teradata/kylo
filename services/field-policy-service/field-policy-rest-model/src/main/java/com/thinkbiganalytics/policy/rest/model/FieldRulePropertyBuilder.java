package com.thinkbiganalytics.policy.rest.model;


import com.thinkbiganalytics.rest.model.LabelValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 3/15/16.
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
    public static enum PROPERTY_TYPE {
        number,string,select,regex,date,chips
    }

    public FieldRulePropertyBuilder(String name){
        this.name = name;
        this.displayName = name;
        this.type = PROPERTY_TYPE.string.name();
        this.placeholder = "";
        this.hint = "";

    }

    public FieldRulePropertyBuilder displayName(String displayName){
        this.displayName = displayName;
        return this;
    }

    public FieldRulePropertyBuilder value(String value){
        this.value = value;
        return this;
    }

    public FieldRulePropertyBuilder placeholder(String placeholder){
        this.placeholder = placeholder;
        return this;
    }

    public FieldRulePropertyBuilder type(PROPERTY_TYPE type){
        this.type = type.name();
        return this;
    }

    public FieldRulePropertyBuilder hint(String hint){
        this.hint = hint;
        return this;
    }

    public FieldRulePropertyBuilder objectProperty(String objectProperty){
        this.objectProperty = objectProperty;
        return this;
    }

    public FieldRulePropertyBuilder addSelectableValues(List<LabelValue>labelValues){
        if(selectableValues == null){
            selectableValues = new ArrayList<>();
        }
        if(labelValues != null) {
            selectableValues.addAll(labelValues);
        }
        return this;
    }

    public FieldRulePropertyBuilder addSelectableValue(LabelValue labelValue){
        if(selectableValues == null){
            selectableValues = new ArrayList<>();
        }
        if(labelValue != null) {
            selectableValues.add(labelValue);
        }
        return this;
    }

    public FieldRulePropertyBuilder addSelectableValue(String label, String value){
        if(selectableValues == null){
            selectableValues = new ArrayList<>();
        }
        selectableValues.add(new LabelValue(label, value));
        return  this;
    }

    public FieldRuleProperty build(){
        FieldRuleProperty property = new FieldRuleProperty();
        property.setName(this.name);
        property.setDisplayName(this.displayName);
        property.setType(this.type);
        property.setHint(this.hint);
        property.setPlaceholder(this.placeholder);
        property.setSelectableValues(this.selectableValues);
        property.setObjectProperty(this.objectProperty);
        property.setValue(this.value);
        return property;
    }
}
