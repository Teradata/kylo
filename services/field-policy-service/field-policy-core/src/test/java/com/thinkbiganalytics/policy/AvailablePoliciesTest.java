package com.thinkbiganalytics.policy;

import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;

import org.junit.Assert;

import org.junit.Test;

import java.util.List;

/**
 * Created by sr186054 on 4/22/16.
 */
public class AvailablePoliciesTest {


  @Test
  public void testAvailablePolicies(){

    List<FieldStandardizationRule> standardizationRules = AvailablePolicies.discoverStandardizationRules();
    List<FieldValidationRule> validationRules = AvailablePolicies.discoverValidationRules();
    Assert.assertTrue(standardizationRules.size() > 0);
    Assert.assertNotNull(validationRules.size() > 0);

    FieldStandardizationRule rule = standardizationRules.get(0);
    Assert.assertNotNull(rule.getObjectClassType());

  }


}
