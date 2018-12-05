/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.category;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.PropertiedMixin;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.JcrHadoopSecurityGroup;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrAbstractRoleMembership;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrEntityRoleMembership;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrSecurityRole;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.role.RoleMembership;
import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 */
public class CategoryDetails extends JcrObject implements PropertiedMixin {
    
    public static final String NODE_TYPE = "tba:categoryDetails";
    public static final String HADOOP_SECURITY_GROUPS = "tba:securityGroups";
    public static final String FEED_ROLE_MEMBERSHIPS = "tba:feedRoleMemberships";
    public static final String FEED_ROLE_MEMBERSHIPS_TYPE = "tba:CategoryFeedRoleMemberships";
    public static final String FEED_USER_FIELDS = "tba:feedUserFields";

    private Optional<FeedOpsAccessControlProvider> opsAccessProvider;
    
    /**
     * @param getNode()
     */
    public CategoryDetails(Node node) {
        super(node);
    }
    
    public CategoryDetails(Node node, Optional<FeedOpsAccessControlProvider> opsPvdr) {
        super(node);
        this.opsAccessProvider = opsPvdr;
    }
    
    public List<? extends Feed> getFeeds() {
        List<JcrFeed> feeds = JcrUtil.getChildrenMatchingNodeType(getNode(), 
                                                                  "tba:feed", 
                                                                  JcrFeed.class,
                                                                  this.opsAccessProvider.map(p -> new Object[] { p }).orElse(new Object[0]));
        return feeds;
    }

    @Nonnull
    public Map<String, String> getUserProperties() {
        return JcrPropertyUtil.getUserProperties(getNode());
    }

    public void setUserProperties(@Nonnull final Map<String, String> userProperties, @Nonnull final Set<UserFieldDescriptor> userFields) {
        JcrPropertyUtil.setUserProperties(getNode(), userFields, userProperties);
    }

    public List<? extends HadoopSecurityGroup> getSecurityGroups() {
        Set<Node> list = JcrPropertyUtil.getReferencedNodeSet(getNode(), HADOOP_SECURITY_GROUPS);
        List<HadoopSecurityGroup> hadoopSecurityGroups = new ArrayList<>();
        if (list != null) {
            for (Node n : list) {
                hadoopSecurityGroups.add(JcrUtil.createJcrObject(n, JcrHadoopSecurityGroup.class));
            }
        }
        return hadoopSecurityGroups;
    }

    public void setSecurityGroups(List<? extends HadoopSecurityGroup> hadoopSecurityGroups) {
        JcrPropertyUtil.setProperty(getNode(), HADOOP_SECURITY_GROUPS, null);

        for (HadoopSecurityGroup securityGroup : hadoopSecurityGroups) {
            Node securityGroupNode = ((JcrHadoopSecurityGroup) securityGroup).getNode();
            JcrPropertyUtil.addToSetProperty(getNode(), HADOOP_SECURITY_GROUPS, securityGroupNode, true);
        }
    }

    public Set<RoleMembership> getFeedRoleMemberships() {
        Node defaultsNode = JcrUtil.getNode(getNode(), FEED_ROLE_MEMBERSHIPS);
        return JcrUtil.getPropertyObjectSet(defaultsNode, JcrAbstractRoleMembership.NODE_NAME, JcrFeedDefaultRoleMembership.class, this).stream()
                        .map(RoleMembership.class::cast)
                        .collect(Collectors.toSet());
    }
    
    public Optional<RoleMembership> getFeedRoleMembership(String roleName) {
        Node defaultsNode = JcrUtil.getNode(getNode(), FEED_ROLE_MEMBERSHIPS);
        return JcrEntityRoleMembership.find(defaultsNode, roleName, JcrFeedDefaultRoleMembership.class, this).map(RoleMembership.class::cast);
    }
    
    public void enableFeedRoles(List<SecurityRole> feedRoles) {
        Node feedRolesNode = JcrUtil.getOrCreateNode(getNode(), FEED_ROLE_MEMBERSHIPS, FEED_ROLE_MEMBERSHIPS_TYPE);
        feedRoles.forEach(role -> JcrAbstractRoleMembership.create(feedRolesNode, ((JcrSecurityRole) role).getNode(), JcrFeedDefaultRoleMembership.class, this));
    }
}
