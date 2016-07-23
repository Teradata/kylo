package com.thinkbiganalytics.policy.precondition.transform;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup;
import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.precondition.Precondition;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.policy.validation.PolicyPropertyTypes;

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

    public List<ObligationGroup> getPreconditions() {
        List<ObligationGroup> policies = new ArrayList<>();
        if (preconditionRules != null) {
            for (PreconditionRule rule : preconditionRules) {
                try {
                    Precondition policy = PreconditionAnnotationTransformer.instance().fromUiModel(rule);
                    policies.addAll(Lists.newArrayList(policy.getPreconditionObligations()));
                } catch (PolicyTransformException e) {
                    e.printStackTrace();
                }
            }
        }
        return policies;
    }

}
