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

import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

class ModifiedSchema {

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
            if (policy.getField() != null) {
                String feedFieldName = policy.getFeedField().toLowerCase();
                String fieldName = policy.getField().toLowerCase();
                validFieldToFeedFieldMap.put(fieldName, feedFieldName);
                if (policy.hasStandardizationPolicies()) {
                    validFieldsWithStandardizers.add(fieldName);
                }
            }
        }

        List<StructField> fieldsList = new ArrayList<>(feedFields.length);
        for (StructField feedField : feedFields) {
            String lowerFeedFieldName = feedField.name().toLowerCase();
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
            fieldsList.add(field);
        }

        return new StructType(fieldsList.toArray(new StructField[0]));
    }

    private ModifiedSchema() {
        throw new UnsupportedOperationException("Instances of SchemaBuild cannot be constructed");
    }
}
