package com.thinkbiganalytics.feedmgr.rest.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public class BaseUiPolicyRule {
  private String name;
  private String displayName;
  private String description;
  private List<FieldRuleProperty> properties;
  private String objectClassType;

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
    if(properties == null) {
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
  }

  @JsonIgnore
  public FieldRuleProperty getProperty(final String name){

    return Iterables.tryFind(getProperties(), new Predicate<FieldRuleProperty>() {
      @Override
      public boolean apply(FieldRuleProperty fieldRuleProperty) {
        return fieldRuleProperty.getName().equalsIgnoreCase(name);
      }
    }).orNull();
  }
}
