package com.thinkbiganalytics.integration;

import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FieldPolicyRuleBuilder {

    private String fieldName;
    private int seq;
    private List<FieldStandardizationRule> standardisations = new ArrayList<>();
    private List<FieldValidationRule> validations = new ArrayList<>();
    private boolean profile;
    private boolean index;

    FieldPolicyRuleBuilder(String fieldName) {
        this.fieldName = fieldName;
    }

    FieldPolicyRuleBuilder withStandardisation(FieldStandardizationRule... rules) {
        for (FieldStandardizationRule rule : rules) {
            rule.setSequence(seq++);
            standardisations.add(rule);
        }
        return this;
    }

    FieldPolicyRuleBuilder withValidation(FieldValidationRule... rules) {
        Arrays.stream(rules).forEach(rule -> {
            rule.setSequence(seq++);
            validations.add(rule);
        });
        return this;
    }

    FieldPolicyRuleBuilder withProfile() {
        profile = true;
        return this;
    }

    FieldPolicyRuleBuilder withIndex() {
        index = true;
        return this;
    }

    FieldPolicy toPolicy() {
        FieldPolicy policy = new FieldPolicy();
        policy.setFieldName(fieldName);
        policy.setFeedFieldName(fieldName);
        policy.setProfile(profile);
        policy.setIndex(index);
        policy.setStandardization(standardisations);
        policy.setValidation(validations);
        return policy;
    }
}
