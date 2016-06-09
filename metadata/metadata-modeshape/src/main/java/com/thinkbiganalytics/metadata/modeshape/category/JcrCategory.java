package com.thinkbiganalytics.metadata.modeshape.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/5/16.
 */
public class JcrCategory extends AbstractJcrSystemEntity implements Category{

    public static String CATEGORY_NAME = "tba:category";
    public static String NODE_TYPE = "tba:category";

    public JcrCategory(Node node) {
        super(node);
    }


    public List<? extends Feed> getFeeds() {
        List<JcrFeed> feeds = JcrUtil.getChildrenMatchingNodeType(this.node, "tba:feed", JcrFeed.class);
        return feeds;
    }

    @Override
    public CategoryId getId() {
        try {
            return new JcrCategory.CategoryId(this.node.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }
     public static class CategoryId extends JcrEntity.EntityId implements Category.ID {

         public CategoryId(Serializable ser) {
             super(ser);
         }
     }


    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public String getName() {
        return getSystemName();
    }

    @Override
    public Integer getVersion() {
        return null;
    }

    @Override
    public DateTime getCreatedTime() {
        return null;
    }

    @Override
    public DateTime getModifiedTime() {
        return null;
    }

    @Override
    public void setDisplayName(String displayName) {
        setTitle(displayName);
    }

    @Override
    public void setName(String name) {
        setSystemName(name);
    }

    @Override
    public void setCreatedTime(DateTime createdTime) {

    }

    @Override
    public void setModifiedTime(DateTime modifiedTime) {

    }
}
