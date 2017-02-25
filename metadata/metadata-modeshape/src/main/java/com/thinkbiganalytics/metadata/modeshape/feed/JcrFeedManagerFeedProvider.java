package com.thinkbiganalytics.metadata.modeshape.feed;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.event.MetadataChange.ChangeType;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChange;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChangeEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;

import org.joda.time.DateTime;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

/**
 */


public class JcrFeedManagerFeedProvider extends BaseJcrProvider<FeedManagerFeed, Feed.ID> implements FeedManagerFeedProvider {

    @Inject
    private FeedProvider feedProvider;
    @Inject
    private MetadataEventService metadataEventService;
    @Inject
    private JcrFeedUtil jcrFeedUtil;

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
        return JcrFeed.NODE_TYPE;
    }

    @Override
    public Class<JcrFeedManagerFeed> getEntityClass() {
        return JcrFeedManagerFeed.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrFeedManagerFeed.class;
    }

    @Override
    public FeedManagerFeed findBySystemName(String categorySystemName, String systemName) {

        JcrFeed feed = (JcrFeed) feedProvider.findBySystemName(categorySystemName, systemName);
        if (feed != null) {
            if (feed instanceof FeedManagerFeed) {
                return (FeedManagerFeed) feed;
            } else {
                return new JcrFeedManagerFeed<>(feed.getNode());
            }
        }
        return null;

    }

    public FeedManagerFeed ensureFeed(Feed feed) {
        FeedManagerFeed fmFeed = findById(feed.getId());
        if (fmFeed == null) {
            JcrFeed jcrFeed = (JcrFeed) feed;
            fmFeed = new JcrFeedManagerFeed(jcrFeed.getNode());
        }
        return fmFeed;

    }

    public FeedManagerFeed ensureFeed(Category.ID categoryId, String feedSystemName) {
        Feed feed = feedProvider.ensureFeed(categoryId, feedSystemName);
        return ensureFeed(feed);
    }


    @Override
    public List<? extends FeedManagerFeed> findByTemplateId(FeedManagerTemplate.ID templateId) {
        String query = "SELECT * from " + EntityUtil.asQueryProperty(JcrFeed.NODE_TYPE) + " as e WHERE e." + EntityUtil.asQueryProperty(FeedDetails.TEMPLATE) + " = $id";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("id", templateId.toString());
        return JcrQueryUtil.find(getSession(), query, JcrFeedManagerFeed.class);
    }

    @Override
    public List<? extends FeedManagerFeed> findByCategoryId(FeedManagerCategory.ID categoryId) {

        String query = "SELECT * from " + EntityUtil.asQueryProperty(JcrFeed.NODE_TYPE) + " as e "
                       + "WHERE e." + EntityUtil.asQueryProperty(FeedSummary.CATEGORY) + " = $id";

        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("id", categoryId.toString());

        try {
            QueryResult result = JcrQueryUtil.query(getSession(), query, bindParams);
            return JcrQueryUtil.queryResultToList(result, JcrFeedManagerFeed.class);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to getFeeds for Category ", e);
        }

    }

    public Feed.ID resolveId(Serializable fid) {
        return new JcrFeed.FeedId(fid);
    }

    @Override
    public void delete(FeedManagerFeed feedManagerFeed) {
        jcrFeedUtil.addPostFeedChangeAction(feedManagerFeed, ChangeType.DELETE);

        // Remove dependent feeds
        final Node node = ((JcrFeedManagerFeed) feedManagerFeed).getNode();
        feedManagerFeed.getDependentFeeds().forEach(feed -> feedManagerFeed.removeDependentFeed((JcrFeed) feed));
        JcrMetadataAccess.getCheckedoutNodes().removeIf(node::equals);

        // Delete feed
        feedManagerFeed.getTemplate().removeFeed(feedManagerFeed);
        super.delete(feedManagerFeed);
    }


}
