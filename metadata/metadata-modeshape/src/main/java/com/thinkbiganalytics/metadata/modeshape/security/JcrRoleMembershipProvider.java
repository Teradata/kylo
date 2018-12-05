/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.security.RoleMembershipProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.catalog.connector.JcrConnector;
import com.thinkbiganalytics.metadata.modeshape.catalog.dataset.JcrDataSet;
import com.thinkbiganalytics.metadata.modeshape.catalog.datasource.JcrDataSource;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrUserDatasource;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.project.JcrProject;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrObjectTypeResolver;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.security.role.RoleMembership;
import com.thinkbiganalytics.security.role.SecurityRole;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

/**
 *
 */
public class JcrRoleMembershipProvider implements RoleMembershipProvider {
    
    private static final String ALL_ACCESS_CONTROLLED_QUERY = "SELECT e.* FROM [tba:accessControlled] AS e ";
    
    @Inject
    private CategoryProvider categoryProvider;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleMembershipProvider#findAll()
     */
    @Override
    public Set<RoleMembership> findAll() {
        return Stream.concat(streamEntityMemberships(), 
                             streamCategoryFeedRoleMemberships())
                        .collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleMembershipProvider#findForRole(com.thinkbiganalytics.security.role.SecurityRole)
     */
    @Override
    public Set<RoleMembership> findForRole(SecurityRole role) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private Stream<RoleMembership> streamCategoryFeedRoleMemberships() {
        return this.categoryProvider.findAll().stream()
                .flatMap(cat -> cat.getFeedRoleMemberships().stream());
    }
    
    private Stream<RoleMembership> streamEntityMemberships() {
        return find(ALL_ACCESS_CONTROLLED_QUERY)
                .map(node -> JcrUtil.toJcrObject(node, "tba:accessControlled", TYPE_RESOLVER))
                .map(AccessControlledMixin.class::cast)
                .flatMap(ac -> ac.getRoleMemberships().stream());
    }
    
    private Stream<Node> find(String query) {
        try {
            QueryResult result = JcrQueryUtil.query(JcrMetadataAccess.getActiveSession(), query);
            if (result != null) {
                @SuppressWarnings("unchecked")
                Iterator<Node> itr = result.getNodes();
                Iterable<Node> iterable = () -> itr;
                return StreamSupport.stream(iterable.spliterator(), false);
            } else {
                return Stream.empty();
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to query for role memberships", e);
        }
    }


    private static final Map<String, Class<? extends JcrObject>> NODE_TYPES_MAP;
    static {
        Map<String, Class<? extends JcrObject>> map = new HashMap<>();
        map.put(JcrFeedTemplate.NODE_TYPE, JcrFeedTemplate.class);
        map.put(JcrCategory.NODE_TYPE, JcrCategory.class);
        map.put(JcrFeed.NODE_TYPE, JcrFeed.class);
        map.put(JcrUserDatasource.NODE_TYPE, JcrUserDatasource.class);
        map.put(JcrProject.NODE_TYPE, JcrProject.class);
        map.put(JcrConnector.NODE_TYPE, JcrConnector.class);
        map.put(JcrDataSource.NODE_TYPE, JcrDataSource.class);
        map.put(JcrDataSet.NODE_TYPE, JcrDataSet.class);
        NODE_TYPES_MAP = map;
    }
    
    public static final JcrObjectTypeResolver<? extends JcrObject> TYPE_RESOLVER = new JcrObjectTypeResolver<JcrObject>() {
        @Override
        public Class<? extends JcrObject> resolve(Node node) {
            try {
                if (NODE_TYPES_MAP.containsKey(node.getPrimaryNodeType().getName())) {
                    return NODE_TYPES_MAP.get(node.getPrimaryNodeType().getName());
                } else {
                    throw new MetadataRepositoryException("Unrecognized type of access controlled entity: " + node.getPrimaryNodeType());
                }
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to determine type of node: " + node, e);
            }
        }
    };

}
