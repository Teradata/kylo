package com.thinkbiganalytics.policy;

import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRuleBuilder;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRuleBuilder;
import com.thinkbiganalytics.policy.standardization.Standardizer;
import com.thinkbiganalytics.policy.validation.Validator;
import com.thinkbiganalytics.standardization.transform.StandardizationAnnotationTransformer;
import com.thinkbiganalytics.validation.transform.ValidatorAnnotationTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 4/22/16.
 */
public class AvailablePolicies {


  public static List<FieldStandardizationRule> discoverStandardizationRules(){

    List<FieldStandardizationRule> rules = new ArrayList <>();
    Set<Class<?>>
        standardizers = ReflectionPolicyAnnotationDiscoverer.getTypesAnnotatedWith(Standardizer.class);
    for(Class c: standardizers){
      Standardizer standardizer = (Standardizer) c.getAnnotation(Standardizer.class);
      List<FieldRuleProperty> properties = StandardizationAnnotationTransformer.instance().getUiProperties(c);
      rules.add(new FieldStandardizationRuleBuilder(standardizer.name()).description(standardizer.description())
                    .addProperties(properties).objectClassType(c).build());
    }
    return rules;
  }

  public static List<FieldValidationRule> discoverValidationRules(){

    List<FieldValidationRule> rules = new ArrayList <>();
    Set<Class<?>>
        validators = ReflectionPolicyAnnotationDiscoverer.getTypesAnnotatedWith(Validator.class);
    for(Class c: validators){
      Validator validator = (Validator) c.getAnnotation(Validator.class);
      List<FieldRuleProperty> properties = ValidatorAnnotationTransformer.instance().getUiProperties(c);
      rules.add(new FieldValidationRuleBuilder(validator.name()).description(validator.description())
                    .addProperties(properties).objectClassType(c).build());
    }
    return rules;
  }


}
