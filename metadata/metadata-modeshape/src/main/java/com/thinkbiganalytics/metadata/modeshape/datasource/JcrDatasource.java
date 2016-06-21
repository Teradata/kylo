package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


    public List<JcrSource> getSources(){
     return  JcrUtil.getNodes(this.node,SOURCE_NAME, JcrSource.class);
    }

    public List<JcrDestination> getDestinations(){
        return  JcrUtil.getNodes(this.node, DESTINATION_NAME, JcrDestination.class);
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
        List<JcrSource> sources = getSources();
        if (sources != null && !sources.isEmpty()) {
            return new HashSet<>(sources);
        }
        return null;
    }

    @Override
    public Set<? extends FeedDestination> getFeedDestinations() {
        List<JcrDestination> destinations = getDestinations();
        if (destinations != null && !destinations.isEmpty()) {
            return new HashSet<>(destinations);
        }
        return null;
    }

    public void setSources(List<FeedSource> sources) {
        //remove and add??

        Node n = JcrUtil.getNode(this.node, SOURCE_NAME);
    }

    public void setDestinations(List<FeedDestination> destinations) {
        Node n = JcrUtil.getNode(this.node, DESTINATION_NAME);
    }
}
