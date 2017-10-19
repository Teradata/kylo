package com.thinkbiganalytics.spark.datavalidator.functions;

/*-
 * #%L
 * thinkbig-spark-validate-cleanse-app
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

import com.thinkbiganalytics.policy.BaseFieldPolicy;
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.policy.FieldPolicyBuilder;
import com.thinkbiganalytics.policy.standardization.LowercaseStandardizer;
import com.thinkbiganalytics.policy.standardization.SimpleRegexReplacer;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.standardization.UppercaseStandardizer;
import com.thinkbiganalytics.policy.validation.CharacterValidator;
import com.thinkbiganalytics.policy.validation.LookupValidator;
import com.thinkbiganalytics.policy.validation.NotNullValidator;
import com.thinkbiganalytics.policy.validation.RangeValidator;
import com.thinkbiganalytics.policy.validation.ValidationResult;
import com.thinkbiganalytics.spark.datavalidator.StandardDataValidator;
import com.thinkbiganalytics.spark.datavalidator.StandardizationAndValidationResult;
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.apache.spark.sql.types.StructField;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CleanseAndValidateRowTest {

    @SuppressWarnings("serial")
    private static final StandardizationPolicy ADD_ONE_STANDARDISATION_POLICY = new StandardizationPolicy() {

        @Override
        public String convertValue(String value) {
            throw new IllegalStateException("Method not implemented");
        }

        @Override
        public Boolean accepts(Object value) {
            return value instanceof Integer;
        }

        @Override
        public Object convertRawValue(Object value) {
            return ((Integer) value) + 1;
        }
    };

    private CleanseAndValidateRow validator;

    @Before
    public void setUp() throws Exception {
        validator = new CleanseAndValidateRow(new FieldPolicy[0], new StructField[0]);
    }

    @Test
    public void testValidateRange() {

        assertEquals(StandardDataValidator.VALID_RESULT, rangeValidate(1, 100, "decimal", "1"));
        assertEquals(StandardDataValidator.VALID_RESULT, rangeValidate(1, 100, "double", "100.0"));
        assertEquals(StandardDataValidator.VALID_RESULT, rangeValidate(1, 100, "decimal", "15.55"));
        assertEquals(StandardDataValidator.VALID_RESULT, rangeValidate(1, 100, "int", "50"));
        assertEquals(StandardDataValidator.VALID_RESULT, rangeValidate(-50, 0, "real", "-32.123"));

        assertTrue(!rangeValidate(1, 100, "decimal", "0").isValid());
        assertTrue(!rangeValidate(1, 100, "double", "0").isValid());
        assertTrue(!rangeValidate(1, 100, "int", "0").isValid());
    }

    private ValidationResult rangeValidate(Number min, Number max, String dataType, String value) {
        RangeValidator validatorPolicy = new RangeValidator(min, max);
        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(validatorPolicy);

        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName("field1").feedFieldName("field1").addPolicies(policies).build();
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, value, HCatDataType.createFromDataType("field1", dataType), new HashMap<Class, Class>());
        return result.getFinalValidationResult();
    }

    @Test
    public void standardizeRegex() {
        SimpleRegexReplacer standardizer = new SimpleRegexReplacer("(?i)foo", "bar");
        String fieldName = "field1";
        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(standardizer);
        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName(fieldName).feedFieldName(fieldName).build();

        HCatDataType fieldDataType = HCatDataType.createFromDataType(fieldName, "string");
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, "aafooaa", fieldDataType, new HashMap<Class, Class>());
        assertEquals(result.getFieldValue(), "aabaraa");

        result = validator.standardizeAndValidateField(fieldPolicy, null, fieldDataType, new HashMap<Class, Class>());
        assertNull(result.getFieldValue());

        result = validator.standardizeAndValidateField(fieldPolicy, "", fieldDataType, new HashMap<Class, Class>());
        assertEquals(result.getFieldValue(), "");
    }


    @Test
    public void standardizeAndValidate() {
        String fieldName = "field1";

        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(new SimpleRegexReplacer("(?i)foo", "bar"));
        policies.add(new LookupValidator("aabaraa"));
        policies.add(new SimpleRegexReplacer("(?i)bar", "test"));
        policies.add(new LookupValidator("aatestaa"));
        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName(fieldName).feedFieldName(fieldName).build();

        HCatDataType fieldDataType = HCatDataType.createFromDataType(fieldName, "string");
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, "aafooaa", fieldDataType, new HashMap<Class, Class>());
        assertEquals(result.getFieldValue(), "aatestaa");
        assertEquals(StandardDataValidator.VALID_RESULT, result.getFinalValidationResult());
    }

    @Test
    public void invalidStandardizeAndValidate() {
        String fieldName = "field1";

        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(new SimpleRegexReplacer("(?i)foo", "bar"));
        policies.add(new LookupValidator("blah"));
        policies.add(new SimpleRegexReplacer("(?i)bar", "test"));
        policies.add(new LookupValidator("aatestaa"));
        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName(fieldName).feedFieldName(fieldName).build();

        HCatDataType fieldDataType = HCatDataType.createFromDataType(fieldName, "string");
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, "aafooaa", fieldDataType, new HashMap<Class, Class>());
        assertEquals("aabaraa", result.getFieldValue());
        assertNotEquals(StandardDataValidator.VALID_RESULT, result.getFinalValidationResult());
    }

    @Test
    public void nullValueStandardizeAndValidate() {
        String fieldName = "field1";

        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(new SimpleRegexReplacer("(?i)foo", "bar"));
        policies.add(new LookupValidator("blah"));
        policies.add(new SimpleRegexReplacer("(?i)bar", "test"));
        policies.add(new LookupValidator("aatestaa"));
        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName(fieldName).feedFieldName(fieldName).build();

        HCatDataType fieldDataType = HCatDataType.createFromDataType(fieldName, "string");
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, null, fieldDataType, new HashMap<Class, Class>());
        assertEquals(StandardDataValidator.VALID_RESULT, result.getFinalValidationResult());

    }

    @Test
    public void mixedStandardizeAndValidate() {
        String fieldValue = "TeSt_fiELd";
        String fieldName = "field1";

        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(UppercaseStandardizer.instance());
        policies.add(new CharacterValidator("UPPERCASE"));
        policies.add(LowercaseStandardizer.instance());
        policies.add(new CharacterValidator("LOWERCASE"));
        policies.add(UppercaseStandardizer.instance());
        policies.add(new CharacterValidator("UPPERCASE"));
        policies.add(LowercaseStandardizer.instance());
        policies.add(new CharacterValidator("LOWERCASE"));

        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName(fieldName).feedFieldName(fieldName).build();

        HCatDataType fieldDataType = HCatDataType.createFromDataType(fieldName, "string");
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, fieldValue, fieldDataType, new HashMap<Class, Class>());
        assertEquals(StandardDataValidator.VALID_RESULT, result.getFinalValidationResult());
        assertEquals("test_field", result.getFieldValue());

    }


    @Test
    public void testValidateNotNull() {

        assertNotEquals(StandardDataValidator.VALID_RESULT, notNullValidate("string", null, false, false));
    }

    private ValidationResult notNullValidate(String dataType, String value, boolean allowEmptyString, boolean trimString) {
        NotNullValidator validatorPolicy = new NotNullValidator(allowEmptyString, trimString);
        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(validatorPolicy);
        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName("field1").feedFieldName("field1").build();

        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, value, HCatDataType.createFromDataType("field1", dataType), new HashMap<Class, Class>());
        return result.getFinalValidationResult();
    }


    @Test
    public void standardizeShouldNotChangeType() {
        String fieldName = "field1";

        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(ADD_ONE_STANDARDISATION_POLICY);
        policies.add(ADD_ONE_STANDARDISATION_POLICY);
        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("temp").fieldName(fieldName).feedFieldName(fieldName).build();

        HCatDataType fieldDataType = HCatDataType.createFromDataType(fieldName, "int");
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, 0, fieldDataType, new HashMap<Class, Class>());
        assertEquals(2, result.getFieldValue());
        assertEquals(StandardDataValidator.VALID_RESULT, result.getFinalValidationResult());
    }

}
