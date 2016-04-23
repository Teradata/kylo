package com.thinkbiganalytics.policy.rest.model;

/**
 * Created by sr186054 on 4/21/16.
 */
public class FieldValidationRuleBuilder extends BasePolicyRuleBuilder<FieldValidationRule,FieldValidationRuleBuilder> {

  public FieldValidationRuleBuilder(String name) {
    super(name);
  }

  @Override
  public FieldValidationRule build(){
    FieldValidationRule rule = new FieldValidationRule();
    rule.setName(this.name);
    rule.setDescription(this.description);
    rule.setDisplayName(this.displayName);
    rule.setProperties(this.properties);
    rule.setObjectClassType(this.objectClassType);
    return rule;
  }

}
