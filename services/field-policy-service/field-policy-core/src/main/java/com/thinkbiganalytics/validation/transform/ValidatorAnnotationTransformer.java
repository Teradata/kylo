package com.thinkbiganalytics.validation.transform;

import com.thinkbiganalytics.policy.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policy.validation.Validator;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRuleBuilder;

import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public class ValidatorAnnotationTransformer
    extends BasePolicyAnnotationTransformer<FieldValidationRule, ValidationPolicy, Validator> implements ValidationTransformer {

  private static final ValidatorAnnotationTransformer instance = new ValidatorAnnotationTransformer();

  @Override
  public FieldValidationRule buildUiModel(Validator annotation, ValidationPolicy policy,
                                          List<FieldRuleProperty> properties) {

    FieldValidationRule rule = new FieldValidationRuleBuilder(annotation.name()).objectClassType(policy.getClass()).description(
        annotation.description()).addProperties(properties).build();
    return rule;
  }

  @Override
  public Class<Validator> getAnnotationClass() {
    return Validator.class;
  }

  public static ValidatorAnnotationTransformer instance() {
    return instance;
  }
}
