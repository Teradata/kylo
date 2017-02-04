package com.thinkbiganalytics.policy.precondition;

/*-
 * #%L
 * thinkbig-feed-manager-precondition-policy
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
 * Service that returns all classes annotated with {@link PreconditionPolicy} transforming the Java classes to user interface friendly {@link PreconditionRule} objects
 */
public class AvailablePolicies {


    /**
     * Find all classes annotated with {@link PreconditionPolicy} and transfrom them into the user interface object
     *
     * @return user interface objects of the {@link PreconditionPolicy} on the classpath
     */
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
