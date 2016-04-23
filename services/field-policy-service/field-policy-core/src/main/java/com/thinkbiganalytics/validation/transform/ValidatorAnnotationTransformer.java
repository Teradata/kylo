package com.thinkbiganalytics.validation.transform;

import com.thinkbiganalytics.policies.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policies.validation.FieldValidator;
import com.thinkbiganalytics.policies.validation.Validator;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRuleBuilder;

import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public class ValidatorAnnotationTransformer
    extends BasePolicyAnnotationTransformer<FieldValidationRule, Validator, FieldValidator> implements ValidationTransformer {

  private static final ValidatorAnnotationTransformer instance = new ValidatorAnnotationTransformer();

  @Override
  public FieldValidationRule buildUiModel(FieldValidator annotation, Validator policy,
                                          List<FieldRuleProperty> properties) {

    FieldValidationRule rule = new FieldValidationRuleBuilder(annotation.name()).objectClassType(policy.getClass()).description(
        annotation.description()).addProperties(properties).build();
    return rule;
  }

  @Override
  public Class<FieldValidator> getAnnotationClass() {
    return FieldValidator.class;
  }

  public static ValidatorAnnotationTransformer instance() {
    return instance;
  }
}
