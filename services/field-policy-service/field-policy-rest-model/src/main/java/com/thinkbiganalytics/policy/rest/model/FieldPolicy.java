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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Represents a set of both {@link FieldStandardizationRule} and {@link FieldValidationRule} captured from the User Interface that will ultimately be converted into domain level policies for ingesting
 * data Refer to the {@link FieldPolicyBuilder} for the builder in helping create these objects
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldPolicy {

    private boolean profile;
    private boolean index;
    private String fieldName;
    private String feedFieldName;

    private List<FieldStandardizationRule> standardization;

    private List<FieldValidationRule> validation;

    private boolean isPartitionColumn;  //default is false

    private String domainTypeId;

    public FieldPolicy() {

    }

    public boolean isProfile() {
        return profile;
    }

    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    public List<FieldStandardizationRule> getStandardization() {
        return standardization;
    }

    public void setStandardization(List<FieldStandardizationRule> standardization) {
        this.standardization = standardization;
    }

    public List<FieldValidationRule> getValidation() {
        return validation;
    }

    public void setValidation(List<FieldValidationRule> validation) {
        this.validation = validation;
    }

    public boolean isIndex() {
        return index;
    }

    public void setIndex(boolean index) {
        this.index = index;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFeedFieldName() {
        return feedFieldName;
    }

    public void setFeedFieldName(String feedFieldName) {
        this.feedFieldName = feedFieldName;
    }

    public boolean isPartitionColumn() {
        return isPartitionColumn;
    }

    public void setPartitionColumn(boolean isPartitionColumn) {
        this.isPartitionColumn = isPartitionColumn;
    }

    public String getDomainTypeId() {
        return domainTypeId;
    }

    public void setDomainTypeId(String domainTypeId) {
        this.domainTypeId = domainTypeId;
    }
}
