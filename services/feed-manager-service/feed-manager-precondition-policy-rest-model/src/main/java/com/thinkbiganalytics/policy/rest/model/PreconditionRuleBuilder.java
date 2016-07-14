package com.thinkbiganalytics.policy.rest.model;

/**
 * Created by sr186054 on 4/21/16.
 */
public class PreconditionRuleBuilder extends BasePolicyRuleBuilder<PreconditionRule, PreconditionRuleBuilder> {

    public PreconditionRuleBuilder(String name) {
        super(name);
    }


    public PreconditionRule build() {
        PreconditionRule rule = new PreconditionRule();
        rule.setName(this.name);
        rule.setDescription(this.description);
        rule.setShortDescription(this.shortDescription);
        rule.setDisplayName(this.displayName);
        rule.setProperties(this.properties);
        rule.setObjectClassType(this.objectClassType);
        rule.buildValueDisplayString();
        return rule;
    }

}
