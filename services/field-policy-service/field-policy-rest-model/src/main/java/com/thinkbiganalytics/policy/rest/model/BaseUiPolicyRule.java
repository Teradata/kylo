package com.thinkbiganalytics.policy.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public class BaseUiPolicyRule {
  private String name;
  private String displayName;
  private String description;
  private String shortDescription;
  private List<FieldRuleProperty> properties;
  private String objectClassType;
  private String propertyValuesDisplayString;

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
}
