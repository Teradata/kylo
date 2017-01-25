package com.thinkbiganalytics.policy;

/*-
 * #%L
 * thinkbig-field-policy-core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.standardization.transform.StandardizationAnnotationTransformer;
import com.thinkbiganalytics.validation.transform.ValidatorAnnotationTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 4/22/16.
 */
public class FieldPolicyTransformer {

  private com.thinkbiganalytics.policy.rest.model.FieldPolicy uiFieldPolicy;
  private FieldPolicyTransformerListener listener;

  public FieldPolicyTransformer(com.thinkbiganalytics.policy.rest.model.FieldPolicy uiFieldPolicy) {
    this.uiFieldPolicy = uiFieldPolicy;
  }

  public void setListener(FieldPolicyTransformerListener listener) {
    this.listener = listener;
  }

  public List<StandardizationPolicy> getStandardizationPolicies() {
    List<StandardizationPolicy> policies = new ArrayList<>();
    List<FieldStandardizationRule> rules = uiFieldPolicy.getStandardization();
    if (rules != null) {
      for (FieldStandardizationRule rule : rules) {
        try {
          StandardizationPolicy policy = StandardizationAnnotationTransformer.instance().fromUiModel(rule);
          policies.add(policy);
          if (listener != null) {
            listener.onAddStandardizationPolicy(policy);
          }
        } catch (PolicyTransformException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return policies;
  }

  public List<ValidationPolicy> getValidationPolicies() {
    List<ValidationPolicy> policies = new ArrayList<>();
    List<FieldValidationRule> rules = uiFieldPolicy.getValidation();
    if (rules != null) {
      for (FieldValidationRule rule : rules) {
        try {
          ValidationPolicy policy = ValidatorAnnotationTransformer.instance().fromUiModel(rule);
          policies.add(policy);
          if (listener != null) {
            listener.onAddValidationPolicy(policy);
          }
        } catch (PolicyTransformException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return policies;
  }

  public FieldPolicy buildPolicy() {
    List<StandardizationPolicy> standardizationPolicies = getStandardizationPolicies();
    List<ValidationPolicy> validators = getValidationPolicies();
    return FieldPolicyBuilder.newBuilder().fieldName(uiFieldPolicy.getFieldName()).feedFieldName(uiFieldPolicy.getFeedFieldName()).setProfile(uiFieldPolicy.isProfile()).addValidators(validators)
        .addStandardizers(standardizationPolicies).build();

  }

}
