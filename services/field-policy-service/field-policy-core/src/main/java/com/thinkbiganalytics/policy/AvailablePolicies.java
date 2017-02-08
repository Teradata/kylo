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
 * Utility class to get the list of Standardization and Validation rules for the User interface
 */
public class AvailablePolicies {


    public static List<FieldStandardizationRule> discoverStandardizationRules() {

        List<FieldStandardizationRule> rules = new ArrayList<>();
        Set<Class<?>>
            standardizers = ReflectionPolicyAnnotationDiscoverer.getTypesAnnotatedWith(Standardizer.class);
        for (Class c : standardizers) {
            Standardizer standardizer = (Standardizer) c.getAnnotation(Standardizer.class);
            List<FieldRuleProperty> properties = StandardizationAnnotationTransformer.instance().getUiProperties(c);
            rules.add(new FieldStandardizationRuleBuilder(standardizer.name()).description(standardizer.description())
                          .addProperties(properties).objectClassType(c).build());
        }
        return rules;
    }

    public static List<FieldValidationRule> discoverValidationRules() {

        List<FieldValidationRule> rules = new ArrayList<>();
        Set<Class<?>>
            validators = ReflectionPolicyAnnotationDiscoverer.getTypesAnnotatedWith(Validator.class);
        for (Class c : validators) {
            Validator validator = (Validator) c.getAnnotation(Validator.class);
            List<FieldRuleProperty> properties = ValidatorAnnotationTransformer.instance().getUiProperties(c);
            rules.add(new FieldValidationRuleBuilder(validator.name()).description(validator.description())
                          .addProperties(properties).objectClassType(c).build());
        }
        return rules;
    }


}
