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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Hold reference to the {@link StandardizationPolicy} and {@link ValidationPolicy} associated to a given field
 */
public class FieldPolicy implements Serializable {

    private String table;
    private String field;
    private String feedField;

    /**
     * Will preserve the value and show the row as valid even if it fails to conform to the target schema type.
     */
    private boolean skipSchemaValidation;
    private boolean nullable = true;
    private List<ValidationPolicy> validators;
    private List<StandardizationPolicy> policies;
    /**
     * Whether attempts to automatically discover the type of field data such as email, credit card, etc.
     */
    private boolean typeDiscovery;

    /**
     * Whether to scan large strings for occurrences of data that should be protected
     */
    private int piiScan;

    private boolean profile;
    private boolean isPartitionColumn;

    protected FieldPolicy() {
    }

    public FieldPolicy(String table, String field, String feedField, boolean skipSchemaValidation, boolean nullable, List<ValidationPolicy> validators,
                       List<StandardizationPolicy> policies, boolean typeDiscovery, int piiScan) {
        this.table = table;
        this.field = field;
        this.feedField = feedField;
        this.skipSchemaValidation = skipSchemaValidation;
        this.nullable = nullable;
        this.validators = validators;
        this.policies = policies;
        this.typeDiscovery = typeDiscovery;
        this.piiScan = piiScan;
    }

    public FieldPolicy(String table, String field, String feedField, boolean skipSchemaValidation, boolean nullable, List<ValidationPolicy> validators,
                       List<StandardizationPolicy> policies, boolean typeDiscovery, int piiScan, boolean isPartitionColumn) {
        this.table = table;
        this.field = field;
        this.feedField = feedField;
        this.skipSchemaValidation = skipSchemaValidation;
        this.nullable = nullable;
        this.validators = validators;
        this.policies = policies;
        this.typeDiscovery = typeDiscovery;
        this.piiScan = piiScan;
        this.isPartitionColumn = isPartitionColumn;
    }

    public static void main(String[] args) {

    }

    public List<ValidationPolicy> getValidators() {
        return validators;
    }

    protected void setValidators(List<ValidationPolicy> validators) {
        this.validators = Collections.unmodifiableList(validators);
    }

    public List<StandardizationPolicy> getStandardizationPolicies() {
        return policies;
    }

    protected void setStandardizationPolicies(List<StandardizationPolicy> policies) {
        this.policies = Collections.unmodifiableList(policies);
    }

    public String getTable() {
        return table;
    }

    protected void setTable(String table) {
        this.table = table;
    }

    public String getField() {
        return field;
    }

    protected void setField(String field) {
        this.field = field;
    }

    public String getFeedField() {
        return feedField;
    }

    protected void setFeedField(String feedField) {
        this.feedField = feedField;
    }

    /**
     * Will preserve the value and show the row as valid even if it fails to conform to the target schema type.
     */
    public Boolean shouldSkipSchemaValidation() {
        return skipSchemaValidation;
    }

    protected void setSkipSchemaValidation(boolean skipSchemaValidation) {
        this.skipSchemaValidation = skipSchemaValidation;
    }

    public boolean isNullable() {
        return nullable;
    }

    protected void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isProfile() {
        return profile;
    }

    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    public boolean shouldTypeDiscovery() {
        return typeDiscovery;
    }

    protected void setTypeDiscovery(boolean typeDiscovery) {
        this.typeDiscovery = typeDiscovery;
    }

    public boolean isPartitionColumn() {
        return isPartitionColumn;
    }

    public void setPartitionColumn(boolean partitionColumn) {
        this.isPartitionColumn = partitionColumn;
    }

    public String toString() {
        return "FieldPolicy validators [" + (validators == null ? "NULL" : validators.size()) + "] policies [" + (policies == null
                                                                                                                  ? "NULL"
                                                                                                                  : policies.size())
               + "]";
    }
}
