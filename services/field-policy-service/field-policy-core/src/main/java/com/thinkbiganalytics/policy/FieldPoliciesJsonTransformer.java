package com.thinkbiganalytics.policy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 4/22/16.
 */
public class FieldPoliciesJsonTransformer {

  private static final Logger log = LoggerFactory.getLogger(FieldPoliciesJsonTransformer.class);
  /**
   * JSON ARRY OF com.thinkbiganalytics.policy.rest.model.FieldPolicy objects
   */
  private String jsonFieldPolicies;

  List<com.thinkbiganalytics.policy.rest.model.FieldPolicy>
      uiFieldPolicies;

  public FieldPoliciesJsonTransformer(String jsonFieldPolicies) {
    this.jsonFieldPolicies = jsonFieldPolicies;
    ObjectMapper mapper = new ObjectMapper();
    try {
      uiFieldPolicies =
          mapper.readValue(jsonFieldPolicies,
                           new TypeReference<List<com.thinkbiganalytics.policy.rest.model.FieldPolicy>>() {
                           });

    } catch (Exception e) {
      e.printStackTrace();
      log.error("ERROR converting Field Policy JSON to Rest Models : {}", e.getMessage(), e);
    }


  }

  public Map<String, FieldPolicy> buildPolicies() {

    Map<String, FieldPolicy> fieldPolicyMap = new HashMap<>();
    PolicyTransformationListener listener = new PolicyTransformationListener();
    if (uiFieldPolicies != null) {
      for (com.thinkbiganalytics.policy.rest.model.FieldPolicy uiFieldPolicy : uiFieldPolicies) {
        FieldPolicyTransformer transformer = new FieldPolicyTransformer(uiFieldPolicy);
        transformer.setListener(listener);
        fieldPolicyMap.put(uiFieldPolicy.getFieldName(), transformer.buildPolicy());
      }
    }
    log.info("Transformed UI Policies to Field Policies.  {} ", listener.getCounts());
    return fieldPolicyMap;

  }

  private class PolicyTransformationListener implements FieldPolicyTransformerListener {

    private int validationCount = 0;
    private int standardizationCount = 0;

    @Override
    public void onAddValidationPolicy(ValidationPolicy policy) {
      validationCount++;
    }

    @Override
    public void onAddStandardizationPolicy(StandardizationPolicy policy) {
      standardizationCount++;
    }

    public String getCounts() {
      return "Total Validation Policies: " + validationCount + ", Total Standardization Policies: " + standardizationCount;
    }
  }

}
