package com.thinkbiganalytics.spark.datavalidator.functions;

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

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.policy.BaseFieldPolicy;
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.policy.standardization.AcceptsEmptyValues;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.NotNullValidator;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationResult;
import com.thinkbiganalytics.spark.datavalidator.CleansedRowResult;
import com.thinkbiganalytics.spark.datavalidator.StandardizationAndValidationResult;
import com.thinkbiganalytics.spark.util.InvalidFormatException;
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.thinkbiganalytics.spark.datavalidator.StandardDataValidator.VALID_RESULT;

/**
 * Spark function to perform both cleansing and validation of a data row based on data policies and the target datatype
 */
public class CleanseAndValidateRow implements Function<Row, CleansedRowResult> {

    /**
     * Name of the processing date/time column.
     */
    public static final String PROCESSING_DTTM_COL = "processing_dttm";

    /**
     * Name of the column containing the rejection reason.
     */
    public static final String REJECT_REASON_COL = "dlp_reject_reason";

    private static final Logger log = LoggerFactory.getLogger(CleanseAndValidateRow.class);
    private static final long serialVersionUID = 5092972811157021179L;

    /**
     * Target data types
     */
    @Nonnull
    private final HCatDataType[] dataTypes;

    /**
     * Indicates if the data contains a processing date/time column.
     */
    private final boolean hasProcessingDttm;

    @Nonnull
    private final FieldPolicy[] policies;

    /**
     * Schema of output rows
     */
    @Nonnull
    private final StructType schema;

    public CleanseAndValidateRow(@Nonnull final FieldPolicy[] policies, @Nonnull final StructField[] fields) {
        this.policies = policies;
        hasProcessingDttm = Iterables.any(Arrays.asList(fields), new Predicate<StructField>() {
            @Override
            public boolean apply(@Nullable StructField input) {
                return input != null && input.name().equals(PROCESSING_DTTM_COL);
            }
        });

        dataTypes = resolveDataTypes(fields);
        schema = getSchema(fields);
    }

