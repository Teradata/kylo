package com.thinkbiganalytics.feedmgr.rest.model.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public abstract class BasePolicyRuleBuilder<T, B extends BasePolicyRuleBuilder> {

  protected String name;
  protected String displayName;
  protected String description;
  protected List<FieldRuleProperty> properties;
  protected String objectClassType;

  public BasePolicyRuleBuilder(String name){
    this.name = name;
    this.displayName = name;
  this.properties = new ArrayList<>();

  }

  public B displayName(String displayName){
    this.displayName = displayName;
    return (B)this;
  }

  public B description(String description){
    this.description = description;
    return (B) this;
  }

  public B addProperty(FieldRuleProperty property){
    this.properties.add(property);
    return (B)this;
  }
  public B addProperties(List<FieldRuleProperty> properties){
    this.properties.addAll(properties);
    return (B)this;
  }
  public B objectClassType(String clazz){
    this.objectClassType = clazz;
    return (B)this;
  }
  public B objectClassType(Class clazz){
    this.objectClassType = clazz.getName();
    return (B)this;
  }

  public abstract T build();


}
