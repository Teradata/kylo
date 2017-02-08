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
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transform a string of JSON into field policy ({@link FieldPolicy}  objects
 */
public class FieldPoliciesJsonTransformer {

    private static final Logger log = LoggerFactory.getLogger(FieldPoliciesJsonTransformer.class);
    List<com.thinkbiganalytics.policy.rest.model.FieldPolicy>
        uiFieldPolicies;
    /**
     * JSON array of com.thinkbiganalytics.policy.rest.model.FieldPolicy objects
     */
    private String jsonFieldPolicies;

    public FieldPoliciesJsonTransformer(String jsonFieldPolicies) {
        this.jsonFieldPolicies = jsonFieldPolicies;
        ObjectMapper mapper = new ObjectMapper();
        try {
            uiFieldPolicies =
                mapper.readValue(jsonFieldPolicies,
                                 new TypeReference<List<com.thinkbiganalytics.policy.rest.model.FieldPolicy>>() {
                                 });

        } catch (Exception e) {
            e.printStackTrace();
            log.error("ERROR converting Field Policy JSON to Rest Models : {}", e.getMessage(), e);
        }


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

}
