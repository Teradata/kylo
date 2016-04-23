package com.thinkbiganalytics.policies;

import com.thinkbiganalytics.policies.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policies.validation.Validator;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.standardization.transform.StandardizationAnnotationTransformer;
import com.thinkbiganalytics.validation.transform.ValidatorAnnotationTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 4/22/16.
 */
public class FieldPolicyTransformer {

  private com.thinkbiganalytics.policy.rest.model.FieldPolicy uiFieldPolicy;

  public FieldPolicyTransformer(com.thinkbiganalytics.policy.rest.model.FieldPolicy uiFieldPolicy) {
    this.uiFieldPolicy = uiFieldPolicy;
  }


  public List<StandardizationPolicy> getStandardizationPolicies() {
    List<StandardizationPolicy> policies = new ArrayList<>();
    List<FieldStandardizationRule> rules = uiFieldPolicy.getStandardization();
    for (FieldStandardizationRule rule : rules) {
      try {
        StandardizationPolicy policy = StandardizationAnnotationTransformer.instance().fromUiModel(rule);
        policies.add(policy);
      } catch (PolicyTransformException e) {
        e.printStackTrace();
      }
    }
    return policies;
  }

  public List<Validator> getValidationPolicies() {
    List<Validator> policies = new ArrayList<>();
    List<FieldValidationRule> rules = uiFieldPolicy.getValidation();
    for (FieldValidationRule rule : rules) {
      try {
        Validator policy = ValidatorAnnotationTransformer.instance().fromUiModel(rule);
        policies.add(policy);
      } catch (PolicyTransformException e) {
        e.printStackTrace();
      }
    }
    return policies;
  }

  public FieldPolicy buildPolicy() {
    List<StandardizationPolicy> standardizationPolicies = getStandardizationPolicies();
    List<Validator> validators = getValidationPolicies();
    return FieldPolicyBuilder.newBuilder().fieldName(uiFieldPolicy.getFieldName()).addValidators(validators)
        .addStandardizers(standardizationPolicies).build();

  }

}
