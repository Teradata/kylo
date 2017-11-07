package com.thinkbiganalytics.metadata.modeshape.datasource;

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

import com.thinkbiganalytics.metadata.api.datasource.DatasourceDetails;
import com.thinkbiganalytics.metadata.api.datasource.JdbcDatasourceDetails;
import com.thinkbiganalytics.metadata.api.datasource.UserDatasource;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.datasource.security.JcrDatasourceAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 * A {@code UserDatasource} that is stored in JRC.
 */
public class JcrUserDatasource extends JcrDatasource implements UserDatasource, AccessControlledMixin {

    private static final Logger log = LoggerFactory.getLogger(JcrUserDatasource.class);
    private static final long serialVersionUID = -6114070693323660125L;

    /**
     * JCR node type
     */
    public static final String NODE_TYPE = "tba:userDatasource";

    /**
     * Sub-node under {@code datasources} node for {@code userDatasource} nodes
     */
    @SuppressWarnings("unused")
    public static final String PATH_NAME = "user";

    /**
     * Name of the details attribute
     */
    static final String DETAILS = "tba:details";

    /**
     * Mapping from domain type to JCR type
     */
    private static final Map<Class<? extends DatasourceDetails>, Class<? extends JcrDatasourceDetails>> DETAILS_DOMAIN_TYPES_MAP = new HashMap<>();

    static {
        DETAILS_DOMAIN_TYPES_MAP.put(JdbcDatasourceDetails.class, JcrJdbcDatasourceDetails.class);
    }

    /**
     * Mapping from JCR type name to JCR type class
     */
    private static final Map<String, Class<? extends JcrDatasourceDetails>> DETAILS_NODE_TYPES_MAP = new HashMap<>();

    static {
        DETAILS_NODE_TYPES_MAP.put(JcrJdbcDatasourceDetails.NODE_TYPE, JcrJdbcDatasourceDetails.class);
    }

    /**
     * Name of the type attribute
     */
    private static final String TYPE = "tba:type";

    @Nonnull
    static Class<? extends JcrDatasourceDetails> resolveDetailsClass(@Nonnull final Class<? extends DatasourceDetails> domainClass) {
        return DETAILS_DOMAIN_TYPES_MAP.getOrDefault(domainClass, JcrDatasourceDetails.class);
    }

    /**
     * Details child node.
     */
    private JcrDatasourceDetails details;

    /**
     * Constructs a {@code JcrUserDatasource} with the specified JCR node.
     *
     * @param node the JCR node
     */
    public JcrUserDatasource(@Nonnull final Node node) {
        super(node);
    }

    @Nonnull
    public Optional<? extends JcrDatasourceDetails> getDetails() {
        if (details == null && JcrUtil.hasNode(getNode(), DETAILS)) {
            details = getDetailsNode()
                .map(this::toDetailsObject)
                .orElse(null);
        }
        return Optional.ofNullable(details);
    }

    @Override
    public Class<? extends JcrAllowedActions> getJcrAllowedActionsType() {
        return JcrDatasourceAllowedActions.class;
    }

    @Override
    public void setName(@Nonnull final String name) {
        setProperty(TITLE, name);
    }

    @Nonnull
    @Override
    public String getType() {
        return getProperty(TYPE, String.class);
    }

    @Override
    public void setType(@Nonnull final String type) {
        setProperty(TYPE, type);
    }

    @Nonnull
    private Optional<Node> getDetailsNode() {
        try {
            return Optional.ofNullable(getNode().getNode(DETAILS));
        } catch (final AccessDeniedException e) {
            log.debug("Access denied to details node for user data source: ", getId(), e);
            return Optional.empty();
        } catch (final PathNotFoundException e) {
            return Optional.empty();
        } catch (final RepositoryException e) {
            throw new MetadataRepositoryException("Failure while finding details node for user data source: " + getId(), e);
        }
    }

    @Nonnull
    private JcrDatasourceDetails toDetailsObject(@Nonnull final Node detailsNode) {
        // Determine type class
        final Class<? extends JcrDatasourceDetails> typeClass;

        try {
            final String typeName = detailsNode.getPrimaryNodeType().getName();
            typeClass = DETAILS_NODE_TYPES_MAP.getOrDefault(typeName, JcrDatasourceDetails.class);
        } catch (final RepositoryException e) {
            throw new MetadataRepositoryException("Failed to determine type of node: " + detailsNode, e);
        }

        // Convert node to object
        return JcrUtil.getJcrObject(detailsNode, typeClass);
    }
}
