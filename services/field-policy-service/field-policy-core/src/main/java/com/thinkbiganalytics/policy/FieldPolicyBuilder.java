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

import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Build domain level {@link StandardizationPolicy} and {@link ValidationPolicy} objects for a given field.
 */
public class FieldPolicyBuilder {

    public static final FieldPolicy SKIP_VALIDATION = FieldPolicyBuilder.newBuilder().skipSchemaValidation().build();

    private FieldPolicy policy = new FieldPolicy();

    private List<BaseFieldPolicy> fieldPolicies = new Vector<>();
    private boolean profile;
    private boolean isPartitionColumn;

    private FieldPolicyBuilder() {
        super();
    }

    public static FieldPolicyBuilder newBuilder() {
        return new FieldPolicyBuilder();
    }


    public FieldPolicyBuilder addPolicies(List<BaseFieldPolicy> policies) {
        fieldPolicies.addAll(policies);
        return this;
    }

    public FieldPolicyBuilder tableName(String tableName) {
        policy.setTable(tableName);
        return this;
    }

    public FieldPolicyBuilder fieldName(String fieldName) {
        policy.setField(fieldName);
        return this;
    }

    public FieldPolicyBuilder disallowNullOrEmpty() {
        policy.setNullable(false);
        return this;
    }

    public FieldPolicyBuilder feedFieldName(String feedFieldName) {
        this.policy.setFeedField(feedFieldName);
        return this;
    }


    public FieldPolicyBuilder skipSchemaValidation() {
        policy.setSkipSchemaValidation(true);
        return this;
    }

    public FieldPolicyBuilder enableDiscoverTypes() {
        policy.setTypeDiscovery(true);
        return this;
    }

    public FieldPolicyBuilder setPartitionColumn(boolean isPartitionColumn) {
        this.isPartitionColumn = isPartitionColumn;
        return this;
    }

    public FieldPolicy build() {
        FieldPolicy newPolicy = this.policy;
        if (StringUtils.isEmpty(newPolicy.getFeedField())) {
            newPolicy.setFeedField(policy.getFeedField());
        }
        List<ValidationPolicy> validationPolicies = new ArrayList<>();
        List<StandardizationPolicy> standardizationPolicies = new ArrayList<>();
        for(BaseFieldPolicy policy : fieldPolicies){
            if(policy instanceof ValidationPolicy) {
                validationPolicies.add((ValidationPolicy)policy);
            }
            if(policy instanceof StandardizationPolicy) {
                standardizationPolicies.add((StandardizationPolicy)policy);
            }
        }
        newPolicy.setAllPolicies(fieldPolicies);
        newPolicy.setValidators(validationPolicies);
        newPolicy.setStandardizationPolicies(standardizationPolicies);
        newPolicy.setProfile(profile);
        newPolicy.setPartitionColumn(isPartitionColumn);
        return newPolicy;
    }

    public FieldPolicyBuilder setProfile(boolean profile) {
        this.profile = profile;
        return this;
    }

}
