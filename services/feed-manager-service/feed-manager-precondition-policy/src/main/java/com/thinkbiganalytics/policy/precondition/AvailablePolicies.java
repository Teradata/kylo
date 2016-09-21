package com.thinkbiganalytics.policy.precondition;

import com.thinkbiganalytics.policy.ReflectionPolicyAnnotationDiscoverer;
import com.thinkbiganalytics.policy.precondition.transform.PreconditionAnnotationTransformer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.policy.rest.model.PreconditionRuleBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 4/22/16.
 */
public class AvailablePolicies {


    public static List<PreconditionRule> discoverPreconditions() {

        List<PreconditionRule> rules = new ArrayList<>();
        Set<Class<?>>
            validators = ReflectionPolicyAnnotationDiscoverer.getTypesAnnotatedWith(PreconditionPolicy.class);
        for (Class c : validators) {
            PreconditionPolicy policy = (PreconditionPolicy) c.getAnnotation(PreconditionPolicy.class);
            String desc = policy.description();
            String shortDesc = policy.shortDescription();
            if (StringUtils.isBlank(desc) && StringUtils.isNotBlank(shortDesc)) {
                desc = shortDesc;
            }
            if (StringUtils.isBlank(shortDesc) && StringUtils.isNotBlank(desc)) {
                shortDesc = desc;
            }
            List<FieldRuleProperty> properties = PreconditionAnnotationTransformer.instance().getUiProperties(c);
            rules.add(new PreconditionRuleBuilder(policy.name()).description(desc).shortDescription(shortDesc)
                          .addProperties(properties).objectClassType(c).build());
        }
        return rules;
    }

    public static List<FieldRuleProperty> findPropertiesMatchingRenderType(List<PreconditionRule> rules, String type) {
        return PreconditionAnnotationTransformer.instance().findPropertiesForRulesetMatchingRenderType(rules, type);
    }


}
