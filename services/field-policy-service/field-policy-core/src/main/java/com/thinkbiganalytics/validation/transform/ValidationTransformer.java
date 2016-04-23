package com.thinkbiganalytics.validation.transform;

import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;

/**
 * Created by sr186054 on 4/21/16.
 */
public interface ValidationTransformer {

  FieldValidationRule toUIModel(ValidationPolicy standardizationRule);

  ValidationPolicy fromUiModel(FieldValidationRule rule)
      throws PolicyTransformException;


}
