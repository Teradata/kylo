package com.thinkbiganalytics.policy;


import com.thinkbiganalytics.policy.rest.model.BaseUiPolicyRule;

import java.lang.annotation.Annotation;

/**
 * Created by sr186054 on 4/21/16. Transforms  between a UI Policy into a Domain FieldPolicy and vice versa
 */
public interface PolicyTransformer<UI extends BaseUiPolicyRule, P extends Object, A extends Annotation> {

    UI toUIModel(P standardizationRule);

    P fromUiModel(UI rule)
        throws PolicyTransformException;


}
