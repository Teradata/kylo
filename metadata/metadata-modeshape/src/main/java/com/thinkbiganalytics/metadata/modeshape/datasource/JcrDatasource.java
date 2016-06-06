package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertiesEntity;
import com.thinkbiganalytics.metadata.modeshape.generic.JcrGenericEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.List;

import javax.jcr.Node;

/**
 * Created by sr186054 on 6/5/16.
 */
public class JcrDatasource extends JcrPropertiesEntity {

    public static final String SOURCE_NAME = "feedSources";
    public static final String DESTINATION_NAME = "feedDestinations";


    public JcrDatasource(Node node) {
        super(node);
    }

    private List<FeedSource> sources;

    private List<FeedDestination> destinations;


    public List<JcrSource> getSources(){
     return  JcrUtil.getNodes(this.node,SOURCE_NAME, JcrSource.class);
    }

    public List<JcrDestination> getDestinations(){
        return  JcrUtil.getNodes(this.node, DESTINATION_NAME, JcrDestination.class);
    }

}
