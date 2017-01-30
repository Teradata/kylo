package com.thinkbiganalytics.policy;

/*-
 * #%L
 * thinkbig-field-policy-common
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


import com.thinkbiganalytics.policy.rest.model.BaseUiPolicyRule;

import java.lang.annotation.Annotation;

/**
 * Transforms  between a UI Policy into a Domain FieldPolicy and vice versa
 */
public interface PolicyTransformer<UI extends BaseUiPolicyRule, P extends Object, A extends Annotation> {

    UI toUIModel(P policyRule);

    P fromUiModel(UI rule)
        throws PolicyTransformException;


}
