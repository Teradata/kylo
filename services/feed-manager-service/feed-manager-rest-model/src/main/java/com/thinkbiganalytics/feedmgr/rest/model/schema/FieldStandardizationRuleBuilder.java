package com.thinkbiganalytics.feedmgr.rest.model.schema;

import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public class FieldStandardizationRuleBuilder extends BasePolicyRuleBuilder<FieldStandardizationRule,FieldStandardizationRuleBuilder> {

  public FieldStandardizationRuleBuilder(String name) {
    super(name);
  }




  public FieldStandardizationRule build(){
    FieldStandardizationRule rule = new FieldStandardizationRule();
    rule.setName(this.name);
    rule.setDescription(this.description);
    rule.setDisplayName(this.displayName);
    rule.setProperties(this.properties);
    rule.setObjectClassType(this.objectClassType);
    return rule;
  }

}
