package com.thinkbiganalytics.feedmgr.service.feed;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.GenericUIPrecondition;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.FieldRulePropertyBuilder;
import com.thinkbiganalytics.rest.model.LabelValue;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 5/5/16.
 */
public class FeedManagerPreconditionService {
    @Inject
    FeedManagerFeedService feedManagerFeedService;


    private static final Function<GenericUIPrecondition, Metric> UI_PRECONDITION_TO_FEED_PRECONDITION = new Function<GenericUIPrecondition, Metric>() {
        @Nullable
        @Override
        public Metric apply(GenericUIPrecondition genericUIPrecondition) {
            if(genericUIPrecondition.getType().equalsIgnoreCase(FeedExecutedSinceFeed.class.getName())) {

                FeedExecutedSinceFeed metric = new FeedExecutedSinceFeed(genericUIPrecondition.getProperty(GenericUIPrecondition.DEPENDENT_FEED_NAME_PROPERTY).getValue(), genericUIPrecondition.
                        getFeedName());
                return metric;
            }
            return null;
        }
    };

    public static List<Metric> uiPreconditionToFeedPrecondition(FeedMetadata feed,Collection<GenericUIPrecondition> uiPreconditions) {
        if (uiPreconditions != null) {
            for (GenericUIPrecondition condition : uiPreconditions) {
                condition.setFeedName(feed.getSystemFeedName());
            }

            return new ArrayList<>(Collections2.transform(uiPreconditions, UI_PRECONDITION_TO_FEED_PRECONDITION));
        }
        return null;
    }

    public Metric uiToFeedPrecondition(FeedMetadata feed, GenericUIPrecondition uiPrecondition) {
        if(uiPrecondition != null) {
            uiPrecondition.setFeedName(feed.getSystemFeedName());
            return UI_PRECONDITION_TO_FEED_PRECONDITION.apply(uiPrecondition);
        }
        return null;
    }


    private GenericUIPrecondition feedDependsOnAnotherFeedPrecondition(List<FeedSummary> feedSummaries){
        GenericUIPrecondition condition = new GenericUIPrecondition();
        condition.setName("Depends on another Feed(s)");
        condition.setDescription("Depends on another Feed completing before running.");
        condition.setType(FeedExecutedSinceFeed.class.getName());
        List<LabelValue> feedSelection = new ArrayList<>();
        for(FeedSummary feedSummary: feedSummaries){
            feedSelection.add(new LabelValue(feedSummary.getCategoryAndFeedDisplayName(),feedSummary.getCategoryAndFeedSystemName()));
        }
        FieldRuleProperty
                property = new FieldRulePropertyBuilder("feedName").displayName("Feed Name").type(FieldRulePropertyBuilder.PROPERTY_TYPE.select).addSelectableValues(feedSelection).hint("Select the feed that this feed depends on").build();
        condition.addProperty(property);
        return condition;
    }

    public List<GenericUIPrecondition> getPossiblePreconditions(){
        List<FeedSummary> feedSummaries = feedManagerFeedService.getFeedSummaryData();
        List<GenericUIPrecondition> conditions = new ArrayList<>();
        conditions.add(feedDependsOnAnotherFeedPrecondition(feedSummaries));
        return conditions;
    }

}
