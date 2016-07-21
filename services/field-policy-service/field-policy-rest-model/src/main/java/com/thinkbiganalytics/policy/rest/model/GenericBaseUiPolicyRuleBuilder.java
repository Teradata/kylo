package com.thinkbiganalytics.policy.rest.model;

/**
 * Created by sr186054 on 4/21/16.
 */
public class GenericBaseUiPolicyRuleBuilder<T extends BaseUiPolicyRule> extends BasePolicyRuleBuilder<T, GenericBaseUiPolicyRuleBuilder> {

    private Class<T> policyClass;

    public GenericBaseUiPolicyRuleBuilder(Class<T> policyClass, String name) {
        super(name);
        this.policyClass = policyClass;
    }

    public T build() {

        T rule = null;
        try {
            rule = policyClass.newInstance();

            rule.setName(this.name);
            rule.setDescription(this.description);
            rule.setDisplayName(this.displayName);
            rule.setProperties(this.properties);
            rule.setObjectClassType(this.objectClassType);
            return rule;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to build Rule Class " + policyClass + ", " + e.getMessage(), e);
        }

    }


}
