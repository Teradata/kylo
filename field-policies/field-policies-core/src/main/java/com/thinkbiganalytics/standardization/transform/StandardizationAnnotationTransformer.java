package com.thinkbiganalytics.standardization.transform;

import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldRuleProperty;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldStandardizationRule;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldStandardizationRuleBuilder;
import com.thinkbiganalytics.policies.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policies.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policies.standardization.Standardizer;

import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public class StandardizationAnnotationTransformer
    extends BasePolicyAnnotationTransformer<FieldStandardizationRule, StandardizationPolicy, Standardizer> {

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
