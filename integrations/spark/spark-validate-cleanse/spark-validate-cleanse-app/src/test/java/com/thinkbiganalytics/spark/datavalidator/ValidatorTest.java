package com.thinkbiganalytics.spark.datavalidator;

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
import com.thinkbiganalytics.policy.FieldPoliciesJsonTransformer;
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
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ValidatorTest {

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

    private Validator validator;

    @Before
    public void setUp() throws Exception {
        URL url = getClass().getClassLoader().getResource("example-policy.json");
        Path pathToPolicyFile = (url != null) ? Paths.get(url.toURI()) : Paths.get("");
        validator = new Validator();
        validator.setArguments("emp", "sampletable", "20001", pathToPolicyFile.toString());
    }


    @Test
    public void testParseRemainingParameters() {
        String[] args = {"targetDatabase", "entity", "partition", "path-to-policy-file", "-h", "hive.setting.1=value.1", "--hiveConf", "hive.setting.2=value.2"};
        CommandLineParams params = Validator.parseRemainingParameters(args, 4);
        List<Param> hiveParams = params.getHiveParams();

        Param first = hiveParams.get(0);
        assertEquals("hive.setting.1", first.getName());
        assertEquals("value.1", first.getValue());

        Param second = hiveParams.get(1);
        assertEquals("hive.setting.2", second.getName());
        assertEquals("value.2", second.getValue());
    }

    @Test
    public void testParseRemainingParametersStorageLevel() {
        String[] args = {"targetDatabase", "entity", "partition", "path-to-policy-file", "--storageLevel", "MEMORY_ONLY"};
        CommandLineParams params = Validator.parseRemainingParameters(args, 4);
        String storageLevel = params.getStorageLevel();
        assertEquals("MEMORY_ONLY", storageLevel);
    }

    @Test
    public void testDefaultStorageLevel() {
        String[] args = {"targetDatabase", "entity", "partition", "path-to-policy-file"};
        CommandLineParams params = Validator.parseRemainingParameters(args, 4);
        String defaultStorageLevel = params.getStorageLevel();
        assertEquals("MEMORY_AND_DISK", defaultStorageLevel);
    }

    @Test
    public void testParseRemainingParametersNumPartitions() {
        String[] args = {"targetDatabase", "entity", "partition", "path-to-policy-file", "--storageLevel", "MEMORY_ONLY", "--numPartitions", "10"};
        CommandLineParams params = Validator.parseRemainingParameters(args, 4);
        Integer numRDDPartitions = params.getNumPartitions();
        assertEquals("10", String.valueOf(numRDDPartitions));
    }

    @Test
    public void testDefaultNumPartitions() {
        String[] args = {"targetDatabase", "entity", "partition", "path-to-policy-file"};
        CommandLineParams params = Validator.parseRemainingParameters(args, 4);
        Integer defaultRDDPartitions = params.getNumPartitions();
        assertEquals("-1", String.valueOf(defaultRDDPartitions));
    }

    @Test
    public void testParseRemainingParameters_missingParameters() {
        String[] args = {"targetDatabase", "entity", "partition", "path-to-policy-file"};
        CommandLineParams params = Validator.parseRemainingParameters(args, 4);
        List<Param> hiveParams = params.getHiveParams();

        assertTrue(hiveParams.isEmpty());
    }

    @Test
    public void testValidateRange() {

        assertEquals(Validator.VALID_RESULT, rangeValidate(1, 100, "decimal", "1"));
        assertEquals(Validator.VALID_RESULT, rangeValidate(1, 100, "double", "100.0"));
        assertEquals(Validator.VALID_RESULT, rangeValidate(1, 100, "decimal", "15.55"));
        assertEquals(Validator.VALID_RESULT, rangeValidate(1, 100, "int", "50"));
        assertEquals(Validator.VALID_RESULT, rangeValidate(-50, 0, "real", "-32.123"));

        assertTrue(!rangeValidate(1, 100, "decimal", "0").isValid());
        assertTrue(!rangeValidate(1, 100, "double", "0").isValid());
        assertTrue(!rangeValidate(1, 100, "int", "0").isValid());
    }

    private ValidationResult rangeValidate(Number min, Number max, String dataType, String value) {
        RangeValidator validatorPolicy = new RangeValidator(min, max);
        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(validatorPolicy);

        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName("field1").feedFieldName("field1").addPolicies(policies).build();
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, value, HCatDataType.createFromDataType("field1", dataType));
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
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, "aafooaa", fieldDataType);
        assertEquals(result.getFieldValue(),"aabaraa");

        result = validator.standardizeAndValidateField(fieldPolicy, null, fieldDataType);
        assertNull(result.getFieldValue());

        result = validator.standardizeAndValidateField(fieldPolicy, "", fieldDataType);
        assertEquals(result.getFieldValue(),"");
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
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, "aafooaa", fieldDataType);
        assertEquals(result.getFieldValue(), "aatestaa");
        assertEquals(Validator.VALID_RESULT,result.getFinalValidationResult());
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
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, "aafooaa", fieldDataType);
        assertEquals("aabaraa",result.getFieldValue() );
        assertNotEquals(Validator.VALID_RESULT,result.getFinalValidationResult());
    }

    @Test
    public void nullValueStandardizeAndValidate() {
        String fieldValue = null;
        String fieldName = "field1";

        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(new SimpleRegexReplacer("(?i)foo", "bar"));
        policies.add(new LookupValidator("blah"));
        policies.add(new SimpleRegexReplacer("(?i)bar", "test"));
        policies.add(new LookupValidator("aatestaa"));
        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName(fieldName).feedFieldName(fieldName).build();

        HCatDataType fieldDataType = HCatDataType.createFromDataType(fieldName, "string");
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, null, fieldDataType);
        assertEquals(Validator.VALID_RESULT,result.getFinalValidationResult());

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
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, fieldValue, fieldDataType);
        assertEquals(Validator.VALID_RESULT,result.getFinalValidationResult());
        assertEquals("test_field", result.getFieldValue());

    }



    @Test
    public void testValidateNotNull() {

        assertNotEquals(Validator.VALID_RESULT, notNullValidate("string", null, false, false));
    }

    private ValidationResult notNullValidate(String dataType, String value, boolean allowEmptyString, boolean trimString) {
        NotNullValidator validatorPolicy = new NotNullValidator(allowEmptyString, trimString);
        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(validatorPolicy);
        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("emp").fieldName("field1").feedFieldName("field1").build();

        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, value, HCatDataType.createFromDataType("field1", dataType));
        return result.getFinalValidationResult();
    }

    @Test
    public void testPolicyMap() {
        String fieldPolicyJson = "[{\"profile\":true,\"index\":false,\"fieldName\":\"fieldA\",\"feedFieldName\":\"fieldA\",\"standardization\":null,\"validation\":[{\"name\":\"Not Null\","
                                 + "\"displayName\":null,\"description\":null,\"shortDescription\":null,\"properties\":[{\"name\":\"EMPTY_STRING\",\"displayName\":null,\"value\":\"false\",\"values\":null,\"placeholder\":null,\"type\":null,\"hint\":null,\"objectProperty\":\"allowEmptyString\",\"selectableValues\":[],\"required\":false,\"group\":null,\"groupOrder\":null,\"layout\":null,\"hidden\":false,\"pattern\":null,\"patternInvalidMessage\":null},{\"name\":\"TRIM_STRING\",\"displayName\":null,\"value\":\"true\",\"values\":null,\"placeholder\":null,\"type\":null,\"hint\":null,\"objectProperty\":\"trimString\",\"selectableValues\":[],\"required\":false,\"group\":null,\"groupOrder\":null,\"layout\":null,\"hidden\":false,\"pattern\":null,\"patternInvalidMessage\":null}],\"objectClassType\":\"com.thinkbiganalytics.policy.validation.NotNullValidator\",\"objectShortClassType\":\"NotNullValidator\",\"propertyValuesDisplayString\":null,\"regex\":null,\"type\":null}]},{\"profile\":true,\"index\":false,\"fieldName\":\"id\",\"feedFieldName\":\"id\",\"standardization\":null,\"validation\":null},{\"profile\":true,\"index\":false,\"fieldName\":\"email\",\"feedFieldName\":\"email\",\"standardization\":null,\"validation\":null},{\"profile\":true,\"index\":false,\"fieldName\":\"gender\",\"feedFieldName\":\"gender\",\"standardization\":null,\"validation\":null},{\"profile\":true,\"index\":false,\"fieldName\":\"ip_address\",\"feedFieldName\":\"ip_address\",\"standardization\":null,\"validation\":null},{\"profile\":true,\"index\":false,\"fieldName\":\"credit_card\",\"feedFieldName\":\"credit_card\",\"standardization\":null,\"validation\":null},{\"profile\":true,\"index\":false,\"fieldName\":\"country\",\"feedFieldName\":\"country\",\"standardization\":null,\"validation\":null},{\"profile\":true,\"index\":false,\"fieldName\":\"birthdate\",\"feedFieldName\":\"birthdate\",\"standardization\":null,\"validation\":null},{\"profile\":true,\"index\":false,\"fieldName\":\"salary\",\"feedFieldName\":\"salary\",\"standardization\":null,\"validation\":null},{\"profile\":true,\"index\":false,\"fieldName\":\"fieldB\",\"feedFieldName\":\"fieldB\",\"standardization\":null,\"validation\":null}]";
        Map<String, FieldPolicy> policyMap = new FieldPoliciesJsonTransformer(fieldPolicyJson).buildPolicies();
        assertEquals(policyMap.size(), 10);

    }

    @Test
    public void standardizeShouldNotChangeType() {
        String fieldName = "field1";

        List<BaseFieldPolicy> policies = new ArrayList<>();
        policies.add(ADD_ONE_STANDARDISATION_POLICY);
        policies.add(ADD_ONE_STANDARDISATION_POLICY);
        FieldPolicy fieldPolicy = FieldPolicyBuilder.newBuilder().addPolicies(policies).tableName("temp").fieldName(fieldName).feedFieldName(fieldName).build();

        HCatDataType fieldDataType = HCatDataType.createFromDataType(fieldName, "int");
        StandardizationAndValidationResult result = validator.standardizeAndValidateField(fieldPolicy, 0, fieldDataType);
        assertEquals(2, result.getFieldValue());
        assertEquals(Validator.VALID_RESULT,result.getFinalValidationResult());
    }

}
