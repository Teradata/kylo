package com.thinkbiganalytics.metadata.modeshape.category;

import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertiesEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.List;
import java.util.Set;

import javax.jcr.Node;

/**
 * Created by sr186054 on 6/5/16.
 */
public class JcrCategory extends AbstractJcrSystemEntity {

    public static String CATEGORY_NAME = "tba:category";
    public static String CATEGORY_TYPE = "tba:category";

    public JcrCategory(Node node) {
        super(node);
    }


    public List<JcrFeed> getFeeds(){
        return JcrUtil.getNodes(this.node,null,JcrFeed.class);
    }






}
