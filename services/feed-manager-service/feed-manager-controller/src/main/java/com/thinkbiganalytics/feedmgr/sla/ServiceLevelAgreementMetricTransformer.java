package com.thinkbiganalytics.feedmgr.sla;


import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementMetric;
import com.thinkbiganalytics.policy.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.GenericBaseUiPolicyRuleBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by sr186054 on 4/21/16. Transforms UI Model to/from Metric class
 */
public class ServiceLevelAgreementMetricTransformer
    extends BasePolicyAnnotationTransformer<ServiceLevelAgreementRule, Metric, ServiceLevelAgreementMetric> implements ServiceLevelAgreementTransformer {

    private static final ServiceLevelAgreementMetricTransformer instance = new ServiceLevelAgreementMetricTransformer();

    @Override
    public ServiceLevelAgreementRule buildUiModel(ServiceLevelAgreementMetric annotation, Metric policy,
                                                  List<FieldRuleProperty> properties) {
        String desc = annotation.description();
        String shortDesc = annotation.shortDescription();
        if (StringUtils.isBlank(desc) && StringUtils.isNotBlank(shortDesc)) {
            desc = shortDesc;
        }
        if (StringUtils.isBlank(shortDesc) && StringUtils.isNotBlank(desc)) {
            shortDesc = desc;
        }

        ServiceLevelAgreementRule
            rule =
            (ServiceLevelAgreementRule) new GenericBaseUiPolicyRuleBuilder<ServiceLevelAgreementRule>(ServiceLevelAgreementRule.class, annotation.name()).objectClassType(policy.getClass())
                .description(
                    desc).shortDescription(shortDesc).addProperties(properties).build();
        return rule;
    }

    @Override
    public Class<ServiceLevelAgreementMetric> getAnnotationClass() {
        return ServiceLevelAgreementMetric.class;
    }

    public static ServiceLevelAgreementMetricTransformer instance() {
        return instance;
    }
}
