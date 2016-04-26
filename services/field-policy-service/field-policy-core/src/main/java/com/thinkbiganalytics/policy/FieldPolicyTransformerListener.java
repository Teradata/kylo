package com.thinkbiganalytics.policy;

import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;

/**
 * Created by sr186054 on 4/25/16.
 */
public interface FieldPolicyTransformerListener {

  public void onAddValidationPolicy(ValidationPolicy policy);

  public void onAddStandardizationPolicy(StandardizationPolicy policy);

}
