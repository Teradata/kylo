package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.rest.model.*;
import com.thinkbiganalytics.feedmgr.service.feed.datasource.NifiFeedDatasourceFactory;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.template.JpaFeedManagerTemplate;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.rest.JerseyClientException;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public class JpaFeedManagerFeedService extends AbstractFeedManagerFeedService implements FeedManagerFeedService {

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    FeedManagerCategoryProvider categoryProvider;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    FeedManagerTemplateService templateRestProvider;


    @Inject
    FeedManagerPreconditionService feedPreconditionModelTransform;

    @Override
    public List<FeedMetadata> getReusableFeeds() {
        return null;
    }

    @Override
    public FeedMetadata getFeedByName(String feedName) {
        FeedMetadata feedMetadata = null;
        FeedManagerFeed domainFeed = feedManagerFeedProvider.findBySystemName(feedName);
        if(domainFeed != null){
            feedMetadata = FeedModelTransform.DOMAIN_TO_FEED.apply(domainFeed);
        }
        return feedMetadata;
    }

    @Override
    public FeedMetadata getFeedById(String id) {
        FeedMetadata feedMetadata = null;
        FeedManagerFeed.ID domainId = feedManagerFeedProvider.resolveId(id);
        FeedManagerFeed domainFeed = feedManagerFeedProvider.findById(domainId);
        if(domainFeed != null){
            feedMetadata = FeedModelTransform.DOMAIN_TO_FEED.apply(domainFeed);
        }
        return feedMetadata;
    }

    @Override
    public Collection<FeedMetadata> getFeeds() {
        Collection<FeedMetadata> feeds = null;
        List<FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findAll();
        if(domainFeeds != null){
         feeds =   FeedModelTransform.domainToFeedMetadata(domainFeeds);
        }
        return feeds;
    }

    @Override
    public Collection<? extends UIFeed> getFeeds(boolean verbose) {
        if(verbose){
            return getFeeds();
        }
        else {
            return getFeedSummaryData();
        }

    }

    @Override
    public List<FeedSummary> getFeedSummaryData() {
        Collection<FeedMetadata> feeds = getFeeds();
        List<FeedSummary> summaryList = new ArrayList<>();
        if(feeds != null && !feeds.isEmpty()) {
            for(FeedMetadata feed: feeds){
                summaryList.add(new FeedSummary(feed));
            }
        }
        return summaryList;


    }

    @Override
    public List<FeedSummary> getFeedSummaryForCategory(String categoryId) {
        List<FeedSummary> summaryList = new ArrayList<>();
        FeedManagerCategory.ID categoryDomainId = categoryProvider.resolveId(categoryId);
        List<FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findByCategoryId(categoryDomainId);
        if(domainFeeds != null && !domainFeeds.isEmpty()) {
            List<FeedMetadata> feeds =    FeedModelTransform.domainToFeedMetadata(domainFeeds);
            for(FeedMetadata feed: feeds){
                summaryList.add(new FeedSummary(feed));
            }
        }
        return summaryList;
    }

    @Override
    public List<FeedMetadata> getFeedsWithTemplate(String registeredTemplateId) {
        List<FeedMetadata> feedMetadatas = null;
        FeedManagerTemplate.ID templateDomainId = templateProvider.resolveId(registeredTemplateId);
        List<FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findByTemplateId(templateDomainId);
        if(domainFeeds != null){
            feedMetadatas = FeedModelTransform.domainToFeedMetadata(domainFeeds);
        }
        return feedMetadatas;
    }

    @Override
    protected RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) throws JerseyClientException {
       return templateRestProvider.getRegisteredTemplate(templateId);
    }

    @Transactional(transactionManager = "metadataTransactionManager")
    public NifiFeed createFeed(FeedMetadata feedMetadata) throws JerseyClientException {
        return super.createFeed(feedMetadata);
    }

    @Override
    @Transactional(transactionManager = "metadataTransactionManager")
    public void saveFeed(FeedMetadata feed) {
        //if this is the first time saving this feed create a new one
        FeedManagerFeed domainFeed = FeedModelTransform.FEED_TO_DOMAIN.apply(feed);
        if(domainFeed.getTemplate() == null){
            FeedManagerTemplate.ID templateId = new JpaFeedManagerTemplate.FeedManagerTemplateId(feed.getTemplateId());
           FeedManagerTemplate template = templateProvider.findById(templateId);
            domainFeed.setTemplate(template);
        }
        Feed baseFeed = null;
        if(domainFeed.isNew()) {

            baseFeed = new JpaFeed(feed.getCategoryAndFeedName(),feed.getDescription());
            domainFeed.setFeed(baseFeed);
            feed.setFeedId(baseFeed.getId().toString());
            //change the state to ENABLED
            domainFeed.setState(FeedMetadata.STATE.ENABLED.name());
            feed.setState(FeedMetadata.STATE.ENABLED.name());
            //TODO Write the Feed Sources and Destinations
           // Datasource datasource = NifiFeedDatasourceFactory.transform(feed);
        }
        else {
            //attach the latest Feed data to this object
           baseFeed =  (JpaFeed) feedProvider.getFeed(new JpaFeed.FeedId(feed.getFeedId()));
            domainFeed.setFeed(baseFeed);
        }
        domainFeed = feedManagerFeedProvider.update(domainFeed);

        //merge in preconditions if they exist
        List<GenericUIPrecondition> preconditions = feed.getSchedule().getPreconditions();
        if (preconditions != null) {
            List<List<com.thinkbiganalytics.metadata.sla.api.Metric>> domainMetrics = new ArrayList<>();
            domainMetrics.add(new ArrayList<Metric>(FeedManagerPreconditionService.uiPreconditionToFeedPrecondition(feed, preconditions)));
            baseFeed = feedProvider.updatePrecondition(baseFeed.getId(), domainMetrics);

        }

    }


    @Override
    public void updateFeedsWithTemplate(String oldTemplateId, String newTemplateId) {
            //not needed
    }
}
