package com.thinkbiganalytics.policy.precondition;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 7/12/16.
 */
@PreconditionPolicy(name = PreconditionPolicyConstants.FEED_EXECUTED_SINCE_FEEDS_NAME, description = "Policy will trigger the feed when all of the supplied feeds have successfully finished")
public class FeedExecutedSinceFeeds implements Precondition,DependentFeedPrecondition {

    @PolicyProperty(name = "Since Feed", type = PolicyPropertyTypes.PROPERTY_TYPE.currentFeed, hidden = true)
    private String sinceCategoryAndFeedName;

    @PolicyProperty(name = "Dependent Feeds", required = true, type = PolicyPropertyTypes.PROPERTY_TYPE.feedChips, placeholder = "Start typing a feed",
                    hint = "Select feed(s) that this feed is dependent upon")
    private String categoryAndFeeds;

    private List<String> categoryAndFeedList;

    public FeedExecutedSinceFeeds(@PolicyPropertyRef(name = "Since Feed") String sinceCategoryAndFeedName, @PolicyPropertyRef(name = "Dependent Feeds") String categoryAndFeeds) {
        this.sinceCategoryAndFeedName = sinceCategoryAndFeedName;
        this.categoryAndFeeds = categoryAndFeeds;
        categoryAndFeedList = Arrays.asList(StringUtils.split(categoryAndFeeds, ","));
    }

    public String getSinceCategoryAndFeedName() {
        return sinceCategoryAndFeedName;
    }

    public void setSinceCategoryAndFeedName(String sinceCategoryAndFeedName) {
        this.sinceCategoryAndFeedName = sinceCategoryAndFeedName;
    }

    public String getCategoryAndFeeds() {
        return categoryAndFeeds;
    }

    public void setCategoryAndFeeds(String categoryAndFeeds) {
        this.categoryAndFeeds = categoryAndFeeds;
    }

    public List<String> getCategoryAndFeedList() {
        return categoryAndFeedList;
    }

    public void setCategoryAndFeedList(List<String> categoryAndFeedList) {
        this.categoryAndFeedList = categoryAndFeedList;
    }

    @Override
    public List<String> getDependentFeedNames() {
        return categoryAndFeedList;
    }

    @Override
    public Set<com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup> buildPreconditionObligations() {
        return Sets.newHashSet(buildPreconditionObligation());
    }

    public com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup buildPreconditionObligation(){
        Set<Metric> metrics = new HashSet<>();
        for (String categoryAndFeed : categoryAndFeedList) {
            FeedExecutedSinceFeed metric = new FeedExecutedSinceFeed(sinceCategoryAndFeedName, categoryAndFeed);
            metrics.add(metric);
        }
        Obligation obligation = new Obligation();
        obligation.setMetrics(Lists.newArrayList(metrics));
        com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup group = new com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup();
        group.addObligation(obligation);
        group.setCondition(ObligationGroup.Condition.REQUIRED.name());
        return group;
    }

}
