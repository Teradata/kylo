package com.thinkbiganalytics.standardization;

import com.thinkbiganalytics.com.thinkbiganalytics.standardization.DateTimeStandardizer;
import com.thinkbiganalytics.com.thinkbiganalytics.standardization.DefaultValueStandardizer;
import com.thinkbiganalytics.com.thinkbiganalytics.standardization.MaskLeavingLastFourDigitStandardizer;
import com.thinkbiganalytics.com.thinkbiganalytics.standardization.RemoveControlCharsStandardizer;
import com.thinkbiganalytics.com.thinkbiganalytics.standardization.SimpleRegexReplacer;
import com.thinkbiganalytics.com.thinkbiganalytics.standardization.StripNonNumeric;
import com.thinkbiganalytics.com.thinkbiganalytics.standardization.UppercaseStandardizer;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldStandardizationRule;
import com.thinkbiganalytics.policies.PolicyTransformException;
import com.thinkbiganalytics.policies.standardization.StandardizationPolicy;
import com.thinkbiganalytics.standardization.transform.StandardizationAnnotationTransformer;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by sr186054 on 4/5/16.
 */
public class TestStandardizationTransform {


  @Test
  public void testDefaultValue() throws IOException {
    String INPUT = "My Default";
    DefaultValueStandardizer standardizer = new DefaultValueStandardizer(INPUT);
    FieldStandardizationRule uiModel = StandardizationAnnotationTransformer.instance().toUIModel(standardizer);
    DefaultValueStandardizer convertedPolicy = fromUI(uiModel, DefaultValueStandardizer.class);
    Assert.assertEquals(INPUT, convertedPolicy.getDefaultStr());
  }


  @Test
  public void testDateTime() throws IOException {
    String FORMAT = "MM/dd/YYYY";
    DateTimeStandardizer standardizer = new DateTimeStandardizer(FORMAT, DateTimeStandardizer.OutputFormats.DATETIME_NOMILLIS);
    FieldStandardizationRule uiModel = StandardizationAnnotationTransformer.instance().toUIModel(standardizer);
    DateTimeStandardizer convertedPolicy = fromUI(uiModel, DateTimeStandardizer.class);
    Assert.assertEquals(FORMAT, convertedPolicy.getInputDateFormat());
    Assert.assertEquals(DateTimeStandardizer.OutputFormats.DATETIME_NOMILLIS, convertedPolicy.getOutputFormat());
  }

  @Test
  public void testRemoveControlCharsStandardizer() throws IOException {
    RemoveControlCharsStandardizer standardizer = RemoveControlCharsStandardizer.instance();
    FieldStandardizationRule uiModel = StandardizationAnnotationTransformer.instance().toUIModel(standardizer);
    RemoveControlCharsStandardizer convertedPolicy = fromUI(uiModel, RemoveControlCharsStandardizer.class);
    Assert.assertEquals(standardizer, convertedPolicy);
  }

  @Test
  public void testUppercaseStandardizer() throws IOException {
    UppercaseStandardizer standardizer = UppercaseStandardizer.instance();
    FieldStandardizationRule uiModel = StandardizationAnnotationTransformer.instance().toUIModel(standardizer);
    UppercaseStandardizer convertedPolicy = fromUI(uiModel, UppercaseStandardizer.class);
    Assert.assertEquals(standardizer, convertedPolicy);
  }

  @Test
  public void testStripNonNumeric() throws IOException {
    StripNonNumeric standardizer = StripNonNumeric.instance();
    FieldStandardizationRule uiModel = StandardizationAnnotationTransformer.instance().toUIModel(standardizer);
    StripNonNumeric convertedPolicy = fromUI(uiModel, StripNonNumeric.class);
    Assert.assertEquals(standardizer, convertedPolicy);
  }

  @Test
  public void testMaskLeavingLastFourDigitStandardizer() throws IOException {
    MaskLeavingLastFourDigitStandardizer standardizer = MaskLeavingLastFourDigitStandardizer.instance();
    FieldStandardizationRule uiModel = StandardizationAnnotationTransformer.instance().toUIModel(standardizer);
    MaskLeavingLastFourDigitStandardizer convertedPolicy = fromUI(uiModel, MaskLeavingLastFourDigitStandardizer.class);
    Assert.assertEquals(standardizer, convertedPolicy);
  }


  @Test
  public void testSimpleRegexReplacer() throws IOException {
    String regex = "\\p{Cc}";
    String replace = "REPLACE";
    SimpleRegexReplacer standardizer = new SimpleRegexReplacer(regex, replace);
    FieldStandardizationRule uiModel = StandardizationAnnotationTransformer.instance().toUIModel(standardizer);
    SimpleRegexReplacer convertedPolicy = fromUI(uiModel, SimpleRegexReplacer.class);
    Assert.assertEquals(regex, convertedPolicy.getPattern().pattern());
    Assert.assertEquals(replace, convertedPolicy.getReplacement());
    Assert.assertEquals(true, convertedPolicy.isValid());

  }


  private <T extends StandardizationPolicy> T fromUI(FieldStandardizationRule uiModel, Class<T> policyClass) {
    try {
      StandardizationPolicy policy = StandardizationAnnotationTransformer.instance().fromUiModel(uiModel);
      return (T) policy;
    } catch (PolicyTransformException e) {
      e.printStackTrace();
      ;
    }
    return null;
  }


}
