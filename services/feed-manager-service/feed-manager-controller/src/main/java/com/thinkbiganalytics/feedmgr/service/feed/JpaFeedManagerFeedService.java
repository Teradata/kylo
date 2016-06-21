package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.GenericUIPrecondition;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.sla.api.Metric;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

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

    @Inject
    FeedModelTransformer feedModelTransformer;

    @Override
    public List<FeedMetadata> getReusableFeeds() {
        return null;
    }

    @Override
    public FeedMetadata getFeedByName(String feedName) {
        FeedMetadata feedMetadata = null;
        FeedManagerFeed domainFeed = feedManagerFeedProvider.findBySystemName(feedName);
        if (domainFeed != null) {
            feedMetadata = FeedModelTransform.DOMAIN_TO_FEED.apply(domainFeed);
        }
        return feedMetadata;
    }

    @Override
    public FeedMetadata getFeedById(String id) {
        return getFeedById(id, false);
    }

    @Override
    public FeedMetadata getFeedById(String id, boolean refreshTargetTableSchema) {
        FeedMetadata feedMetadata = null;
        FeedManagerFeed.ID domainId = feedManagerFeedProvider.resolveId(id);
        FeedManagerFeed domainFeed = feedManagerFeedProvider.findById(domainId);
        if (domainFeed != null) {
            feedMetadata = FeedModelTransform.DOMAIN_TO_FEED.apply(domainFeed);
        }
        if (refreshTargetTableSchema && feedMetadata != null) {
            feedModelTransformer.refreshTableSchemaFromHive(feedMetadata);
        }
        return feedMetadata;
    }

    @Override
    public Collection<FeedMetadata> getFeeds() {
        Collection<FeedMetadata> feeds = null;
        List<FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findAll();
        if (domainFeeds != null) {
            feeds = FeedModelTransform.domainToFeedMetadata(domainFeeds);
        }
        return feeds;
    }

    @Override
    public Collection<? extends UIFeed> getFeeds(boolean verbose) {
        if (verbose) {
            return getFeeds();
        } else {
            return getFeedSummaryData();
        }

    }

    @Override
    public List<FeedSummary> getFeedSummaryData() {
        Collection<FeedMetadata> feeds = getFeeds();
        List<FeedSummary> summaryList = new ArrayList<>();
        if (feeds != null && !feeds.isEmpty()) {
            for (FeedMetadata feed : feeds) {
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
        if (domainFeeds != null && !domainFeeds.isEmpty()) {
            List<FeedMetadata> feeds = FeedModelTransform.domainToFeedMetadata(domainFeeds);
            for (FeedMetadata feed : feeds) {
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
        if (domainFeeds != null) {
            feedMetadatas = FeedModelTransform.domainToFeedMetadata(domainFeeds);
        }
        return feedMetadatas;
    }

    @Override
    protected RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) {
        return templateRestProvider.getRegisteredTemplate(templateId);
    }

    @Transactional(transactionManager = "metadataTransactionManager")
    public NifiFeed createFeed(FeedMetadata feedMetadata) {
        if (feedMetadata.getState() == null) {
            feedMetadata.setState(Feed.State.ENABLED.name());
        }
        return super.createFeed(feedMetadata);
    }

    @Override
    @Transactional(transactionManager = "metadataTransactionManager")
    public void saveFeed(FeedMetadata feed) {
        //if this is the first time saving this feed create a new one
        FeedManagerFeed domainFeed = feedModelTransformer.feedToDomain(feed);
        if (domainFeed.getState() == null) {
            domainFeed.setState(Feed.State.ENABLED);
        }
        domainFeed = feedManagerFeedProvider.update(domainFeed);

        //merge in preconditions if they exist
        List<GenericUIPrecondition> preconditions = feed.getSchedule().getPreconditions();
        if (preconditions != null) {
            List<List<com.thinkbiganalytics.metadata.sla.api.Metric>> domainMetrics = new ArrayList<>();
            domainMetrics.add(new ArrayList<Metric>(FeedManagerPreconditionService.uiPreconditionToFeedPrecondition(feed, preconditions)));
            feedProvider.updatePrecondition(domainFeed.getId(), domainMetrics);
        }

    }

    @Transactional(transactionManager = "metadataTransactionManager")
    private boolean enableFeed(Feed.ID feedId) {
        return feedProvider.enableFeed(feedId);
    }

    @Transactional(transactionManager = "metadataTransactionManager")
    private boolean disableFeed(Feed.ID feedId) {
        return feedProvider.disableFeed(feedId);
    }

    public FeedSummary enableFeed(String feedId) {
        if (StringUtils.isNotBlank(feedId)) {
            FeedMetadata feedMetadata = getFeedById(feedId);
            Feed.ID domainId = feedProvider.resolveFeed(feedId);
            boolean enabled = enableFeed(domainId);
            //re fetch it
            if (enabled) {
                feedMetadata.setState(Feed.State.ENABLED.name());
            }
            FeedSummary feedSummary = new FeedSummary(feedMetadata);
            return feedSummary;
        }
        return null;

    }

    public FeedSummary disableFeed(String feedId) {
        if (StringUtils.isNotBlank(feedId)) {
            FeedMetadata feedMetadata = getFeedById(feedId);
            Feed.ID domainId = feedProvider.resolveFeed(feedId);
            boolean enabled = disableFeed(domainId);
            //re fetch it
            if (enabled) {
                feedMetadata.setState(Feed.State.DISABLED.name());
            }
            FeedSummary feedSummary = new FeedSummary(feedMetadata);
            return feedSummary;
        }
        return null;
    }


    @Override
    public void updateFeedsWithTemplate(String oldTemplateId, String newTemplateId) {
        //not needed
    }
}
