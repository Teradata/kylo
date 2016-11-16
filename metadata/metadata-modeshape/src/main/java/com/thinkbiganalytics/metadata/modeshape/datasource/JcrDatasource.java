package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedDestination;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedSource;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/5/16.
 */
public class JcrDatasource extends AbstractJcrAuditableSystemEntity implements Datasource {
    


    public static final String NODE_TYPE = "tba:datasource";
    public static final String SOURCE_NAME = "tba:feedSources";
    public static final String DESTINATION_NAME = "tba:feedDestinations";

    public static final String TYPE_NAME = "datasourceType";


    public JcrDatasource(Node node) {
        super(node);
    }


    @Override
    public DatasourceId getId() {
        try {
            return new JcrDatasource.DatasourceId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    public static class DatasourceId extends JcrEntity.EntityId implements Datasource.ID {

        public DatasourceId(Serializable ser) {
            super(ser);
        }
    }


    public List<JcrFeedSource> getSources() {
        return JcrUtil.getJcrObjects(this.node, SOURCE_NAME, JcrFeedSource.class);
    }

    public List<JcrFeedDestination> getDestinations() {
        return JcrUtil.getJcrObjects(this.node, DESTINATION_NAME, JcrFeedDestination.class);
    }


    @Override
    public String getName() {
        return super.getProperty(TITLE, String.class);
    }

    @Override
    public String getDescription() {
        return super.getProperty(DESCRIPTION, String.class);
    }


    @Override
    public Set<? extends FeedSource> getFeedSources() {
        return JcrPropertyUtil.getReferencedNodeSet(this.node, SOURCE_NAME).stream()
                        .map(n -> JcrUtil.createJcrObject(n, JcrFeedSource.class))
                        .collect(Collectors.toSet());
    }

    @Override
    public Set<? extends FeedDestination> getFeedDestinations() {
        return JcrPropertyUtil.getReferencedNodeSet(this.node, DESTINATION_NAME).stream()
            .map(n -> JcrUtil.createJcrObject(n, JcrFeedDestination.class))
            .collect(Collectors.toSet());
    }

    public void setSources(List<FeedSource> sources) {
        JcrPropertyUtil.setProperty(this.node, SOURCE_NAME, null);
        
        for (FeedSource src : sources) {
            Node destNode = ((JcrFeedSource) src).getNode();
            addSourceNode(destNode);
        }
    }

    public void setDestinations(List<FeedDestination> destinations) {
        JcrPropertyUtil.setProperty(this.node, DESTINATION_NAME, null);
        
        for (FeedDestination dest : destinations) {
            Node destNode = ((JcrFeedSource) dest).getNode();
            addDestinationNode(destNode);
        }
    }


    public void addSourceNode(Node node) {
        JcrPropertyUtil.addToSetProperty(this.node, SOURCE_NAME, node, true);
    }
    
    public void removeSourceNode(Node node) {
        JcrPropertyUtil.removeFromSetProperty(this.node, SOURCE_NAME, node);
    }
    
    public void addDestinationNode(Node node) {
        JcrPropertyUtil.addToSetProperty(this.node, DESTINATION_NAME, node, true);
    }
    
    public void removeDestinationNode(Node node) {
        JcrPropertyUtil.removeFromSetProperty(this.node, DESTINATION_NAME, node);
    }


}
