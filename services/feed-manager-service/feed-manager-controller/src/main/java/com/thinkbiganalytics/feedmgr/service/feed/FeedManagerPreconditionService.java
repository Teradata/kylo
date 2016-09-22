package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.precondition.PreconditionPolicyRuleCache;
import com.thinkbiganalytics.policy.precondition.transform.PreconditionAnnotationTransformer;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/5/16.
 */
public class FeedManagerPreconditionService {
    @Inject
    FeedManagerFeedService feedManagerFeedService;

    @Inject
    PreconditionPolicyRuleCache preconditionPolicyRuleCache;

    public List<PreconditionRule> getPossiblePreconditions() {
        List<PreconditionRule> rules = preconditionPolicyRuleCache.getPreconditionRules();
        //find and attach Feed Lookup list to those that are of that type

        feedManagerFeedService
            .applyFeedSelectOptions(
                PreconditionAnnotationTransformer.instance()
                    .findPropertiesForRulesetMatchingRenderTypes(rules, new String[]{PolicyPropertyTypes.PROPERTY_TYPE.feedChips.name(),
                                                                                     PolicyPropertyTypes.PROPERTY_TYPE.feedSelect.name(),
                                                                                     PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name()}));
        return rules;
    }

}
