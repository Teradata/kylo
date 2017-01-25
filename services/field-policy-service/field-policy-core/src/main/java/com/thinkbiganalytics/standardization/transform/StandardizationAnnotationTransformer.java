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
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.standardization.Standardizer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRuleBuilder;

import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public class StandardizationAnnotationTransformer
    extends BasePolicyAnnotationTransformer<FieldStandardizationRule, StandardizationPolicy, Standardizer> implements StandardizationTransformer {

  private static final StandardizationAnnotationTransformer instance = new StandardizationAnnotationTransformer();

  @Override
  public FieldStandardizationRule buildUiModel(Standardizer annotation, StandardizationPolicy policy,
                                               List<FieldRuleProperty> properties) {
    FieldStandardizationRule
        rule =
        new FieldStandardizationRuleBuilder(annotation.name()).objectClassType(policy.getClass()).description(
            annotation.description()).addProperties(properties).build();
    return rule;
  }

  @Override
  public Class<Standardizer> getAnnotationClass() {
    return Standardizer.class;
  }

  public static StandardizationAnnotationTransformer instance() {
    return instance;
  }
}
