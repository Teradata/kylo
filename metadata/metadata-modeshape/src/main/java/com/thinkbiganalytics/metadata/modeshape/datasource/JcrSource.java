package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertiesEntity;

import javax.jcr.Node;

/**
 * Created by sr186054 on 6/5/16.
 */
public class JcrSource extends JcrPropertiesEntity {

    public JcrSource(JcrDatasource datasource, Node node) {
        super(node);
        this.datasource = datasource;
    }


    private JcrDatasource datasource;



}
