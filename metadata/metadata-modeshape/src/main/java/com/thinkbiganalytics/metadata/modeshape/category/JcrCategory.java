package com.thinkbiganalytics.metadata.modeshape.category;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.security.RoleAssignments;
import com.thinkbiganalytics.metadata.modeshape.JcrAccessControlledSupport;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.JcrHadoopSecurityGroup;
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

    public static final String HADOOP_SECURITY_GROUPS = "tba:securityGroups";
    public static String CATEGORY_NAME = "tba:category";
    public static String NODE_TYPE = "tba:category";
    public static String ICON = "tba:icon";
    public static String ICON_COLOR = "tba:iconColor";

    private JcrAccessControlledSupport accessControlled;

    public JcrCategory(Node node) {
        super(node);
        this.accessControlled = new JcrAccessControlledSupport(node);
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

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public void setDisplayName(String displayName) {
        setTitle(displayName);
    }

    @Override
    public String getName() {
        return getSystemName();
    }

    @Override
    public void setName(String name) {
        if (!getName().equals(name)) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Integer getVersion() {
        return null;
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
        return this.accessControlled.getAllowedActions();
    }

    @Override
    public RoleAssignments getRoleAssignments() {
        return this.accessControlled.getRoleAssignments();
    }

    @Override
    public String getIcon() {
        return getProperty(ICON, String.class, true);
    }

    public void setIcon(String icon) {
        setProperty(ICON, icon);
    }

    @Override
    public String getIconColor() {
        return getProperty(ICON_COLOR, String.class, true);
    }

    public void setIconColor(String iconColor) {
        setProperty(ICON_COLOR, iconColor);
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

    public static class CategoryId extends JcrEntity.EntityId implements Category.ID {

        public CategoryId(Serializable ser) {
            super(ser);
        }
    }
}
