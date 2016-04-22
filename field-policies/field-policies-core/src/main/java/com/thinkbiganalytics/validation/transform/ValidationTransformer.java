package com.thinkbiganalytics.validation.transform;

import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldValidationRule;
import com.thinkbiganalytics.policies.PolicyTransformException;
import com.thinkbiganalytics.policies.validation.Validator;

/**
 * Created by sr186054 on 4/21/16.
 */
public interface ValidationTransformer {

  FieldValidationRule toUIModel(Validator standardizationRule);

  Validator fromUiModel(FieldValidationRule rule)
      throws PolicyTransformException;


}
