package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * kylo-spark-validate-cleanse-core
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.thinkbiganalytics.spark.datavalidator.StandardDataValidator.PROCESSING_DTTM_COL;
import static com.thinkbiganalytics.spark.datavalidator.StandardDataValidator.REJECT_REASON_COL;

class ModifiedSchema {

    private static final Logger log = LoggerFactory.getLogger(ModifiedSchema.class);

    @Nonnull
    public static StructType getInvalidTableSchema(@Nonnull final StructField[] feedFields, @Nonnull final FieldPolicy[] policies) {
        final List<StructField> feedFieldsList = new ArrayList<>(feedFields.length);
        final List<String> policyMapFeedFieldNames = getPolicyMapFeedFieldNames(policies);

        for (StructField feedField : feedFields) {
            String lowerFieldName = feedField.name().toLowerCase();

            if (policyMapFeedFieldNames.contains(lowerFieldName)) {
                addToList(feedField, feedFieldsList);
            } else {
                log.warn("Feed table field {} is not present in policy map", lowerFieldName);
            }

        }
        return finalizeFieldsList(feedFieldsList);
    }

    @Nonnull
    public static StructType getValidTableSchema(@Nonnull final StructField[] feedFields, @Nonnull final StructField[] validFields, @Nonnull final FieldPolicy[] policies) {
        // Map of the lower feed valid name to the field type
        final Map<String, StructField> validFieldsMap = new HashMap<>();
        for (StructField validField : validFields) {
            String lowerFieldName = validField.name().toLowerCase();
            validFieldsMap.put(lowerFieldName, validField);
        }

        // A map of the feedFieldName to validFieldName
        final Map<String, String> validFieldToFeedFieldMap = new HashMap<>();
        // List of all those validFieldNames that have a standardizer on them
        final List<String> validFieldsWithStandardizers = new ArrayList<>();
        for (FieldPolicy policy : policies) {
            String feedFieldName = policy.getFeedField().toLowerCase();
            String fieldName = policy.getField().toLowerCase();
            validFieldToFeedFieldMap.put(fieldName, feedFieldName);
            if (policy.hasStandardizationPolicies()) {
                validFieldsWithStandardizers.add(fieldName);
            }
        }

        List<StructField> fieldsList = new ArrayList<>(feedFields.length);
        final List<String> policyMapFeedFieldNames = getPolicyMapFeedFieldNames(policies);
        for (StructField feedField : feedFields) {
            String lowerFeedFieldName = feedField.name().toLowerCase();
            if (policyMapFeedFieldNames.contains(lowerFeedFieldName)) {
                StructField field = feedField;
                //get the corresponding valid table field name
                String lowerFieldName = validFieldToFeedFieldMap.get(lowerFeedFieldName);
                //if we are standardizing then use the field type matching the _valid table
                if (validFieldsWithStandardizers.contains(lowerFieldName)) {
                    //get the valid table
                    field = validFieldsMap.get(lowerFieldName);
                    HCatDataType dataType = HCatDataType.createFromDataType(field.name(), field.dataType().simpleString());
                    if (dataType != null && dataType.isDateOrTimestamp()) {
                        field = new StructField(field.name(), DataTypes.StringType, field.nullable(), field.metadata());
                    }
                }
                addToList(field, fieldsList);
            } else {
                log.warn("Valid table field {} is not present in policy map", lowerFeedFieldName);
            }

        }
        return finalizeFieldsList(fieldsList);
    }

    private static void addToList(@Nonnull final StructField field, @Nonnull final List<StructField> fieldList) {
        if (!field.name().equalsIgnoreCase(PROCESSING_DTTM_COL) && !field.name().equalsIgnoreCase(REJECT_REASON_COL)) {
            fieldList.add(field);
        }
    }

    /**
     * Add in the REJECT_REASON_COL to the fields list after the PROCESSING_DTTM
     */
    @Nonnull
    private static StructType finalizeFieldsList(@Nonnull final List<StructField> fieldsList) {
        // Insert the two custom fields before the processing partition column
        fieldsList.add(new StructField(PROCESSING_DTTM_COL, DataTypes.StringType, true, Metadata.empty()));
        fieldsList.add(fieldsList.size() - 1, new StructField(REJECT_REASON_COL, DataTypes.StringType, true, Metadata.empty()));

        return new StructType(fieldsList.toArray(new StructField[0]));
    }

    /**
     * List of all the feedFieldNames that are part of the policyMap
     */
    @Nonnull
    public static List<String> getPolicyMapFeedFieldNames(@Nonnull final FieldPolicy[] policies) {
        return Lists.transform(Arrays.asList(policies), new Function<FieldPolicy, String>() {
            @Nullable
            @Override
            public String apply(@Nullable final FieldPolicy policy) {
                return (policy != null) ? policy.getFeedField().toLowerCase() : null;
            }
        });
    }

    private ModifiedSchema() {
        throw new UnsupportedOperationException("Instances of SchemaBuild cannot be constructed");
    }
}
