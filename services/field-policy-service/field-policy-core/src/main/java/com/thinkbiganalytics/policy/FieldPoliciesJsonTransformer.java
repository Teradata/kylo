package com.thinkbiganalytics.policy;

/*-
 * #%L
 * thinkbig-field-policy-core
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Transform a string of JSON into field policy ({@link FieldPolicy}  objects
 */
public class FieldPoliciesJsonTransformer {

    private static final Logger log = LoggerFactory.getLogger(FieldPoliciesJsonTransformer.class);

    @Nonnull
    private final List<com.thinkbiganalytics.policy.rest.model.FieldPolicy> uiFieldPolicies;

    @SuppressWarnings("squid:S2637")
    public FieldPoliciesJsonTransformer(@Nonnull final String jsonFieldPolicies) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            uiFieldPolicies = objectMapper.readValue(jsonFieldPolicies, new TypeReference<List<com.thinkbiganalytics.policy.rest.model.FieldPolicy>>() {
            });
        } catch (final Exception e) {
            log.error("ERROR converting Field Policy JSON to Rest Models : {}", e.getMessage(), e);
            throw Throwables.propagate(e);
        }
        Preconditions.checkNotNull(uiFieldPolicies, "Field policies cannot be null");
    }

    public FieldPoliciesJsonTransformer(@Nonnull final List<com.thinkbiganalytics.policy.rest.model.FieldPolicy> fieldPolicies) {
        this.uiFieldPolicies = fieldPolicies;
    }

    /**
     * build a map of field to field policies
     *
     * @return a map with the field name as the key and a field policy object as the valure listing the possible {@link StandardizationPolicy} and {@link ValidationPolicy} associated with the given
     * field
     */
    public Map<String, FieldPolicy> buildPolicies() {

        Map<String, FieldPolicy> fieldPolicyMap = new HashMap<>();
        PolicyTransformationListener listener = new PolicyTransformationListener();
        if (uiFieldPolicies != null) {
            for (com.thinkbiganalytics.policy.rest.model.FieldPolicy uiFieldPolicy : uiFieldPolicies) {
                FieldPolicyTransformer transformer = new FieldPolicyTransformer(uiFieldPolicy);
                transformer.setListener(listener);
                if (uiFieldPolicy.getFieldName() != null) {
                    fieldPolicyMap.put(uiFieldPolicy.getFieldName().toLowerCase().trim(), transformer.buildPolicy());
                }
            }
        }
        log.info("Transformed UI Policies to Field Policies.  {} ", listener.getCounts());
        return fieldPolicyMap;

    }

    /**
     * Listener to count the total standardizers/validators on a given field
     */
    private class PolicyTransformationListener implements FieldPolicyTransformerListener {

        private int validationCount = 0;
        private int standardizationCount = 0;

        @Override
        public void onAddValidationPolicy(ValidationPolicy policy) {
            validationCount++;
        }

        @Override
        public void onAddStandardizationPolicy(StandardizationPolicy policy) {
            standardizationCount++;
        }

        public String getCounts() {
            return "Total Validation Policies: " + validationCount + ", Total Standardization Policies: " + standardizationCount;
        }
    }

    public void augmentPartitionColumnValidation() {
        log.info("Augmenting partition column validation");
        for (com.thinkbiganalytics.policy.rest.model.FieldPolicy uiFieldPolicy : uiFieldPolicies) {
            if (uiFieldPolicy.isPartitionColumn()) {
                log.info("Found a partition column: " + uiFieldPolicy.getFieldName());
                boolean augmentRule = true;

                List<FieldValidationRule> currentValidationRules = uiFieldPolicy.getValidation();
                if (currentValidationRules != null) {
                    log.info("There are validation rules already set for " + uiFieldPolicy.getFieldName());
                    for (FieldValidationRule fieldValidationRule : currentValidationRules) {
                        if (fieldValidationRule.getObjectClassType().equals("com.thinkbiganalytics.policy.validation.NotNullValidator")
                            || (fieldValidationRule.getObjectShortClassType().equals("NotNullValidator"))) {
                            log.info("NotNull validation rule already set for " + uiFieldPolicy.getFieldName());
                            augmentRule = false;
                        }
                    }
                }

                if (augmentRule) {
                    log.info("Augmenting the field " + uiFieldPolicy.getFieldName() + " with NotNull validation rule");
                    if (currentValidationRules == null) {
                        currentValidationRules = new ArrayList<>();
                    }
                    FieldValidationRule fvRule = new FieldValidationRule();
                    fvRule.setName("Partition Column Not Null");
                    fvRule.setObjectClassType("com.thinkbiganalytics.policy.validation.NotNullValidator");
                    fvRule.setObjectShortClassType("NotNullValidator");

                    List<FieldRuleProperty> fieldRulePropertyList = new ArrayList<>();

                    FieldRuleProperty fieldRuleProperty1 = new FieldRuleProperty();
                    fieldRuleProperty1.setName("EMPTY_STRING");
                    fieldRuleProperty1.setDisplayName(null);
                    fieldRuleProperty1.setValue("false");
                    fieldRuleProperty1.setPlaceholder(null);
                    fieldRuleProperty1.setType(null);
                    fieldRuleProperty1.setHint(null);
                    fieldRuleProperty1.setObjectProperty("allowEmptyString");
                    fieldRuleProperty1.setRequired(false);
                    fieldRuleProperty1.setGroup(null);
                    fieldRuleProperty1.setLayout(null);
                    fieldRuleProperty1.setHidden(false);
                    fieldRuleProperty1.setPattern(null);
                    fieldRuleProperty1.setPatternInvalidMessage(null);

                    FieldRuleProperty fieldRuleProperty2 = new FieldRuleProperty();
                    fieldRuleProperty2.setName("TRIM_STRING");
                    fieldRuleProperty2.setDisplayName(null);
                    fieldRuleProperty2.setValue("true");
                    fieldRuleProperty2.setPlaceholder(null);
                    fieldRuleProperty2.setType(null);
                    fieldRuleProperty2.setHint(null);
                    fieldRuleProperty2.setObjectProperty("trimString");
                    fieldRuleProperty2.setRequired(false);
                    fieldRuleProperty2.setGroup(null);
                    fieldRuleProperty2.setLayout(null);
                    fieldRuleProperty2.setHidden(false);
                    fieldRuleProperty2.setPattern(null);
                    fieldRuleProperty2.setPatternInvalidMessage(null);

                    fieldRulePropertyList.add(fieldRuleProperty1);
                    fieldRulePropertyList.add(fieldRuleProperty2);

                    fvRule.setProperties(fieldRulePropertyList);

                    currentValidationRules.add(fvRule);
                    log.info("Added rule for NotNull validation on " + uiFieldPolicy.getFieldName());
                    uiFieldPolicy.setValidation(currentValidationRules);
                }

            }
        }
    }
}
