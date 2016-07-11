package com.thinkbiganalytics.feedmgr.service.feed;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.GenericUIPrecondition;
import com.thinkbiganalytics.feedmgr.rest.model.UIPrecondition;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.FieldRulePropertyBuilder;
import com.thinkbiganalytics.rest.model.LabelValue;

import org.apache.commons.lang3.StringUtils;

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


    private static final Function<GenericUIPrecondition, List<Metric>> UI_PRECONDITION_TO_FEED_PRECONDITION = new Function<GenericUIPrecondition, List<Metric>>() {
        @Nullable
        @Override
        public List<Metric> apply(GenericUIPrecondition genericUIPrecondition) {
            List<Metric> preconditions = new ArrayList<>();
            if(genericUIPrecondition.getType().equalsIgnoreCase(FeedExecutedSinceFeed.class.getName())) {
               FieldRuleProperty property =  genericUIPrecondition.getProperty(GenericUIPrecondition.DEPENDENT_FEED_NAME_PROPERTY);
                if(property.getValues() != null && !property.getValues().isEmpty()) {
                    List<String> dependentFeedNames = property.getValues();
                    for (String name : dependentFeedNames) {
                        FeedExecutedSinceFeed metric = new FeedExecutedSinceFeed(name, genericUIPrecondition.
                            getFeedName());
                        preconditions.add(metric);

                    }
                }
                else if(StringUtils.isNotBlank(property.getValue())) {
                    FeedExecutedSinceFeed metric = new FeedExecutedSinceFeed(property.getValue(), genericUIPrecondition.
                    getFeedName());
                    preconditions.add(metric);
                }
                return preconditions;
            }
            return null;
        }
    };

    private static List<Metric> flatten(Collection<List<Metric>> metrics){
        List<Metric> finalList = new ArrayList<>();
        if(metrics != null)   {
for(List<Metric> m: metrics){
    if(m != null && !m.isEmpty()) {
        finalList.addAll(m);
    }
}
        }
        return finalList;
    }

    public static List<Metric> uiPreconditionToFeedPrecondition(FeedMetadata feed,Collection<GenericUIPrecondition> uiPreconditions) {
        if (uiPreconditions != null) {
            ///explode the ui preconditoins into new objects if more than 1

            for (GenericUIPrecondition condition : uiPreconditions) {
                condition.setFeedName(feed.getSystemFeedName());
            }

           Collection<List<Metric>> metrics = FluentIterable.from(uiPreconditions).transform(UI_PRECONDITION_TO_FEED_PRECONDITION).toList();
            return flatten(metrics);
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
                property = new FieldRulePropertyBuilder("feedName").displayName("Feed Name").type(FieldRulePropertyBuilder.PROPERTY_TYPE.chips).placeholder("Start typing a feed name").addSelectableValues(feedSelection).hint("Select the feed(s) that this feed depends on").build();
        property.setValues(new ArrayList<>());
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
