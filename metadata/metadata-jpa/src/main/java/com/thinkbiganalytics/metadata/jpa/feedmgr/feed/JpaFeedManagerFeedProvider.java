package com.thinkbiganalytics.metadata.jpa.feedmgr.feed;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.jpa.BaseJpaProvider;
import com.thinkbiganalytics.metadata.jpa.category.JpaCategory;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;
import com.thinkbiganalytics.metadata.jpa.feedmgr.category.JpaFeedManagerCategory;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 5/3/16.
 */
public class JpaFeedManagerFeedProvider extends BaseJpaProvider<FeedManagerFeed, Feed.ID> implements FeedManagerFeedProvider {

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private CategoryProvider<FeedManagerCategory> categoryProvider;

    public FeedManagerFeed ensureFeed(Feed feed) {
        FeedManagerFeed fmFeed = findById(feed.getId());
        if (fmFeed == null) {
            fmFeed = new JpaFeedManagerFeed(new JpaFeed.FeedId(feed.getId()));
        }
        return fmFeed;
    }

    @Override
    public FeedManagerFeed ensureFeed(Category.ID categoryId, String feedSystemName) {
        Category c = categoryProvider.findById(categoryId);
        JpaFeedManagerFeed feedManagerFeed = new JpaFeedManagerFeed(feedSystemName, null);
        feedManagerFeed.setCategory((JpaFeedManagerCategory) c);
        return feedManagerFeed;


    }

    @Override
    public Class<? extends FeedManagerFeed> getEntityClass() {
        return JpaFeedManagerFeed.class;
    }

    @Override
    public Feed.ID resolveId(Serializable fid) {
        return new JpaFeed.FeedId(fid);
    }

    public FeedManagerFeed findBySystemName(String categorySystemName, String systemName) {

        FeedManagerFeed feed =  null;
        try {
            feed = (FeedManagerFeed) entityManager.createNamedQuery(FeedManagerNamedQueries.FEED_FIND_BY_SYSTEM_NAME)
                    .setParameter("systemName", systemName)
                .setParameter("categorySystemName", categorySystemName)
                .getSingleResult();
        }catch(NoResultException e){
            e.printStackTrace();
        }
        return feed;
    }

    public List<FeedManagerFeed> findByTemplateId(FeedManagerTemplate.ID templateId) {

        List<FeedManagerFeed> feeds =  null;
        try {
            feeds = ( List<FeedManagerFeed>) entityManager.createNamedQuery(FeedManagerNamedQueries.FEED_FIND_BY_TEMPLATE_ID)
                    .setParameter("templateId", templateId).getResultList();
        }catch(NoResultException e){
            e.printStackTrace();
        }
        return feeds;
    }

    public List<FeedManagerFeed> findByCategoryId(FeedManagerCategory.ID categoryId) {

        List<FeedManagerFeed> feeds =  null;
        try {
            feeds = ( List<FeedManagerFeed>) entityManager.createNamedQuery(FeedManagerNamedQueries.FEED_FIND_BY_CATEGORY_ID)
                    .setParameter("categoryId", categoryId).getResultList();
        }catch(NoResultException e){
            e.printStackTrace();
        }
        return feeds;
    }


}
