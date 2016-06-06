package com.thinkbiganalytics.metadata.modeshape.feed;

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDestination;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrSource;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;


import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/4/16.
 */
public class JcrFeed extends AbstractJcrSystemEntity {


    public static final String SOURCE_NAME = "tba:sources";
    public static final String SOURCE_TYPE = "tba:feedSource";
    public static final String DESTINATION_NAME = "tba:destinations";


    public JcrFeed(Node node) {
        super(node);
    }

    public JcrCategory getCategory(){
        try {
            return new JcrCategory(node.getParent());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Category for feed "+getTitle(),e);
        }
    }


    public List<JcrSource> getSources(){
        return  JcrUtil.getNodes(this.node,SOURCE_NAME, JcrSource.class);
    }

    public List<JcrDestination> getDestinations(){
        return  JcrUtil.getNodes(this.node, DESTINATION_NAME, JcrDestination.class);
    }

}
