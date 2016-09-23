package com.thinkbiganalytics.metadata.modeshape.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.JcrHadoopSecurityGroup;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * An implementation of {@link Category} backed by a JCR repository.
 */
public class JcrCategory extends AbstractJcrAuditableSystemEntity implements Category {

    public static String CATEGORY_NAME = "tba:category";
    public static String NODE_TYPE = "tba:category";
    public static final String HADOOP_SECURITY_GROUPS = "tba:securityGroups";

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
            return new JcrCategory.CategoryId(getObjectId());
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
    public void setDisplayName(String displayName) {
        setTitle(displayName);
    }

    @Override
    public void setName(String name) {
        setSystemName(name);
    }

    @Nonnull
    @Override
    public Map<String, String> getUserProperties() {
        return JcrPropertyUtil.getUserProperties(node);
    }

    @Override
    public void setUserProperties(@Nonnull final Map<String, String> userProperties, @Nonnull final Set<UserFieldDescriptor> userFields) {
        JcrPropertyUtil.setUserProperties(node, userFields, userProperties);
    }

    @Override
    public AllowedActions getAllowedActions() {
        return JcrUtil.getOrCreateNode(this.node, JcrAllowedActions.NODE_NAME, JcrAllowedActions.NODE_TYPE, JcrAllowedActions.class);
    }

    public List<? extends HadoopSecurityGroup> getSecurityGroups() {
        Set<Node> list = JcrPropertyUtil.getReferencedNodeSet(this.node, HADOOP_SECURITY_GROUPS);
        List<HadoopSecurityGroup> hadoopSecurityGroups = new ArrayList<>();
        if (list != null) {
            for (Node n : list) {
                hadoopSecurityGroups.add(JcrUtil.createJcrObject(n, JcrHadoopSecurityGroup.class));
            }
        }
        return hadoopSecurityGroups;
    }

    public void setSecurityGroups(List<? extends HadoopSecurityGroup> hadoopSecurityGroups) {
        JcrPropertyUtil.setProperty(this.node, HADOOP_SECURITY_GROUPS, null);

        for (HadoopSecurityGroup securityGroup : hadoopSecurityGroups) {
            Node securityGroupNode = ((JcrHadoopSecurityGroup) securityGroup).getNode();
            JcrPropertyUtil.addToSetProperty(this.node, HADOOP_SECURITY_GROUPS, securityGroupNode, true);
        }
    }
}
