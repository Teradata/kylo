package com.thinkbiganalytics.feedmgr.sla;


import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.policy.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.GenericBaseUiPolicyRuleBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by sr186054 on 4/21/16.
 */
public class ServiceLevelAgreementActionConfigTransformer
    extends BasePolicyAnnotationTransformer<ServiceLevelAgreementActionUiConfigurationItem, ServiceLevelAgreementActionConfiguration, ServiceLevelAgreementActionConfig> {

    private static final ServiceLevelAgreementActionConfigTransformer instance = new ServiceLevelAgreementActionConfigTransformer();

    public ServiceLevelAgreementActionUiConfigurationItem buildUiModel(ServiceLevelAgreementActionConfig annotation, ServiceLevelAgreementActionConfiguration policy,
                                                                       List<FieldRuleProperty> properties) {
        String desc = annotation.description();
        String shortDesc = annotation.shortDescription();
        if (StringUtils.isBlank(desc) && StringUtils.isNotBlank(shortDesc)) {
            desc = shortDesc;
        }
        if (StringUtils.isBlank(shortDesc) && StringUtils.isNotBlank(desc)) {
            shortDesc = desc;
        }

        ServiceLevelAgreementActionUiConfigurationItem
            rule =
            (ServiceLevelAgreementActionUiConfigurationItem) new GenericBaseUiPolicyRuleBuilder<ServiceLevelAgreementActionUiConfigurationItem>(ServiceLevelAgreementActionUiConfigurationItem.class,
                                                                                                                                                annotation.name()).objectClassType(policy.getClass())
                .description(
                    desc).shortDescription(shortDesc).addProperties(properties).build();
        rule.setActionClasses(Lists.newArrayList(annotation.actionClasses()));
        return rule;
    }

    @Override
    public void afterFromUiModel(ServiceLevelAgreementActionConfiguration policy, ServiceLevelAgreementActionUiConfigurationItem uiModel) {
        super.afterFromUiModel(policy, uiModel);
        policy.setActionClasses(uiModel.getActionClasses());
    }

    @Override
    public Class<ServiceLevelAgreementActionConfig> getAnnotationClass() {
        return ServiceLevelAgreementActionConfig.class;
    }

    public static ServiceLevelAgreementActionConfigTransformer instance() {
        return instance;
    }
}
