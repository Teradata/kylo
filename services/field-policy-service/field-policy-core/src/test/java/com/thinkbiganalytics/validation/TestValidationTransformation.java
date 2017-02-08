package com.thinkbiganalytics.validation;

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

import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.policy.validation.CreditCardValidator;
import com.thinkbiganalytics.policy.validation.EmailValidator;
import com.thinkbiganalytics.policy.validation.IPAddressValidator;
import com.thinkbiganalytics.policy.validation.LengthValidator;
import com.thinkbiganalytics.policy.validation.LookupValidator;
import com.thinkbiganalytics.policy.validation.NotNullValidator;
import com.thinkbiganalytics.policy.validation.RangeValidator;
import com.thinkbiganalytics.policy.validation.RegexValidator;
import com.thinkbiganalytics.policy.validation.TimestampValidator;
import com.thinkbiganalytics.policy.validation.USPhoneValidator;
import com.thinkbiganalytics.policy.validation.USZipCodeValidator;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.validation.transform.ValidatorAnnotationTransformer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 */
public class TestValidationTransformation {


    @Test
    @Ignore
    public void testRangeValidator() {
        Integer min = 10;
        Integer max = 20;
        RangeValidator validator = new RangeValidator(min, max);
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        RangeValidator convertedValidator = fromUI(uiModel, RangeValidator.class);
        Assert.assertEquals(min, convertedValidator.getMin());
        Assert.assertEquals(max, convertedValidator.getMin());
    }

    @Test
    public void testCreditCardValidator() {
        CreditCardValidator validator = CreditCardValidator.instance();
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        CreditCardValidator convertedValidator = fromUI(uiModel, CreditCardValidator.class);
        Assert.assertEquals(validator, convertedValidator);
    }

    @Test
    public void testEmailValidator() {
        EmailValidator validator = EmailValidator.instance();
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        EmailValidator convertedValidator = fromUI(uiModel, EmailValidator.class);
        Assert.assertEquals(validator, convertedValidator);
    }

    @Test
    public void testIPAddressValidator() {
        IPAddressValidator validator = IPAddressValidator.instance();
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        IPAddressValidator convertedValidator = fromUI(uiModel, IPAddressValidator.class);
        Assert.assertEquals(validator, convertedValidator);
    }

    @Test
    public void testLookupValidator() {
        String lookupList = "one,two,three";
        LookupValidator validator = new LookupValidator(lookupList);
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        LookupValidator convertedValidator = fromUI(uiModel, LookupValidator.class);
        Assert.assertEquals(lookupList, convertedValidator.getLookupList());
        for (String value : StringUtils.split(lookupList, ",")) {
            Assert.assertTrue(value, convertedValidator.getLookupValues().contains(value));
        }
    }

    @Test
    public void testLengthValidator() {
        Integer min = 10;
        Integer max = 20;
        LengthValidator validator = new LengthValidator(min, max);
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        LengthValidator convertedValidator = fromUI(uiModel, LengthValidator.class);
        Assert.assertEquals(min.intValue(), convertedValidator.getMinLength());
        Assert.assertEquals(max.intValue(), convertedValidator.getMaxLength());
    }

    @Test
    public void testRegexValidator() {
        String regex = ".";
        RegexValidator validator = new RegexValidator(regex);
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        RegexValidator convertedValidator = fromUI(uiModel, RegexValidator.class);
        Assert.assertEquals(regex, convertedValidator.getRegexExpression());
        Assert.assertEquals(regex, convertedValidator.getPattern().pattern());
    }

    @Test
    public void testTimestampValidator() {
        TimestampValidator validator = new TimestampValidator(false);
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        TimestampValidator convertedValidator = fromUI(uiModel, TimestampValidator.class);
        Assert.assertEquals(validator.isAllowNull(), convertedValidator.isAllowNull());
    }

    @Test
    public void testUSPhoneValidator() {
        USPhoneValidator validator = USPhoneValidator.instance();
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        USPhoneValidator convertedValidator = fromUI(uiModel, USPhoneValidator.class);
        Assert.assertEquals(validator, convertedValidator);
    }

    @Test
    public void testUSZipCodeValidator() {
        USZipCodeValidator validator = USZipCodeValidator.instance();
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        USZipCodeValidator convertedValidator = fromUI(uiModel, USZipCodeValidator.class);
        Assert.assertEquals(validator, convertedValidator);
    }

    @Test
    public void testNotNullValidator() {
        NotNullValidator validator = new NotNullValidator(false, true);
        FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
        NotNullValidator convertedValidator = fromUI(uiModel, NotNullValidator.class);
        Assert.assertFalse(convertedValidator.isAllowEmptyString());
        Assert.assertTrue(convertedValidator.isTrimString());

    }


    private <T extends ValidationPolicy> T fromUI(FieldValidationRule uiModel, Class<T> policyClass) {
        try {
            ValidationPolicy policy = ValidatorAnnotationTransformer.instance().fromUiModel(uiModel);
            return (T) policy;
        } catch (PolicyTransformException e) {
            e.printStackTrace();
            ;
        }
        return null;
    }


}
