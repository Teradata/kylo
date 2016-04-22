package com.thinkbiganalytics.policies;

import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldStandardizationRule;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldValidationRule;

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
    Assert.assertTrue(standardizationRules.size() >0);
    Assert.assertNotNull(validationRules.size() >0);

  }
}
