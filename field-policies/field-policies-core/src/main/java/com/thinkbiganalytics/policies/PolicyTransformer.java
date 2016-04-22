package com.thinkbiganalytics.policies;

import com.thinkbiganalytics.feedmgr.rest.model.schema.BaseUiPolicyRule;

import java.lang.annotation.Annotation;

/**
 * Created by sr186054 on 4/21/16.
 */
public interface PolicyTransformer<UI extends BaseUiPolicyRule,P extends FieldPolicyItem, A extends Annotation> {

  UI toUIModel(P standardizationRule);

  P fromUiModel(UI rule)
      throws PolicyTransformException;


}
