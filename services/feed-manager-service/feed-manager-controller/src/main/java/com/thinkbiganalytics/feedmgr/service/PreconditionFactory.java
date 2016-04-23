package com.thinkbiganalytics.feedmgr.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.GenericUIPrecondition;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceFeedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.policy.rest.model.FieldRulePropertyBuilder;
import com.thinkbiganalytics.rest.model.LabelValue;

/**
 * Created by sr186054 on 3/15/16.
 */
@Component
public class PreconditionFactory {

    @Autowired
    MetadataService metadataService;

    private MetadataService getMetadataService(){
        return metadataService;
    }

    public Metric getPrecondition(GenericUIPrecondition condition )  {

        try {
            if(condition.getType().equalsIgnoreCase(FeedExecutedSinceFeedMetric.class.getName())) {

                return  FeedExecutedSinceFeedMetric.named(condition.getProperty(GenericUIPrecondition.DEPENDENT_FEED_NAME_PROPERTY).getValue(), condition.getFeedName());
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getDependentFeedName(GenericUIPrecondition condition){
        if(condition != null ){
            return condition.getDependentFeedName();
        }
        return null;
    }

    public List<Metric> getPreconditions(List<GenericUIPrecondition> uiPreconditions){
        List<Metric> conditions = new ArrayList<>();
        for(GenericUIPrecondition uiPrecondition: uiPreconditions){
         Metric m = getPrecondition(uiPrecondition);
            if(m != null){
                conditions.add(m);
            }
        }
        return conditions;
    }

    public List<GenericUIPrecondition> getPossiblePreconditions(){

       List<GenericUIPrecondition> conditions = new ArrayList<>();
        conditions.add(feedDependsOnAnotherFeedPrecondition());
        return conditions;
    }

    public void applyFeedName(List<GenericUIPrecondition> condtions, String feedName){
        for(GenericUIPrecondition condition: condtions){
            condition.setFeedName(feedName);
        }
    }


    public GenericUIPrecondition feedDependsOnAnotherFeedPrecondition(){
        GenericUIPrecondition condition = new GenericUIPrecondition();
        condition.setName("Depends on another Feed(s)");
        condition.setDescription("Depends on another Feed completing before running.");
        condition.setType(FeedExecutedSinceFeedMetric.class.getName());
        List<FeedSummary> feedSummaries = metadataService.getFeedSummaryData();
        List<LabelValue> feedSelection = new ArrayList<>();
        for(FeedSummary feedSummary: feedSummaries){
            feedSelection.add(new LabelValue(feedSummary.getCategoryAndFeedDisplayName(),feedSummary.getCategoryAndFeedDSystemName()));
        }
        FieldRuleProperty
            property = new FieldRulePropertyBuilder("feedName").displayName("Feed Name").type(FieldRulePropertyBuilder.PROPERTY_TYPE.select).addSelectableValues(feedSelection).hint("Select the feed that this feed depends on").build();
        condition.addProperty(property);
        return condition;
    }

}
