package com.thinkbiganalytics.policy.precondition.transform;

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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.precondition.Precondition;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 7/12/16.
 */
public class PreconditionPolicyTransformer {

    private List<PreconditionRule> preconditionRules;

    public PreconditionPolicyTransformer(List<PreconditionRule> preconditionRules) {
        this.preconditionRules = preconditionRules;
    }

    public void applyFeedNameToCurrentFeedProperties(String category, String feed) {
        if (this.preconditionRules != null) {
            List<FieldRuleProperty>
                properties =
                PreconditionAnnotationTransformer.instance().findPropertiesForRulesetMatchingRenderType(preconditionRules, PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name());
            if (properties != null && !properties.isEmpty()) {
                for (FieldRuleProperty property : properties) {
                    property.setValue(category + "." + feed);
                }
            }
        }
    }

    public List<Precondition> getPreconditionPolicies(){
        List<Precondition> policies = new ArrayList<>();
        if (preconditionRules != null) {
            for (PreconditionRule rule : preconditionRules) {
                try {
                    Precondition policy = PreconditionAnnotationTransformer.instance().fromUiModel(rule);
                    policies.add(policy);
                } catch (PolicyTransformException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return policies;

    }

    public List<ObligationGroup> getPreconditionObligationGroups() {
        List<ObligationGroup> policies = new ArrayList<>();
        if (preconditionRules != null) {
            for (PreconditionRule rule : preconditionRules) {
                try {
                    Precondition policy = PreconditionAnnotationTransformer.instance().fromUiModel(rule);
                    policies.addAll(Lists.newArrayList(policy.buildPreconditionObligations()));
                } catch (PolicyTransformException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return policies;
    }

}
