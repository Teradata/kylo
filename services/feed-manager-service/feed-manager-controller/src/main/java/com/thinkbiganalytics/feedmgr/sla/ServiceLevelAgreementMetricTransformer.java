package com.thinkbiganalytics.feedmgr.sla;


import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementMetric;
import com.thinkbiganalytics.policy.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policy.ReflectionPolicyAnnotationDiscoverer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.GenericBaseUiPolicyRuleBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 4/21/16. Transforms UI Model to/from Metric class
 */
public class ServiceLevelAgreementMetricTransformer
    extends BasePolicyAnnotationTransformer<ServiceLevelAgreementRule, Metric, ServiceLevelAgreementMetric> implements ServiceLevelAgreementTransformer {

    private static final ServiceLevelAgreementMetricTransformer instance = new ServiceLevelAgreementMetricTransformer();

    @Override
    public ServiceLevelAgreementRule buildUiModel(ServiceLevelAgreementMetric annotation, Metric policy,
                                                  List<FieldRuleProperty> properties) {
      return buildUiModel(annotation,policy.getClass(),properties);
    }


    private ServiceLevelAgreementRule buildUiModel(ServiceLevelAgreementMetric annotation, Class policyClass,
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
            (ServiceLevelAgreementRule) new GenericBaseUiPolicyRuleBuilder<ServiceLevelAgreementRule>(ServiceLevelAgreementRule.class, annotation.name()).objectClassType(policyClass)
                .description(desc).shortDescription(shortDesc).addProperties(properties).build();
        return rule;
    }





    public List<ServiceLevelAgreementRule> discoverSlaMetrics() {

        List<ServiceLevelAgreementRule> rules = new ArrayList<>();
        Set<Class<?>>
            metrics = ReflectionPolicyAnnotationDiscoverer.getTypesAnnotatedWith(ServiceLevelAgreementMetric.class);
        for (Class c : metrics) {
            List<FieldRuleProperty> properties = getUiProperties(c);
            ServiceLevelAgreementMetric policy = (ServiceLevelAgreementMetric) c.getAnnotation(ServiceLevelAgreementMetric.class);
            rules.add(buildUiModel(policy,c,properties));
        }
        return rules;
    }




        @Override
    public Class<ServiceLevelAgreementMetric> getAnnotationClass() {
        return ServiceLevelAgreementMetric.class;
    }

    public static ServiceLevelAgreementMetricTransformer instance() {
        return instance;
    }
}
