package com.thinkbiganalytics.standardization;

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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.policy.AvailablePolicies;
import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.standardization.DateTimeStandardizer;
import com.thinkbiganalytics.policy.standardization.DefaultValueStandardizer;
import com.thinkbiganalytics.policy.standardization.MaskLeavingLastFourDigitStandardizer;
import com.thinkbiganalytics.policy.standardization.RemoveControlCharsStandardizer;
import com.thinkbiganalytics.policy.standardization.SimpleRegexReplacer;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.standardization.StripNonNumeric;
import com.thinkbiganalytics.policy.standardization.UppercaseStandardizer;
import com.thinkbiganalytics.standardization.transform.StandardizationAnnotationTransformer;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
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

    @Test
    public void testUiCreation() {
        List<FieldStandardizationRule> standardizationRules = AvailablePolicies.discoverStandardizationRules();
        FieldStandardizationRule defaultValue = Iterables.tryFind(standardizationRules, new Predicate<FieldStandardizationRule>() {
            @Override
            public boolean apply(FieldStandardizationRule fieldStandardizationRule) {
                return fieldStandardizationRule.getName().equalsIgnoreCase("Default Value");
            }
        }).orNull();

        defaultValue.getProperty("Default Value").setValue("a new default value");
        DefaultValueStandardizer convertedPolicy = fromUI(defaultValue, DefaultValueStandardizer.class);
        Assert.assertEquals("a new default value", convertedPolicy.getDefaultStr());
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
