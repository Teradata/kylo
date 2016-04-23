package com.thinkbiganalytics.policies;

import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldRuleProperty;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldStandardizationRule;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldStandardizationRuleBuilder;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldValidationRule;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldValidationRuleBuilder;
import com.thinkbiganalytics.policies.standardization.Standardizer;
import com.thinkbiganalytics.policies.validation.FieldValidator;
import com.thinkbiganalytics.standardization.transform.StandardizationAnnotationTransformer;
import com.thinkbiganalytics.validation.transform.ValidatorAnnotationTransformer;

import org.reflections.Reflections;

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
        standardizers =new Reflections("com.thinkbiganalytics").getTypesAnnotatedWith(Standardizer.class);
    for(Class c: standardizers){
      Standardizer standardizer = (Standardizer) c.getAnnotation(Standardizer.class);
      List<FieldRuleProperty> properties = StandardizationAnnotationTransformer.instance().getUiProperties(c);
      rules.add(new FieldStandardizationRuleBuilder(standardizer.name()).description(standardizer.description())
                    .addProperties(properties).build());
    }
    return rules;
  }

  public static List<FieldValidationRule> discoverValidationRules(){

    List<FieldValidationRule> rules = new ArrayList <>();
    Set<Class<?>>
        validators =new Reflections("com.thinkbiganalytics").getTypesAnnotatedWith(FieldValidator.class);
    for(Class c: validators){
      FieldValidator validator = (FieldValidator) c.getAnnotation(FieldValidator.class);
      List<FieldRuleProperty> properties = ValidatorAnnotationTransformer.instance().getUiProperties(c);
      rules.add(new FieldValidationRuleBuilder(validator.name()).description(validator.description())
                    .addProperties(properties).build());
    }
    return rules;
  }

}
