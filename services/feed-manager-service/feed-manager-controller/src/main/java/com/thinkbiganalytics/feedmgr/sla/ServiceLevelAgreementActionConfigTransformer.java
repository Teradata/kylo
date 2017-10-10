package com.thinkbiganalytics.feedmgr.sla;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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


import com.google.common.collect.Lists;
import com.thinkbiganalytics.classnameregistry.ClassNameChangeRegistry;
import com.thinkbiganalytics.metadata.sla.alerts.ServiceLevelAgreementActionUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionValidation;
import com.thinkbiganalytics.policy.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policy.ReflectionPolicyAnnotationDiscoverer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.GenericBaseUiPolicyRuleBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Transform to/from  {@link ServiceLevelAgreementActionUiConfigurationItem} / {@link ServiceLevelAgreementActionConfiguration}
 */
public class ServiceLevelAgreementActionConfigTransformer
    extends BasePolicyAnnotationTransformer<ServiceLevelAgreementActionUiConfigurationItem, ServiceLevelAgreementActionConfiguration, ServiceLevelAgreementActionConfig> {

    private static final ServiceLevelAgreementActionConfigTransformer instance = new ServiceLevelAgreementActionConfigTransformer();

    public static ServiceLevelAgreementActionConfigTransformer instance() {
        return instance;
    }

    /**
     * Cache of the ActionConfiguration items discovered via inspecting the ServiceLevelAgreementActionConfig annotation
     */
    private static List<ServiceLevelAgreementActionUiConfigurationItem> actionConfigurationRegistry = null;

    public ServiceLevelAgreementActionUiConfigurationItem buildUiModel(ServiceLevelAgreementActionConfig annotation, ServiceLevelAgreementActionConfiguration policy,
                                                                       List<FieldRuleProperty> properties) {
        return buildUiModel(annotation, policy.getClass(), properties);
    }

    private ServiceLevelAgreementActionUiConfigurationItem buildUiModel(ServiceLevelAgreementActionConfig annotation, Class policyClass,
                                                                        List<FieldRuleProperty> properties) {
        String desc = annotation.description();
        String shortDesc = annotation.shortDescription();
        if (StringUtils.isBlank(desc) && StringUtils.isNotBlank(shortDesc)) {
            desc = shortDesc;
        }
        if (StringUtils.isBlank(shortDesc) && StringUtils.isNotBlank(desc)) {
            shortDesc = desc;
        }
        String velocityTemplateType = annotation.velocityTemplateType();

        ServiceLevelAgreementActionUiConfigurationItem
            rule =
            (ServiceLevelAgreementActionUiConfigurationItem) new GenericBaseUiPolicyRuleBuilder<ServiceLevelAgreementActionUiConfigurationItem>(ServiceLevelAgreementActionUiConfigurationItem.class,
                                                                                                                                                annotation.name()).objectClassType(policyClass)
                .description(
                    desc).shortDescription(shortDesc).addProperties(properties).build();
        rule.setActionClasses(Lists.newArrayList(annotation.actionClasses()));
        rule.setVelocityTemplateType(velocityTemplateType);
        return rule;
    }

    public List<ServiceLevelAgreementActionValidation> validateAction(String actionConfigurationClassName) {
        List<ServiceLevelAgreementActionValidation> validation = null;
        try {
            Class<? extends ServiceLevelAgreementActionConfiguration> configurationClass = ClassNameChangeRegistry.findClass(actionConfigurationClassName);
            ServiceLevelAgreementActionConfig annotation = (ServiceLevelAgreementActionConfig) configurationClass.getAnnotation(ServiceLevelAgreementActionConfig.class);
            Class<? extends ServiceLevelAgreementAction>[] actions = annotation.actionClasses();
            if (actions != null) {
                List<Class<? extends ServiceLevelAgreementAction>> actionClassList = Lists.newArrayList(actions);
                validation = ServiceLevelAgreementActionUtil.validateActionConfiguration(actionClassList);
            } else {
                validation.add(new ServiceLevelAgreementActionValidation(false, "No Actions are defined for :" + actionConfigurationClassName));
            }

        } catch (ClassNotFoundException e) {
            validation.add(new ServiceLevelAgreementActionValidation(false, "ImmutableAction Configuration Not Found: " + e.getMessage()));
        }

        return validation;

    }

    private synchronized void buildActionConfigurationRegistry() {
        actionConfigurationRegistry = new ArrayList<>();
        List<ServiceLevelAgreementActionUiConfigurationItem> rules = new ArrayList<>();
        Set<Class<?>>
            items = ReflectionPolicyAnnotationDiscoverer.getTypesAnnotatedWith(ServiceLevelAgreementActionConfig.class);
        for (Class c : items) {
            List<FieldRuleProperty> properties = getUiProperties(c);
            ServiceLevelAgreementActionConfig policy = (ServiceLevelAgreementActionConfig) c.getAnnotation(ServiceLevelAgreementActionConfig.class);
            ServiceLevelAgreementActionUiConfigurationItem
                configItem = buildUiModel(policy, c, properties);
            rules.add(configItem);
            actionConfigurationRegistry.add(configItem);
        }
    }

    public List<ServiceLevelAgreementActionUiConfigurationItem> discoverActionConfigurations() {
        if (actionConfigurationRegistry == null) {
            buildActionConfigurationRegistry();
        }
        return actionConfigurationRegistry;

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
}
