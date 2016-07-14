package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.precondition.AvailablePolicies;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.rest.model.LabelValue;

import java.util.ArrayList;
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
        List<FieldRuleProperty> feedLookupLists = AvailablePolicies.findPropertiesMatchingRenderType(rules, PolicyProperty.PROPERTY_TYPE.feedChips.name());
        if (feedLookupLists != null && !feedLookupLists.isEmpty()) {
            List<FeedSummary> feedSummaries = feedManagerFeedService.getFeedSummaryData();
            List<LabelValue> feedSelection = new ArrayList<>();
            for (FeedSummary feedSummary : feedSummaries) {
                feedSelection.add(new LabelValue(feedSummary.getCategoryAndFeedDisplayName(), feedSummary.getCategoryAndFeedSystemName()));
            }
            for (FieldRuleProperty property : feedLookupLists) {
                property.setSelectableValues(feedSelection);
                property.setValues(new ArrayList<>());
            }
        }
        return rules;
    }

}