    @Override
    public CleansedRowResult call(@Nonnull final Row row) throws Exception {
        /*
    Cache for performance. Validators accept different parameters (numeric,string, etc) so we need to resolve the type using reflection
     */
        Map<Class, Class> validatorParamType = new HashMap<>();

        int nulls = hasProcessingDttm ? 1 : 0;

        // Create placeholder for the new values plus one columns for reject_reason
        Object[] newValues = new Object[dataTypes.length + 1];
        boolean rowValid = true;
        String sbRejectReason;
        List<ValidationResult> results = null;
        boolean[] columnsValid = new boolean[dataTypes.length];

        Map<Integer, Object> originalValues = new HashMap<>();

        // Iterate through columns to cleanse and validate
        for (int idx = 0; idx < dataTypes.length; idx++) {
            ValidationResult result;
            FieldPolicy fieldPolicy = policies[idx];
            HCatDataType dataType = dataTypes[idx];
            boolean columnValid = true;
            boolean isBinaryType = dataType.getConvertibleType().equals(byte[].class);

            // Extract the value (allowing for null or missing field for odd-ball data)
            Object val = (idx == row.length() || row.isNullAt(idx) ? null : row.get(idx));
            // Handle complex types by passing them through

            if (dataType.isUnchecked()) {
                if (val == null) {
                    nulls++;
                }
                newValues[idx] = val;
                originalValues.put(idx, val);
            } else {
                Object fieldValue = (val);
                boolean isEmpty;

                if (fieldValue == null) {
                    nulls++;
                }
                originalValues.put(idx, fieldValue);

                StandardizationAndValidationResult standardizationAndValidationResult = standardizeAndValidateField(fieldPolicy, fieldValue, dataType, validatorParamType);
                result = standardizationAndValidationResult.getFinalValidationResult();

                //only apply the standardized result value if the routine is valid
                fieldValue = result.isValid() ? standardizationAndValidationResult.getFieldValue() : fieldValue;

                //reevaluate the isEmpty flag
                isEmpty = ((fieldValue == null) || (StringUtils.isEmpty(fieldValue.toString())));

                //if the field is a binary type, but cant be converted set it to null.
                //hive will auto convert byte[] or String fields to a target binary type.
                if (result.isValid() && isBinaryType && !(fieldValue instanceof byte[]) && !(fieldValue instanceof String)) {
                    //set it to null
                    fieldValue = null;
                } else if ((dataType.isNumeric() || isBinaryType) && isEmpty) {
                    //if its a numeric column and the field is empty then set it to null as well
                    fieldValue = null;
                }
                newValues[idx] = fieldValue;

                if (!result.isValid()) {
                    rowValid = false;
                    results = (results == null ? new Vector<ValidationResult>() : results);
                    results.addAll(standardizationAndValidationResult.getValidationResults());
                    columnValid = false;
                }

            }

            // Record fact that we there was an invalid column
            columnsValid[idx] = columnValid;
        }
        // Return success unless all values were null.  That would indicate a blank line in the file.
        if (nulls >= dataTypes.length) {
            rowValid = false;
            results = (results == null ? new Vector<ValidationResult>() : results);
            results.add(ValidationResult.failRow("empty", "Row is empty"));
        }

        if (!rowValid) {
            for (int idx = 0; idx < dataTypes.length; idx++) {
                //if the value is not able to match the invalid dataTypes and the datatype has changed then replace with original value
                //the _invalid table dataTypes matches the source, not the destination
                if (newValues[idx] == null || originalValues.get(idx) == null || newValues[idx].getClass() != originalValues.get(idx).getClass()) {
                    newValues[idx] = originalValues.get(idx);
                }
                //otherwise the data has changed, but its still the same data type so we can keep the newly changed value

            }
        }

        // Convert to reject reasons to JSON
        sbRejectReason = toJSONArray(results);

        // Record the results in the appended columns, move processing partition value last
        if (hasProcessingDttm) {
            newValues[dataTypes.length] = newValues[dataTypes.length - 1]; //PROCESSING_DTTM_COL
            newValues[dataTypes.length - 1] = sbRejectReason;   //REJECT_REASON_COL
        } else {
            newValues[dataTypes.length] = sbRejectReason;
        }

        return new CleansedRowResult(RowFactory.create(newValues), columnsValid, rowValid);
    }

    /**
     * Gets the schema for the output rows.
     */
    @Nonnull
    public StructType getSchema() {
        return schema;
    }

