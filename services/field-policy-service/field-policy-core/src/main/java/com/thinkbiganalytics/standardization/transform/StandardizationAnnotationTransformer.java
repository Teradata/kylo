package com.thinkbiganalytics.standardization.transform;

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

import com.thinkbiganalytics.policy.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRuleBuilder;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.standardization.Standardizer;

import java.util.List;

/**
 * Transformation class to convert domain {@link StandardizationPolicy} to/from ui objects {@link FieldStandardizationRule}
 */
public class StandardizationAnnotationTransformer
    extends BasePolicyAnnotationTransformer<FieldStandardizationRule, StandardizationPolicy, Standardizer> implements StandardizationTransformer {

    private static final StandardizationAnnotationTransformer instance = new StandardizationAnnotationTransformer();

    public static StandardizationAnnotationTransformer instance() {
        return instance;
    }

    /**
     * Create the User interface object
     *
     * @param annotation the class annotation to look for
     * @param policy     the domain object needed to convert
     * @param properties the fields that should be used/injected when creating the ui object t
     * @return the converted user interface object
     */
    @Override
    public FieldStandardizationRule buildUiModel(Standardizer annotation, StandardizationPolicy policy,
                                                 List<FieldRuleProperty> properties) {
        FieldStandardizationRule
            rule =
            new FieldStandardizationRuleBuilder(annotation.name()).objectClassType(policy.getClass()).description(
                annotation.description()).addProperties(properties).build();
        return rule;
    }

    /**
     * Return the Standardizer annotation to look for
     *
     * @return the Standardizer annotation class
     */
    @Override
    public Class<Standardizer> getAnnotationClass() {
        return Standardizer.class;
    }
}
