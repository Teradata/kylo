package com.thinkbiganalytics.validation.transform;

import com.thinkbiganalytics.policies.PolicyTransformException;
import com.thinkbiganalytics.policies.validation.Validator;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;

/**
 * Created by sr186054 on 4/21/16.
 */
public interface ValidationTransformer {

  FieldValidationRule toUIModel(Validator standardizationRule);

  Validator fromUiModel(FieldValidationRule rule)
      throws PolicyTransformException;


}
