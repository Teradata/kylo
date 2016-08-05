package com.thinkbiganalytics.metadata.modeshape.sla;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementRelationship;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.auth.AdminCredentials;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailability;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailabilityListener;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

/**
 * Created by sr186054 on 8/4/16.
 */
public class JcrFeedServiceLevelAgreementProvider implements FeedServiceLevelAgreementProvider, ModeShapeAvailabilityListener {

    @Inject
    private ExtensibleTypeProvider typeProvider;

    @Inject
    private ExtensibleEntityProvider entityProvider;

    @Inject
    FeedProvider feedProvider;

    @Inject
    private JcrMetadataAccess metadata;

    @Inject
    private ModeShapeAvailability modeShapeAvailability;

    /**
     * Finds All Service Level Agreements and also adds in an related feeds
     */
    @Override
    public List<FeedServiceLevelAgreement> findAllAgreements() {
        String query = "SELECT * from [" + JcrServiceLevelAgreement.NODE_TYPE + "] AS sla "
                       + "LEFT JOIN [" + JcrFeedServiceLevelAgreementRelationship.NODE_TYPE + "] AS feedSla ON feedSla.[" + JcrFeedServiceLevelAgreementRelationship.SLA + "] = sla.[jcr:uuid] ";
        return queryToList(query, null);

    }

    @PostConstruct
    public void scheduleServiceLevelAgreements() {
        modeShapeAvailability.subscribe(this);
    }

    @Override
    public void modeShapeAvailable() {
        createType();
    }

