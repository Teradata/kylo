package com.thinkbiganalytics.spark.metadata;

/*-
 * #%L
 * kylo-spark-shell-client-app
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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.thinkbiganalytics.policy.FieldPoliciesJsonTransformer;
import com.thinkbiganalytics.policy.FieldPolicyBuilder;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.datavalidator.CleansedRowResult;
import com.thinkbiganalytics.spark.datavalidator.DataValidator;
import com.thinkbiganalytics.spark.datavalidator.DataValidatorResult;
import com.thinkbiganalytics.spark.model.TransformResult;
import com.thinkbiganalytics.spark.rest.model.TransformValidationResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Validates the data and adds it to the result.
 */
public class ValidationStage implements Function<TransformResult, TransformResult> {

    /**
     * Field policies.
     */
    @Nonnull
    private final FieldPolicy[] policies;

    /**
     * Data validator.
     */
    @Nonnull
    private final DataValidator validator;

    /**
     * Constructs a {@code ValidationStage}.
     */
    public ValidationStage(@Nonnull final FieldPolicy[] policies, @Nonnull final DataValidator validator) {
        this.policies = policies;
        this.validator = validator;
    }

    @Nonnull
    @Override
    public TransformResult apply(@Nullable final TransformResult result) {
        Preconditions.checkNotNull(result);

        // Validate the data set
        final DataValidatorResult validatorResult = validator.validate(result.getDataSet(), getPolicyMap(result.getDataSet().schema()));

        // Add the validation result to the transform result
        final List<List<TransformValidationResult>> rows = validatorResult.getCleansedRowResultRDD()
            .map(new ListTransformValidationResults())
            .collect();
        result.setValidationResults(rows);

        // Add the profile to the transform result
        final List<OutputRow> profile = (result.getProfile() != null) ? new ArrayList<>(result.getProfile()) : new ArrayList<OutputRow>();
        profile.addAll(validator.getProfileStats(validatorResult));
        result.setProfile(profile);

        return result;
    }

    /**
     * Gets the policy map for the specified schema.
     */
    private Map<String, com.thinkbiganalytics.policy.FieldPolicy> getPolicyMap(@Nonnull final StructType schema) {
        final Map<String, com.thinkbiganalytics.policy.FieldPolicy> policyMap = new FieldPoliciesJsonTransformer(Arrays.asList(policies)).buildPolicies();

        for (final StructField field : schema.fields()) {
            final String name = field.name().toLowerCase().trim();
            if (!policyMap.containsKey(name)) {
                final com.thinkbiganalytics.policy.FieldPolicy policy = FieldPolicyBuilder.newBuilder().fieldName(name).feedFieldName(name).build();
                policyMap.put(name, policy);
            }
        }

        return policyMap;
    }

    /**
     * Extracts the validation results from the cleansed row results.
     */
    public static class ListTransformValidationResults implements org.apache.spark.api.java.function.Function<CleansedRowResult, List<TransformValidationResult>>, Serializable {

        private static final long serialVersionUID = 8380469504292741103L;

        /**
         * JSON deserializer.
         */
        private transient ObjectMapper objectMapper;

        /**
         * Object type for reject reasons column.
         */
        private transient TypeReference<List<TransformValidationResult>> validationResultType;

        /**
         * Constructs a {@code ListTransformValidationResults}.
         */
        public ListTransformValidationResults() {
            init();
        }

        @Override
        @SuppressWarnings("squid:S1168")
        public List<TransformValidationResult> call(@Nonnull final CleansedRowResult result) throws Exception {
            final Row row = result.getRow();
            final String rejectReasonString = (String) row.get(row.length() - 1);
            if (StringUtils.isNotEmpty(rejectReasonString)) {
                return objectMapper.readValue(rejectReasonString, validationResultType);
            } else {
                return null;
            }
        }

        /**
         * Initialize class properties.
         */
        private void init() {
            objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            validationResultType = new TypeReference<List<TransformValidationResult>>() {
            };
        }

        private void readObject(@Nonnull final ObjectInputStream in) throws ClassNotFoundException, IOException {
            in.defaultReadObject();
            init();
        }
    }
}