    StandardizationAndValidationResult standardizeAndValidateField(FieldPolicy fieldPolicy, Object value, HCatDataType dataType, Map<Class, Class> validatorParamType) {
        StandardizationAndValidationResult result = new StandardizationAndValidationResult(value);

        List<BaseFieldPolicy> fieldPolicies = fieldPolicy.getAllPolicies();

        int standardizerCount = 0;
        for (BaseFieldPolicy p : fieldPolicies) {
            if (p instanceof StandardizationPolicy) {
                standardizerCount++;
            }
        }
        boolean validateNullValues = false;
        int processedStandardizers = 0;
        for (BaseFieldPolicy p : fieldPolicies) {
            boolean isEmpty = ((result.getFieldValue() == null) || (StringUtils.isEmpty(result.getFieldValue().toString())));
            if (p instanceof StandardizationPolicy) {
                processedStandardizers++;
                StandardizationPolicy standardizationPolicy = (StandardizationPolicy) p;
                boolean shouldStandardize = true;
                if (isEmpty && !(standardizationPolicy instanceof AcceptsEmptyValues)) {
                    shouldStandardize = false;
                }

                if (!standardizationPolicy.accepts(result.getFieldValue())) {
                    shouldStandardize = false;
                }

                if (shouldStandardize) {
                    Object newValue = standardizationPolicy.convertRawValue(result.getFieldValue());

                    //If this is the last standardizer for this field and the standardized value is returned as a String, and target column is not String, then validate and convert it to correct type
                    if (newValue != null && dataType.getConvertibleType() != newValue.getClass() && standardizerCount == processedStandardizers) {
                        try {
                            //Date and timestamp fields can be valid as strings
                            boolean isValueOk = dataType.isStringValueValidForHiveType(newValue.toString());
                            if (!isValueOk) {
                                //if the current string is not in a correct format attempt to convert it
                                try {
                                    newValue = dataType.toNativeValue(newValue.toString());
                                } catch (RuntimeException e) {
                                    result.addValidationResult(ValidationResult
                                                                   .failField("incompatible", dataType.getName(),
                                                                              "Not convertible to " + dataType.getNativeType()));
                                }
                            }
                        } catch (InvalidFormatException e) {
                            log.warn("Could not convert value {} to correct type {}", newValue.toString(), dataType.getConvertibleType().getName());
                        }
                    }
                    result.setFieldValue(newValue);
                }
            }

            if (p instanceof ValidationPolicy) {
                ValidationPolicy validationPolicy = (ValidationPolicy) p;
                //run through the validator if the value is not null, or if we are allowed to validate nulls based upon a previous
                //not null validator
                if (!isEmpty || validateNullValues || validationPolicy instanceof NotNullValidator) {
                    ValidationResult validationResult = validateValue(validationPolicy, dataType, result.getFieldValue(), validatorParamType);
                    if (isEmpty && validationPolicy instanceof NotNullValidator) {
                        validateNullValues = validationPolicy != VALID_RESULT;
                    }
                    //only need to add those that are invalid
                    if (validationResult != VALID_RESULT) {
                        result.addValidationResult(validationResult);
                        break; //exit out of processing if invalid records found.
                    }
                }
                //reset the failOnEmpty flag back to false
                if (!(validationPolicy instanceof NotNullValidator)) {
                    validateNullValues = false;
                }
            }

        }
        ValidationResult finalValidationCheck = finalValidationCheck(fieldPolicy, dataType, result.getFieldValue());
        if (finalValidationCheck != VALID_RESULT) {
            result.addValidationResult(finalValidationCheck);
        }

        return result;
    }

    /**
     * Perform validation using both dataTypes validation the validation policies
     */
    private ValidationResult finalValidationCheck(FieldPolicy fieldPolicy, HCatDataType fieldDataType, Object fieldValue) {

        boolean isEmpty = ((fieldValue instanceof String && StringUtils.isEmpty((String) fieldValue)) || fieldValue == null);
        if (!isEmpty && !fieldPolicy.shouldSkipSchemaValidation() && !fieldDataType.isValueConvertibleToType(fieldValue)) {
            return ValidationResult
                .failField("incompatible", fieldDataType.getName(),
                           "Not convertible to " + fieldDataType.getNativeType());
        }

        return VALID_RESULT;
    }

    /**
     * List of all the feedFieldNames that are part of the policyMap
     */
    @Nonnull
    private List<String> getPolicyMapFeedFieldNames() {
        return FluentIterable.from(Arrays.asList(policies))
            .filter(new Predicate<FieldPolicy>() {
                @Override
                public boolean apply(@Nullable FieldPolicy input) {
                    return (input != null && input.getFeedField() != null);
                }
            })
            .transform(new com.google.common.base.Function<FieldPolicy, String>() {
                @Override
                public String apply(@Nullable FieldPolicy input) {
                    assert input != null;
                    return input.getFeedField().toLowerCase();
                }
            })
            .toList();
    }

