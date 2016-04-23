package com.thinkbiganalytics.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.com.thinkbiganalytics.policy.standardization.DateTimeStandardizer;
import com.thinkbiganalytics.com.thinkbiganalytics.policy.standardization.DefaultValueStandardizer;
import com.thinkbiganalytics.com.thinkbiganalytics.policy.validation.RangeValidator;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldPolicyBuilder;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldStandardizationRule;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldValidationRule;
import com.thinkbiganalytics.standardization.transform.StandardizationAnnotationTransformer;
import com.thinkbiganalytics.validation.transform.ValidatorAnnotationTransformer;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 4/22/16.
 */
public class TestJsonPolicies {


  @Test
  public void testJson() throws IOException {
    List<com.thinkbiganalytics.feedmgr.rest.model.schema.FieldPolicy> fieldPolicies = new ArrayList<>();
    List<FieldStandardizationRule> standardizationPolicyList = new ArrayList<>();
    List<FieldValidationRule> validationRules = new ArrayList<>();

    DefaultValueStandardizer defaultValueStandardizer = new DefaultValueStandardizer("My Default");
    standardizationPolicyList.add(StandardizationAnnotationTransformer.instance().toUIModel(defaultValueStandardizer));

    DateTimeStandardizer
        dateTimeStandardizer =
        new DateTimeStandardizer("MM/DD/YYYY", DateTimeStandardizer.OutputFormats.DATETIME_NOMILLIS);
    standardizationPolicyList.add(StandardizationAnnotationTransformer.instance().toUIModel(dateTimeStandardizer));

    RangeValidator validator = new RangeValidator(10, 20);
    validationRules.add(ValidatorAnnotationTransformer.instance().toUIModel(validator));

    fieldPolicies.add(new FieldPolicyBuilder("Field1").addStandardization(standardizationPolicyList).addValidations(
        validationRules).build());
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(fieldPolicies);

    FieldPoliciesJsonTransformer fieldPolicyTransformer = new FieldPoliciesJsonTransformer(json);
    Map<String, com.thinkbiganalytics.policy.FieldPolicy> policyMap = fieldPolicyTransformer.buildPolicies();
    com.thinkbiganalytics.policy.FieldPolicy field1Policy = policyMap.get("Field1");
    Assert.assertEquals(2, field1Policy.getStandardizationPolicies().size());
    Assert.assertEquals(1, field1Policy.getValidators().size());

  }

}
