package com.thinkbiganalytics.policy.rest.model;

/*-
 * #%L
 * thinkbig-field-policy-rest-model
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

import java.util.ArrayList;
import java.util.List;

/**
 * A Builder for creating {@link FieldPolicy} objects.
 *
 * @see FieldPolicy
 */
public class FieldPolicyBuilder {

    private boolean profile;
    private boolean index;
    private String fieldName;
    private String feedFieldName;
    private boolean isPartitionColumn;

    /**
     * list of field level standardization rules captured in the user interface
     */
    private List<FieldStandardizationRule> standardization;

    /**
     * list of field level validation rules captured in the user interface
     **/
    private List<FieldValidationRule> validation;

    public FieldPolicyBuilder(String fieldName) {
        this.fieldName = fieldName;
        this.feedFieldName = fieldName;
        this.standardization = new ArrayList<>();
        this.validation = new ArrayList<>();
        this.isPartitionColumn = false;
    }

    public FieldPolicyBuilder addValidations(List<FieldValidationRule> validation) {
        this.validation.addAll(validation);
        return this;
    }

    public FieldPolicyBuilder addStandardization(List<FieldStandardizationRule> standardization) {
        this.standardization.addAll(standardization);
        return this;
    }

    public FieldPolicyBuilder index(boolean index) {
        this.index = index;
        return this;
    }

    public FieldPolicyBuilder profile(boolean profile) {
        this.profile = profile;
        return this;
    }

    public FieldPolicyBuilder feedFieldName(String feedFieldName) {
        this.feedFieldName = feedFieldName;
        return this;
    }

    public FieldPolicyBuilder setPartitionColumn(boolean isPartitionColumn) {
        this.isPartitionColumn = isPartitionColumn;
        return this;
    }

    /**
     * Build a new {@link FieldPolicy}
     *
     * @return a new {@link FieldPolicy} object
     */
    public FieldPolicy build() {
        FieldPolicy policy = new FieldPolicy();
        policy.setFieldName(this.fieldName);
        policy.setFeedFieldName(this.feedFieldName);
        policy.setStandardization(this.standardization);
        policy.setValidation(this.validation);
        policy.setProfile(this.profile);
        policy.setIndex(this.index);
        policy.setPartitionColumn(isPartitionColumn);
        return policy;
    }


}