    /**
     * Gets the output schema for the specified target fields.
     */
    @Nonnull
    private StructType getSchema(@Nonnull final StructField[] feedFields) {
        final List<StructField> feedFieldsList = new ArrayList<>(feedFields.length);
        final List<String> policyMapFeedFieldNames = getPolicyMapFeedFieldNames();

        for (final StructField feedField : feedFields) {
            final String lowerFieldName = feedField.name().toLowerCase();
            if (policyMapFeedFieldNames.contains(lowerFieldName)) {
                if (!feedField.name().equalsIgnoreCase(PROCESSING_DTTM_COL) && !feedField.name().equalsIgnoreCase(REJECT_REASON_COL)) {
                    feedFieldsList.add(feedField);
                }
            } else {
                log.warn("Feed table field {} is not present in policy map", lowerFieldName);
            }

        }

        // Insert the two custom fields before the processing partition column
        if (hasProcessingDttm) {
            feedFieldsList.add(new StructField(PROCESSING_DTTM_COL, DataTypes.StringType, true, Metadata.empty()));
            feedFieldsList.add(feedFieldsList.size() - 1, new StructField(REJECT_REASON_COL, DataTypes.StringType, true, Metadata.empty()));
        } else {
            feedFieldsList.add(new StructField(REJECT_REASON_COL, DataTypes.StringType, true, Metadata.empty()));
        }

        return new StructType(feedFieldsList.toArray(new StructField[0]));
    }

    /**
     * Converts the table schema into the corresponding data type structures
     */
    @Nonnull
    private HCatDataType[] resolveDataTypes(StructField[] fields) {
        List<HCatDataType> cols = new ArrayList<>(fields.length);

        for (StructField field : fields) {
            String colName = field.name();
            String dataType = field.dataType().simpleString();
            cols.add(HCatDataType.createFromDataType(colName, dataType));
        }
        return cols.toArray(new HCatDataType[0]);
    }

    /* Resolve the type of param required by the validator. A cache is used to avoid cost of reflection */
    private Class resolveValidatorParamType(ValidationPolicy validator, Map<Class, Class> validatorParamType) {
        Class expectedParamClazz = validatorParamType.get(validator.getClass());
        if (expectedParamClazz == null) {
            // Cache for future references

            Object t = validator.getClass().getGenericInterfaces()[0];
            if (t instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) t;
                expectedParamClazz = (Class) type.getActualTypeArguments()[0];
            } else {
                expectedParamClazz = String.class;
            }
            validatorParamType.put(validator.getClass(), expectedParamClazz);
        }
        return expectedParamClazz;
    }

    private String toJSONArray(List<ValidationResult> results) {
        // Convert to reject reasons to JSON
        StringBuilder sb = null;
        if (results != null) {
            sb = new StringBuilder();
            for (ValidationResult result : results) {
                if (sb.length() > 0) {
                    sb.append(",");
                } else {
                    sb.append("[");
                }
                sb.append(result.toJSON());
            }
            sb.append("]");
        }
        return (sb == null ? "" : sb.toString());
    }

    private ValidationResult validateValue(ValidationPolicy validator, HCatDataType fieldDataType, Object fieldValue, Map<Class, Class> validatorParamType) {
        try {
            // Resolve the type of parameter required by the validator. A cache is used to avoid cost of reflection.
            Class expectedParamClazz = resolveValidatorParamType(validator, validatorParamType);
            Object nativeValue = fieldValue;
            if (expectedParamClazz != String.class && fieldValue instanceof String) {
                nativeValue = fieldDataType.toNativeValue(fieldValue.toString());
            }
            if (!validator.validate(nativeValue)) {

                //get any fields in this validator annotated with PolicyProperty

                return ValidationResult
                    .failFieldRule("rule", fieldDataType.getName(), validator.getClass().getSimpleName(),
                                   "Rule violation");
            }
            return VALID_RESULT;
        } catch (InvalidFormatException | ClassCastException e) {
            return ValidationResult
                .failField("incompatible", fieldDataType.getName(),
                           "Not convertible to " + fieldDataType.getNativeType());
        }

    }
}
