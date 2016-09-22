package com.thinkbiganalytics.policy;

import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sr186054 on 4/22/16.
 */
public class AvailablePoliciesTest {

  private static final Logger log = LoggerFactory.getLogger(AvailablePoliciesTest.class);

  @Test
  public void testAvailablePolicies(){

    List<FieldStandardizationRule> standardizationRules = AvailablePolicies.discoverStandardizationRules();
    List<FieldValidationRule> validationRules = AvailablePolicies.discoverValidationRules();

    log.info("Available Standardizers: {}, Validators: {} ", standardizationRules.size(), validationRules.size());

    Assert.assertTrue(standardizationRules.size() > 0);
    Assert.assertTrue(validationRules.size() > 0);

    FieldStandardizationRule rule = standardizationRules.get(0);
    log.info("First Standardizer is {}",rule);
    Assert.assertNotNull(rule.getObjectClassType());

  }


}
