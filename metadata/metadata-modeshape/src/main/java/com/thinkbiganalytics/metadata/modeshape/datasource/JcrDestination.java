package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertiesEntity;
import com.thinkbiganalytics.metadata.modeshape.generic.JcrGenericEntity;

import javax.jcr.Node;

/**
 * Created by sr186054 on 6/5/16.
 */
public class JcrDestination extends JcrPropertiesEntity {


    public JcrDestination(Node node) {
        super(node);
    }
}
