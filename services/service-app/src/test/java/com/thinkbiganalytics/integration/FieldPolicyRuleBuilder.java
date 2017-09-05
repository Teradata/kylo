package com.thinkbiganalytics.integration;

/*-
 * #%L
 * kylo-service-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
