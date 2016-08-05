package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.precondition.AvailablePolicies;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/5/16.
 */
public class FeedManagerPreconditionService {
    @Inject
    FeedManagerFeedService feedManagerFeedService;


    public List<PreconditionRule> getPossiblePreconditions() {
        List<PreconditionRule> rules = AvailablePolicies.discoverPreconditions();
        //find and attach Feed Lookup list to those that are of that type
        List<FieldRuleProperty> feedLookupLists = AvailablePolicies.findPropertiesMatchingRenderType(rules, PolicyPropertyTypes.PROPERTY_TYPE.feedChips.name());
        feedManagerFeedService.applyFeedSelectOptions(feedLookupLists);
        return rules;
    }

}
