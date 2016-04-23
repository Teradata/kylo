package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 3/15/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericUIPrecondition  implements UIPrecondition{

    //property on generic ui precondition that will resolve to systemCategoryName.systemFeedName
    public static String DEPENDENT_FEED_NAME_PROPERTY = "feedName";

    private String feedName;
    private String name;
    private String type;
    private String displayName;
    private String description;
    private List<FieldRuleProperty> properties;


    public  GenericUIPrecondition(){
        this.properties = new ArrayList<>();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addProperty(FieldRuleProperty property){
        getProperties().add(property);
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }


    @JsonIgnore
    public  String getDependentFeedName(){
        if(this.getProperty(GenericUIPrecondition.DEPENDENT_FEED_NAME_PROPERTY) != null) {
            return   this.getProperty(GenericUIPrecondition.DEPENDENT_FEED_NAME_PROPERTY).getValue();
        }
        return null;
    }




    @JsonIgnore
    public FieldRuleProperty getProperty(final String name){
        return Iterables.tryFind(properties, new Predicate<FieldRuleProperty>() {
            @Override
            public boolean apply(FieldRuleProperty fieldRuleProperty) {
                return fieldRuleProperty.getName().equalsIgnoreCase(name);
            }
        }).orNull();
    }
}
