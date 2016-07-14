package com.thinkbiganalytics.policy.precondition.transform;

import com.thinkbiganalytics.policy.PolicyProperty;
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
                PreconditionAnnotationTransformer.instance().findPropertiesForRulesetMatchingRenderType(preconditionRules, PolicyProperty.PROPERTY_TYPE.currentFeed.name());
            if (properties != null && !properties.isEmpty()) {
                for (FieldRuleProperty property : properties) {
                    property.setValue(category + "." + feed);
                }
            }
        }
    }

    public List<Precondition> getPreconditions() {
        List<Precondition> policies = new ArrayList<>();

        if (preconditionRules != null) {
            for (PreconditionRule rule : preconditionRules) {
                try {
                    Precondition policy = PreconditionAnnotationTransformer.instance().fromUiModel(rule);
                    policies.add(policy);
                } catch (PolicyTransformException e) {
                    e.printStackTrace();
                }
            }
        }
        return policies;
    }

}
