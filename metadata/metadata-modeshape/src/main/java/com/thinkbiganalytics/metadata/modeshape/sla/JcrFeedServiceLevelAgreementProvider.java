package com.thinkbiganalytics.metadata.modeshape.sla;

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

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementRelationship;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

/**
 */
public class JcrFeedServiceLevelAgreementProvider implements FeedServiceLevelAgreementProvider {

    public static final String RELATED_FEEDS_NODE = "tba:relatedFeeds"; /// A node containing list of feed references related to an SLA

    private static final String RELATIONSHIP_QUERY = "SELECT feedsRel.* from " + "[" + JcrServiceLevelAgreement.NODE_TYPE + "] AS sla "
                    + "JOIN [" + JcrFeedServiceLevelAgreementRelationship.NODE_TYPE + "] AS feedsRel ON ISCHILDNODE(feedsRel, sla) ";

    @Inject
    FeedProvider feedProvider;

    /**
     * Finds All Service Level Agreements and also adds in an related feeds
     * Filter out the Precondition SLAs as those are not to be managed via the SLA screen.
     */
    @Override
    public List<FeedServiceLevelAgreement> findAllAgreements() {
        return findAllRelationships().stream()
            .map(rel -> rel.getAgreement())
            .collect(Collectors.toList());
    }

    public FeedServiceLevelAgreement findAgreement(ServiceLevelAgreement.ID id) {
        String query = RELATIONSHIP_QUERY + "WHERE sla.[mode:id] = $slaId ";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("slaId", id.toString());
        
        return streamQueryResult(query, bindParams).findFirst()
            .map(rel -> rel.getAgreement())
            .orElse(null);
    }

    public List<FeedServiceLevelAgreement> findFeedServiceLevelAgreements(Feed.ID feedId) {
        String query = RELATIONSHIP_QUERY + "WHERE feedsRel.[" + JcrFeedServiceLevelAgreementRelationship.FEEDS + "] IN ('" + feedId.toString() + "')";

        return streamQueryResult(query, null)
            .map(rel -> rel.getAgreement())
            .collect(Collectors.toList());
    }

    public List<FeedServiceLevelAgreementRelationship> findAllRelationships() {
        return streamQueryResult(RELATIONSHIP_QUERY, null).collect(Collectors.toList());
    }

    public FeedServiceLevelAgreementRelationship findRelationship(ServiceLevelAgreement.ID id) {
        String query = RELATIONSHIP_QUERY + "WHERE sla.[mode:id] = $slaId ";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("slaId", id.toString());
        
        return streamQueryResult(query, bindParams).findFirst().orElse(null);
    }

    /**
     * Create/Update the Relationship between a SLA and a Set of Feeds
     */
    @Override
    public FeedServiceLevelAgreementRelationship relate(ServiceLevelAgreement sla, Set<Feed.ID> feedIds) {
        Set<Feed> feeds = feedIds.stream()
            .map(id -> feedProvider.getFeed(id))
            .filter(feed -> feed != null)
            .collect(Collectors.toSet());
        
        return relateFeeds(sla, feeds);
    }

    @Override
    public FeedServiceLevelAgreementRelationship relateFeeds(ServiceLevelAgreement sla, Set<Feed> feeds) {
        Node slaNode = ((JcrServiceLevelAgreement) sla).getNode();
        JcrFeedServiceLevelAgreementRelationship relationship = JcrUtil.getOrCreateNode(slaNode, 
                                                                                        RELATED_FEEDS_NODE, 
                                                                                        JcrFeedServiceLevelAgreementRelationship.NODE_TYPE, 
                                                                                        JcrFeedServiceLevelAgreementRelationship.class);
        
        feeds.stream()
            .map(feed -> feedProvider.updateFeedServiceLevelAgreement(feed.getId(), sla))
            .map(JcrFeed.class::cast)
            .map(JcrFeed::getNode)
            .forEach(node -> JcrPropertyUtil.addToSetProperty(relationship.getNode(), 
                                                              JcrFeedServiceLevelAgreementRelationship.FEEDS, 
                                                              node, 
                                                              true));
        return relationship;
    }

    @Override
    public boolean removeFeedRelationships(ServiceLevelAgreement.ID id) {
        JcrFeedServiceLevelAgreementRelationship extensibleEntity = (JcrFeedServiceLevelAgreementRelationship) findRelationship(id);
        if (extensibleEntity != null) {
            return extensibleEntity.removeFeedRelationships(id);
        } else {
            return false;
        }
    }

    public boolean removeAllRelationships(ServiceLevelAgreement.ID id) {
        try {
            JcrFeedServiceLevelAgreementRelationship extensibleEntity = (JcrFeedServiceLevelAgreementRelationship) findRelationship(id);
            if (extensibleEntity != null) {
                extensibleEntity.removeFeedRelationships(id);
                extensibleEntity.getNode().remove();
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("unable to remove Feed SLA relationships for SLA " + id, e);
        }
        return false;
    }

    protected Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }
    
    private Stream<JcrFeedServiceLevelAgreementRelationship> streamQueryResult(String query, Map<String, String> params) {
        try {
            QueryResult result = JcrQueryUtil.query(getSession(), query, params);

            return JcrQueryUtil.queryResultStream(result, JcrFeedServiceLevelAgreementRelationship.class);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("failed to query feed SLA relationships", e);
        }
    }
}
