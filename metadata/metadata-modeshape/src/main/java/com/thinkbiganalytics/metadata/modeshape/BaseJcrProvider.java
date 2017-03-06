package com.thinkbiganalytics.metadata.modeshape;

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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrTool;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.modeshape.jcr.api.JcrTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

/**
 */
public abstract class BaseJcrProvider<T, PK extends Serializable> implements BaseProvider<T, PK> {

    private static final Logger log = LoggerFactory.getLogger(BaseJcrProvider.class);

    private static final Pattern INVALID_SYSTEM_NAME_PATTERN = Pattern.compile("[^(A-Z)(a-z)(0-9)_-]");
    protected Class<T> entityClass;
    protected Class<? extends JcrEntity> jcrEntityClass;

    public BaseJcrProvider() {
        this.entityClass = (Class<T>) getEntityClass();
        this.jcrEntityClass = getJcrEntityClass();
    }

    protected Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }

    public void save() {
        try {
            getSession().save();
        } catch (RepositoryException e) {
            log.error("Failed to save session state.", e);
            throw new MetadataRepositoryException("Failed to save the current session state.", e);
        }
    }

    public abstract Class<? extends T> getEntityClass();

    public abstract Class<? extends JcrEntity> getJcrEntityClass();

    /**
     * return the JCR NodeType for this entity (i.e. tba:category, tba:feed)
     */
    public abstract String getNodeType(Class<? extends JcrEntity> jcrEntityType);

    /**
     * \
     * Gets the entity class appropriate for the given node type if polymophic types are
     * supported by the provider implementation.  By default if simply returns the result
     * of getJcrEntityClass().
     *
     * @return the appropriate entity class
     */
    public Class<? extends JcrEntity> getJcrEntityClass(String jcrNodeType) {
        return getJcrEntityClass();
    }

    /**
     * \
     * Gets the entity class appropriate for the given node polymophic types are
     * supported by the provider implementation.  By default if simply returns the result
     * of getJcrEntityClass(String nodeType) by calling getPrimaryNodeType().name() on the node.
     *
     * @return the appropriate entity class
     */
    public Class getJcrEntityClass(Node node) {
        try {
            return getJcrEntityClass(node.getPrimaryNodeType().getName());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to determine type of node: " + node, e);
        }
    }

    /**
     * Tests whether an Entity Node exists for a Parent Path and relative Path.
     */
    public boolean hasEntityNode(String parentPath, String relPath) {
        Session session = getSession();

        try {
            Node typesNode = session.getNode(parentPath);
            return JcrUtil.hasNode(typesNode, relPath);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new entity of type: " + getEntityClass(), e);
        }
    }
    
    /**
     * Creates a new Entity Node object for a Parent Path, relative Path and node type
     */
    public Node findOrCreateEntityNode(String parentPath, String relPath, Class<? extends JcrEntity> jcrEntityType) {
        Session session = getSession();
        
        try {
            Node typesNode = session.getNode(parentPath);
            JcrTools tools = new JcrTool();
            Node entNode = tools.findOrCreateChild(typesNode, relPath, getNodeType(jcrEntityType));
            return entNode;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new entity of type: " + getEntityClass(), e);
        }
    }


    public T findOrCreateEntity(String path, String relPath, Map<String, Object> props) {
        return findOrCreateEntity(path, relPath, props, null);
    }

    public T findOrCreateEntity(String path, String relPath, Map<String, Object> props, Object... constructorArgs) {
        return findOrCreateEntity(path, relPath, getJcrEntityClass(), props, constructorArgs);
    }

    public T findOrCreateEntity(String path, String relPath, Class<? extends JcrEntity> entClass) {
        return findOrCreateEntity(path, relPath, entClass, null);
    }

    public T findOrCreateEntity(String path, String relPath, Class<? extends JcrEntity> entClass, Map<String, Object> props, Object... constructorArgs) {
        Session session = getSession();
        Node entNode = findOrCreateEntityNode(path, relPath, entClass);
        entNode = JcrPropertyUtil.setProperties(session, entNode, props);
        Class<? extends JcrEntity> actualClass = getJcrEntityClass(entNode); // Handle subtypes
        return (T) JcrUtil.createJcrObject(entNode, actualClass, constructorArgs);
    }

    public Node getNodeByIdentifier(PK id) {
        try {
            Node node = getSession().getNodeByIdentifier(id.toString());
            return node;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failure while finding entity by ID: " + id, e);
        }
    }

    @Override
    public T findById(PK id) {
        try {
            Node node = null;
            try {
                node = getSession().getNodeByIdentifier(id.toString());
            } catch (ItemNotFoundException e) {
                //swallow this exception
                // if we dont find the item then return null
            }
            if (node != null) {
                return (T) constructEntity(node);
            } else {
                return null;
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denided to feed with ID: {}", id, e);
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failure while finding entity by ID: " + id, e);
        }
    }

    protected T constructEntity(Node node) {
        @SuppressWarnings("unchecked")
        T entity = (T) constructEntity(node, getJcrEntityClass(node));
        return entity;
    }

    protected <T extends JcrObject> T constructEntity(Node node, Class<T> entityClass) {
        return JcrUtil.createJcrObject(node, entityClass);
    }

    public List<T> findWithExplainPlan(String queryExpression) {

        try {
            org.modeshape.jcr.api.query.Query query = (org.modeshape.jcr.api.query.Query) getSession().getWorkspace().getQueryManager().createQuery(queryExpression, "JCR-SQL2");
            org.modeshape.jcr.api.query.QueryResult result = query.explain();
            String plan = result.getPlan();
            log.info(plan);
            return find(queryExpression);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failure while finding entity ", e);
        }

    }

    public List<Node> findNodes(String query) {
        return Lists.newArrayList(findIterableNodes(query));
    }

    public List<T> find(String query) {
        List<T> entities = new ArrayList<>();
        try {
            QueryResult result = JcrQueryUtil.query(getSession(), query);
            if (result != null) {
                NodeIterator nodeIterator = result.getNodes();
                while (nodeIterator.hasNext()) {
                    Node node = nodeIterator.nextNode();
                    T entity = constructEntity(node);
                    entities.add(entity);
                }
            }
            return entities;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to findAll for Type : " + getNodeType(getJcrEntityClass()), e);
        }
    }

    public <D, R> Iterable<D> findIterable(String query, Class<D> domainClass, Class<R> resultClass) {
        return () -> {
            return StreamSupport.stream(findIterableNodes(query).spliterator(), false)
                .map(node -> {
                    try {
                        @SuppressWarnings("unchecked")
                        D entity = (D) ConstructorUtils.invokeConstructor(resultClass, node);
                        return entity;
                    } catch (Exception e) {
                        throw new MetadataRepositoryException("Failed to create entity: " + resultClass, e);
                    }
                })
                .iterator();
        };
    }

    public T findFirst(String query) {
        try {
            QueryResult result = JcrQueryUtil.query(getSession(), query);
            if (result != null) {
                NodeIterator nodeIterator = result.getNodes();
                if (nodeIterator.hasNext()) {
                    Node node = nodeIterator.nextNode();
                    T entity = constructEntity(node);
                    return entity;
                }
            }
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to findAll for Type : " + getNodeType(getJcrEntityClass()), e);
        }
    }

    public <T extends JcrObject> T findFirst(String query, Class<T> resultClass) {
        try {
            QueryResult result = JcrQueryUtil.query(getSession(), query);
            if (result != null) {
                NodeIterator nodeIterator = result.getNodes();
                if (nodeIterator.hasNext()) {
                    Node node = nodeIterator.nextNode();
                    T entity = constructEntity(node, resultClass);
                    return entity;
                }
            }
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to findAll for Type : " + getNodeType(getJcrEntityClass()), e);
        }
    }

    @Override
    public List<T> findAll() {

        String jcrQuery = "SELECT * FROM [" + getNodeType(getJcrEntityClass()) + "]";
        return find(jcrQuery);

    }

    @Override
    public T create(T t) {
        try {
            getSession().save();
            return t;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to save session", e);
        }
    }

    @Override
    public T update(T t) {
        try {
            getSession().save();
            return t;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to save session", e);
        }
    }

    @Override
    public void delete(T t) {
        if (t != null) {
            if (t instanceof JcrObject) {
                JcrObject jcrObject = (JcrObject) t;
                jcrObject.remove();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public void deleteById(PK id) {
        T item = findById(id);
        delete(item);
    }


    public Iterable<Node> findIterableNodes(String query) {
        return () -> {
            try {
                QueryResult result = JcrQueryUtil.query(getSession(), query);
                @SuppressWarnings("unchecked")
                Iterator<Node> itr = (Iterator<Node>) result.getNodes();
                return itr;
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failure executing query: " + query, e);
            }
        };
    }

    protected String sanitizeSystemName(String systemName) {
        return INVALID_SYSTEM_NAME_PATTERN.matcher(systemName).replaceAll("_");
    }

    protected String sanitizeTitle(String title) {
        return StringEscapeUtils.escapeJava(title);
    }

}
