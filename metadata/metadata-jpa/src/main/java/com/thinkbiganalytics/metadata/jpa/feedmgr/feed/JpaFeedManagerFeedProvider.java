package com.thinkbiganalytics.metadata.jpa.feedmgr.feed;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.jpa.BaseJpaProvider;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;

import javax.persistence.NoResultException;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 5/3/16.
 */
public class JpaFeedManagerFeedProvider extends BaseJpaProvider<FeedManagerFeed,Feed.ID>  implements FeedManagerFeedProvider{


    @Override
    public Class<? extends FeedManagerFeed> getEntityClass() {
        return JpaFeedManagerFeed.class;
    }

    @Override
    public Feed.ID resolveId(Serializable fid) {
        return new JpaFeed.FeedId(fid);
    }

    public FeedManagerFeed findBySystemName(String systemName) {

        FeedManagerFeed feed =  null;
        try {
            feed = (FeedManagerFeed) entityManager.createNamedQuery(FeedManagerNamedQueries.FEED_FIND_BY_SYSTEM_NAME)
                    .setParameter("systemName", systemName)
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