    private List<FeedServiceLevelAgreement> queryToList(String query, Map<String, String> params) {
        QueryResult result = null;
        try {
            result = JcrQueryUtil.query(getSession(), query, params);

            List<FeedServiceLevelAgreement> entities = new ArrayList<>();

            if (result != null) {
                try {
                    RowIterator rowIterator = result.getRows();
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.nextRow();
                        Node slaNode = row.getNode("sla");
                        Set<JcrFeed> feeds = null;

                        Node feedSlaNode = row.getNode("feedSla");
                        //Left Join will result in the node being NULL if its not there
                        if (feedSlaNode != null) {
                            JcrFeedServiceLevelAgreementRelationship feedServiceLevelAgreementRelationship = new JcrFeedServiceLevelAgreementRelationship(feedSlaNode);
                            feeds = (Set<JcrFeed>) feedServiceLevelAgreementRelationship.getFeeds();
                        }
                        JcrFeedServiceLevelAgreement entity = new JcrFeedServiceLevelAgreement(slaNode, feeds);
                        entities.add(entity);
                    }
                } catch (RepositoryException e) {
                    throw new MetadataRepositoryException("Unable to parse QueryResult to List for feedSla", e);

                }
            }
            return entities;


        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to execute Feed SLA Query.  Query is: " + query, e);
        }
    }

    public FeedServiceLevelAgreement findAgreement(ServiceLevelAgreement.ID id) {
        String query = "SELECT * from [" + JcrServiceLevelAgreement.NODE_TYPE + "] AS sla "
                       + "LEFT JOIN [" + JcrFeedServiceLevelAgreementRelationship.NODE_TYPE + "] AS feedSla ON feedSla.[" + JcrFeedServiceLevelAgreementRelationship.SLA + "] = sla.[jcr:uuid] "
                       + "WHERE sla.[jcr:uuid] = $slaId ";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("slaId", id.toString());
        List<FeedServiceLevelAgreement> list = queryToList(query, bindParams);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;

    }


    public List<FeedServiceLevelAgreement> findFeedServiceLevelAgreements(Feed.ID feedId) {
        String query = "SELECT * from [" + JcrServiceLevelAgreement.NODE_TYPE + "] AS sla "
                       + "JOIN [" + JcrFeedServiceLevelAgreementRelationship.NODE_TYPE + "] AS feedSla ON feedSla.[" + JcrFeedServiceLevelAgreementRelationship.SLA + "] = sla.[jcr:uuid] "
                       + "WHERE feedSla.[" + JcrFeedServiceLevelAgreementRelationship.FEEDS + "] IN ('" + feedId.toString() + "')";
        List<FeedServiceLevelAgreement> list = queryToList(query, null);
        return list;
    }


    public List<ExtensibleEntity> findAllRelationships() {
        return entityProvider.getEntities(JcrFeedServiceLevelAgreementRelationship.NODE_TYPE);
    }

    public FeedServiceLevelAgreementRelationship findRelationship(ServiceLevelAgreement.ID id) {
        List<? extends ExtensibleEntity> entities = entityProvider.findEntitiesMatchingProperty(JcrFeedServiceLevelAgreementRelationship.NODE_TYPE, JcrFeedServiceLevelAgreementRelationship.SLA, id);
        if (entities != null && !entities.isEmpty()) {
            return new JcrFeedServiceLevelAgreementRelationship((JcrExtensibleEntity) entities.get(0));
        }
        return null;
    }

    /**
     * Returns the relationship object for SLA to a Set of Feeds
     */
    public FeedServiceLevelAgreementRelationship getRelationship(ExtensibleEntity.ID id) {
        ExtensibleEntity entity = entityProvider.getEntity(id);
        return new JcrFeedServiceLevelAgreementRelationship((JcrExtensibleEntity) entity);
    }


    /**
     * Creates the Extensible Entity Type
     */
    public String createType() {
        return metadata.commit(new AdminCredentials(), () -> {
            ExtensibleType feedSla = typeProvider.getType(JcrFeedServiceLevelAgreementRelationship.TYPE_NAME);
            if (feedSla == null) {
                feedSla = typeProvider.buildType(JcrFeedServiceLevelAgreementRelationship.TYPE_NAME)
                    .field(JcrFeedServiceLevelAgreementRelationship.FEEDS)
                    .type(FieldDescriptor.Type.WEAK_REFERENCE)
                    .displayName("Feeds")
                    .description("The Feeds referenced on this SLA")
                    .required(false)
                    .collection(true)
                    .add()
                    .field(JcrFeedServiceLevelAgreementRelationship.SLA)
                    .type(FieldDescriptor.Type.WEAK_REFERENCE)
                    .displayName("SLA")
                    .description("The SLA")
                    .required(true)
                    .add()
                    .build();
            }

            return feedSla.getName();
        });
    }


    /**
     * Create/Update the Relationship between a SLA and a Set of Feeds
     */
    @Override
    public FeedServiceLevelAgreementRelationship relate(ServiceLevelAgreement sla, Set<Feed.ID> feedIds) {
        JcrFeedServiceLevelAgreementRelationship relationship = null;
        //find if this relationship already exists
        Set<Node> feedNodes = new HashSet<>();
        Map<String, Object> props = new HashMap<>();
        Set<Feed> feeds = new HashSet<>();
        for (Feed.ID feedId : feedIds) {
            Feed feed = feedProvider.getFeed(feedId);
            if (feed != null) {
                feeds.add(feed);
            }
        }
        return relateFeeds(sla, feeds);
    }

    @Override
    public FeedServiceLevelAgreementRelationship relateFeeds(ServiceLevelAgreement sla, Set<Feed> feeds) {
        JcrFeedServiceLevelAgreementRelationship relationship = null;
        //find if this relationship already exists
        JcrFeedServiceLevelAgreementRelationship feedSla = (JcrFeedServiceLevelAgreementRelationship) findRelationship(sla.getId());
        Set<Node> feedNodes = new HashSet<>();
        Map<String, Object> props = new HashMap<>();
        for (Feed feed : feeds) {
            if (feed != null) {
                JcrFeed jcrFeed = (JcrFeed) feed;
                feedNodes.add(jcrFeed.getNode());
            }
        }
        props.put(JcrFeedServiceLevelAgreementRelationship.FEEDS, feedNodes);
        props.put(JcrFeedServiceLevelAgreementRelationship.SLA, ((JcrServiceLevelAgreement) sla).getNode());

        //remove any existing relationships
        removeFeedRelationships(sla.getId());
        if (feedSla == null) {

            ExtensibleType type = typeProvider.getType(JcrFeedServiceLevelAgreementRelationship.TYPE_NAME);
            JcrExtensibleEntity entity = (JcrExtensibleEntity) entityProvider.createEntity(type, props);

            relationship = new JcrFeedServiceLevelAgreementRelationship(entity.getNode());
        } else {
            JcrExtensibleEntity entity = (JcrExtensibleEntity) entityProvider.updateEntity(feedSla, props);
            relationship = new JcrFeedServiceLevelAgreementRelationship(entity.getNode());
        }
        //update the feed relationships
        for (Feed feed : feeds) {
            feedProvider.updateFeedServiceLevelAgreement(feed.getId(), sla);
        }

        return relationship;
    }


    @Override
    public boolean removeFeedRelationships(ServiceLevelAgreement.ID id) {
        try {
            Session session = getSession();
            JcrFeedServiceLevelAgreementRelationship extensibleEntity = (JcrFeedServiceLevelAgreementRelationship) findRelationship(id);
            if (extensibleEntity != null) {
                JcrMetadataAccess.ensureCheckoutNode(extensibleEntity.getNode());
                for (Feed feed : extensibleEntity.getFeeds()) {
                    JcrFeed jcrFeed = (JcrFeed) feed;
                    JcrMetadataAccess.ensureCheckoutNode(jcrFeed.getNode());
                }
                return extensibleEntity.removeFeedRelationships(id);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("unable to remove Feed SLA relationships for SLA " + id, e);
        }
        return false;
    }


    protected Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }


}
