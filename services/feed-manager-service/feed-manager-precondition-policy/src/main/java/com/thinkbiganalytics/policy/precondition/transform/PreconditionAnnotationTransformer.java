package com.thinkbiganalytics.policy.precondition.transform;


import com.thinkbiganalytics.policy.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policy.precondition.Precondition;
import com.thinkbiganalytics.policy.precondition.PreconditionPolicy;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.policy.rest.model.PreconditionRuleBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public class PreconditionAnnotationTransformer
    extends BasePolicyAnnotationTransformer<PreconditionRule, Precondition, PreconditionPolicy> implements PreconditionTransformer {

    private static final PreconditionAnnotationTransformer instance = new PreconditionAnnotationTransformer();

    @Override
    public PreconditionRule buildUiModel(PreconditionPolicy annotation, Precondition policy,
                                         List<FieldRuleProperty> properties) {
        String desc = annotation.description();
        String shortDesc = annotation.shortDescription();
        if (StringUtils.isBlank(desc) && StringUtils.isNotBlank(shortDesc)) {
            desc = shortDesc;
        }
        if (StringUtils.isBlank(shortDesc) && StringUtils.isNotBlank(desc)) {
            shortDesc = desc;
        }

        PreconditionRule
            rule =
            new PreconditionRuleBuilder(annotation.name()).objectClassType(policy.getClass()).description(
                desc).shortDescription(shortDesc).addProperties(properties).build();
        return rule;
    }

    @Override
    public Class<PreconditionPolicy> getAnnotationClass() {
        return PreconditionPolicy.class;
    }

    public static PreconditionAnnotationTransformer instance() {
        return instance;
    }
}
