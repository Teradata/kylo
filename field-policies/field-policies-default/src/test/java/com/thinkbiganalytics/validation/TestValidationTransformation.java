package com.thinkbiganalytics.validation;

import com.thinkbiganalytics.com.thinkbiganalytics.validation.CreditCardValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.EmailValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.IPAddressValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.LengthValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.LookupValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.RangeValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.RegexValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.TimestampValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.USPhoneValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.USZipCodeValidator;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldValidationRule;
import com.thinkbiganalytics.policies.PolicyTransformException;
import com.thinkbiganalytics.policies.validation.Validator;
import com.thinkbiganalytics.validation.transform.ValidatorAnnotationTransformer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by sr186054 on 4/5/16.
 */
public class TestValidationTransformation {


  @Test
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
    Assert.assertEquals(min.intValue(), convertedValidator.getMaxLength());
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
    TimestampValidator validator = TimestampValidator.instance();
    FieldValidationRule uiModel = ValidatorAnnotationTransformer.instance().toUIModel(validator);
    TimestampValidator convertedValidator = fromUI(uiModel, TimestampValidator.class);
    Assert.assertEquals(validator, convertedValidator);
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


  private <T extends Validator> T fromUI(FieldValidationRule uiModel, Class<T> policyClass) {
    try {
      Validator policy = ValidatorAnnotationTransformer.instance().fromUiModel(uiModel);
      return (T) policy;
    } catch (PolicyTransformException e) {
      e.printStackTrace();
      ;
    }
    return null;
  }


}
