package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.sla.FeedServiceLevelAgreements;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 5/1/16.
 */
public interface FeedManagerFeedService {

    List<FeedMetadata> getReusableFeeds();

    FeedMetadata getFeedByName(String categoryName, String feedName);

    FeedMetadata getFeedById(String id);

    FeedMetadata getFeedById(String id, boolean refreshTargetTableSchema);

    Collection<FeedMetadata> getFeeds();

    public Collection<? extends UIFeed> getFeeds(boolean verbose);

    public List<FeedSummary> getFeedSummaryData();

    public List<FeedSummary> getFeedSummaryForCategory(String categoryId);

    List<FeedMetadata> getFeedsWithTemplate(String registeredTemplateId);

    NifiFeed createFeed(FeedMetadata feedMetadata);

    void saveFeed(FeedMetadata feed);

    FeedSummary enableFeed(String feedId);

    FeedSummary disableFeed(String feedId);

    public void updateFeedsWithTemplate(String oldTemplateId, String newTemplateId);

    public void applyFeedSelectOptions(List<FieldRuleProperty> properties);

    public List<ServiceLevelAgreement> saveFeedSla(FeedServiceLevelAgreements serviceLevelAgreements);

    public FeedServiceLevelAgreements getFeedServiceLevelAgreements(String feedId);


}
