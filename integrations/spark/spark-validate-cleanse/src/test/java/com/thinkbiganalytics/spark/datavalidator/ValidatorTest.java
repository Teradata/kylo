/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.datavalidator;

import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.RangeValidator;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationResult;
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ValidatorTest {

    private Validator validator;

    @Before
    public void setUp() throws Exception {
        URL url = getClass().getClassLoader().getResource("example-policy.json");
        Path pathToPolicyFile = Paths.get(url.toURI());
        validator = new Validator("emp", "sampletable", "20001", pathToPolicyFile.toString());
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
        List<ValidationPolicy> validationPolicies = new ArrayList<>();
        validationPolicies.add(validatorPolicy);
        List<StandardizationPolicy> standardizationPolicies = new ArrayList<>();
        FieldPolicy fieldPolicy = new FieldPolicy("emp", "field1", false, false, validationPolicies, standardizationPolicies, false, 0);
        return validator.validateField(fieldPolicy, HCatDataType.createFromDataType("field1", dataType), value);
    }
}