package com.thinkbiganalytics.metadata.modeshape.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryNotFoundException;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;

/**
 * Created by sr186054 on 6/4/16.
 */
public class JcrFeed<C extends Category> extends AbstractJcrAuditableSystemEntity implements Feed<C> {

    public static final String PRECONDITION_TYPE = "tba:feedPrecondition";

    public static final String PRECONDITION = "tba:precondition";
    public static final String DEPENDENTS = "tba:dependentFeeds";
    public static final String NODE_TYPE = "tba:feed";
    public static final String SOURCE_NAME = "tba:sources";
    public static final String DESTINATION_NAME = "tba:destinations";
    public static final String CATEGORY = "tba:category";

    public static final String STATE = "tba:state";

    public static final String TEMPLATE = "tba:template";
    public static final String SCHEDULE_PERIOD = "tba:schedulingPeriod"; // Cron expression, or Timer Expression
    public static final String SCHEDULE_STRATEGY = "tba:schedulingStrategy"; //CRON_DRIVEN, TIMER_DRIVEN


    public JcrFeed(Node node) {
        super(node);
    }

    public JcrFeed(Node node, JcrCategory category) {
        super(node);
        setProperty(CATEGORY, category);
    }

    @Override
    public FeedId getId() {
        try {
            return new JcrFeed.FeedId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }
    public static class FeedId extends JcrEntity.EntityId implements Feed.ID {

        public FeedId(Serializable ser) {
            super(ser);
        }
    }

    protected C getCategory(Class<? extends JcrCategory> categoryClass) {
        C category = null;
        try {
            category = (C) getProperty(JcrFeed.CATEGORY, categoryClass);
        } catch (Exception e) {
            if (category == null) {
                try {
                    category = (C) JcrUtil.constructNodeObject(node.getParent(), categoryClass, null);
                } catch (Exception e2) {
                    throw new CategoryNotFoundException("Unable to find category on Feed for category type  " + categoryClass + ". Exception: " + e.getMessage(), null);
                }
            }
        }
        if (category == null) {
            throw new CategoryNotFoundException("Unable to find category on Feed ", null);
        }
        return category;

    }

    public C getCategory() {

        return (C) getCategory(JcrCategory.class);
    }

    public FeedManagerTemplate getTemplate() {
        return getProperty(TEMPLATE, JcrFeedTemplate.class);
    }

    public void setTemplate(FeedManagerTemplate template) {
        setProperty(TEMPLATE, template);
    }

    public List<? extends FeedSource> getSources() {
        return JcrUtil.getNodes(this.node, SOURCE_NAME, JcrFeedSource.class);
    }

    public List<? extends FeedDestination> getDestinations() {
        return JcrUtil.getNodes(this.node, DESTINATION_NAME, JcrFeedDestination.class);
    }


    @Override
    public String getName() {
        return getSystemName();
    }
    
    @Override
    public String getQualifiedName() {
        return getCategory().getName() + "." + getName();
    }

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public State getState() {
        return getProperty(STATE, Feed.State.ENABLED);
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public FeedPrecondition getPrecondition() {
        try {
            if (this.node.hasNode(PRECONDITION)) {
                return new JcrFeedPrecondition(this.node.getNode(PRECONDITION), this);
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the feed precondition", e);
        }
    }
    
    @Override
    public List<Feed<C>> getDependentFeeds() {
        List<Feed<C>> deps = new ArrayList<>();
        Set<Node> depNodes = JcrPropertyUtil.getSetProperty(this.node, DEPENDENTS);
        
        for (Node depNode : depNodes) {
            deps.add(new JcrFeed<C>(depNode));
        }
        
        return deps;
    }
    
    @Override
    public boolean addDependentFeed(Feed<?> feed) {
        JcrFeed<?> dependent = (JcrFeed<?>) feed;
        Node depNode = dependent.getNode();
        
        return JcrPropertyUtil.addToSetProperty(this.node, DEPENDENTS, depNode);
    }
    
    @Override
    public boolean removeDependentFeed(Feed<?> feed) {
        JcrFeed<?> dependent = (JcrFeed<?>) feed;
        Node depNode = dependent.getNode();
        
        return JcrPropertyUtil.removeFromSetProperty(this.node, DEPENDENTS, depNode);
    }

    @Override
    public FeedSource getSource(final Datasource.ID id) {
        return JcrUtil.getNodelist(this.node, SOURCE_NAME).stream()
                        .filter(node -> JcrPropertyUtil.isReferencing(node, JcrFeedConnection.DATASOURCE, id.toString()))
                        .findAny()
                        .map(node -> new JcrFeedSource(node))
                        .orElse(null);
    }
//
//    @Override
//    public FeedSource getSource(final FeedSource.ID id) {
//        @SuppressWarnings("unchecked")
//        List<FeedSource> sources = (List<FeedSource>) getSources();
//        FeedSource source = null;
//        
//        if (sources != null && !sources.isEmpty()) {
//            source = Iterables.tryFind(sources, new Predicate<FeedSource>() {
//                @Override
//                public boolean apply(FeedSource jcrSource) {
//                    return jcrSource.getId().equals(id);
//                }
//            }).orNull();
//        }
//        return source;
//
//    }

    @Override
    public FeedDestination getDestination(final Datasource.ID id) {
        return JcrPropertyUtil.getReferencedNodeSet(this.node, DESTINATION_NAME).stream()
                        .filter(node -> JcrPropertyUtil.isReferencing(this.node, JcrFeedConnection.DATASOURCE, id.toString()))
                        .findAny()
                        .map(node -> new JcrFeedDestination(node))
                        .orElse(null);
    }
//
//    @Override
//    public FeedDestination getDestination(final FeedDestination.ID id) {
//        @SuppressWarnings("unchecked")
//        List<FeedDestination> destinations = (List<FeedDestination>) getDestinations();
//        FeedDestination destination = null;
//
//        if (destinations != null && !destinations.isEmpty()) {
//            destination = Iterables.tryFind(destinations, new Predicate<FeedDestination>() {
//                @Override
//                public boolean apply(FeedDestination jcrDestination) {
//                    return jcrDestination.getId().equals(id);
//                }
//            }).orNull();
//        }
//        return destination;
//    }

    @Override
    public void setInitialized(boolean flag) {

    }

    @Override
    public void setDisplayName(String name) {
        setTitle(name);
    }

    @Override
    public void setState(State state) {
        setProperty(STATE, state);
    }


    public String getSchedulePeriod(){
        return getProperty(SCHEDULE_PERIOD,String.class);
    }
    public void setSchedulePeriod(String schedulePeriod){
        setProperty(SCHEDULE_PERIOD,schedulePeriod);
    }

    public String getScheduleStrategy(){
        return getProperty(SCHEDULE_STRATEGY,String.class);
    }
    
    public void setScheduleStrategy(String scheduleStrategy){
        setProperty(SCHEDULE_STRATEGY,scheduleStrategy);
    }
    
    public void setPrecondition(JcrServiceLevelAgreement sla) {
//        Node precondNode
    }

}
