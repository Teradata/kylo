package com.thinkbiganalytics.metadata.modeshape.common;

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

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.UnknownPropertyException;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
  */
public class JcrObject {
    
    private static final Logger log = LoggerFactory.getLogger(JcrObject.class);

    protected final Node node;

    private String versionName;

    private String versionableIdentifier;

    /**
     *
     */
    public JcrObject(Node node) {
        this.node = node;
        String nodeName = null;
        try {
            nodeName = node.getName();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to create JcrObject from node " + nodeName, e);
        }
    }

    public String getObjectId() throws RepositoryException {
        if (this.node.isNodeType("nt:frozenNode")) {
            return this.versionableIdentifier;
        } else {
            return this.node.getIdentifier();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + ": " + this.node.getName();
        } catch (RepositoryException e) {
            return getClass().getSimpleName() + " - error: " + e.getMessage();
        }
    }

    public String getTypeName() {
        try {
            return this.node.getPrimaryNodeType().getName();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity type name", e);
        }
    }

    public boolean isNew() {
        return this.node.isNew();
    }

    public boolean isModified() {
        return this.node.isModified();
    }

    public void refresh(boolean keepChanges) {
        try {
            this.node.refresh(keepChanges);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to refresh Node. ", e);
        }
    }

    public String getPath() {
        try {
            return JcrUtil.getPath(node);
        } catch (AccessControlException e) {
            // If it can't be accessed assume it is hidden 
            return null;
        }
    }

    public String getNodeName() {
        try {
            return JcrUtil.getName(node);
        } catch (AccessControlException e) {
            // If it can't be accessed assume it is hidden 
            return null;
        }
    }

    public void remove() {
        try {
            this.node.remove();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to remove the node", e);
        }
    }

    public boolean isLive() {
        if (this.node != null) {
            try {
                if (this.node.getSession() != null) {
                    return this.node.getSession().isLive();
                }
            } catch (RepositoryException e) {

            }
        }
        return false;
    }

    public Map<String, Object> getProperties() {
        return JcrPropertyUtil.getProperties(this.node);
    }

    public Object getProperty(String name) {
        try {
            return JcrPropertyUtil.getProperty(this.node, name);
        } catch (AccessControlException e) {
            // If it can't be accessed assume it is hidden 
            return null;
        }
    }

    public <T> Set<T> getPropertyAsSet(String name, Class<T> objectType) {
        Object o = null;
        try {
            o = JcrPropertyUtil.getProperty(this.node, name);
        } catch (UnknownPropertyException | AccessControlException e) {

        }
        if (o != null) {
            if (o instanceof Collection) {
                //convert the objects to the correct type if needed
                if (JcrObject.class.isAssignableFrom(objectType)) {
                    Set<T> objects = new HashSet<>();
                    for (Object collectionObj : (Collection) o) {
                        T obj = null;
                        if (collectionObj instanceof Node) {

                            try {
                                obj = ConstructorUtils.invokeConstructor(objectType, (Node) collectionObj);
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                obj = (T) collectionObj;
                            }

                        } else {
                            obj = (T) collectionObj;
                        }
                        objects.add(obj);
                    }
                    return objects;
                } else {
                    return new HashSet<T>((Collection) o);
                }
            } else {
                Set<T> set = new HashSet<>();
                if (JcrObject.class.isAssignableFrom(objectType) && o instanceof Node) {
                    T obj = null;
                    try {
                        obj = ConstructorUtils.invokeConstructor(objectType, (Node) o);
                        set.add((T) obj);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {

                    }
                    set.add(obj);
                } else {
                    set.add((T) o);
                }
                return set;
            }
        } else {
            return new HashSet<T>();
        }
    }

    public <T> T getProperty(String name, Class<T> type) {
        return getProperty(name, type, false);
    }

    public <T> T getProperty(String name, Class<T> type, boolean allowNotFound) {
        return getPropertyFromNode(this.node, name, type, allowNotFound);
    }

    public <T> T getPropertyFromNode(Node node, String name, Class<T> type, boolean allowNotFound) {
        Object o = JcrPropertyUtil.getProperty(node, name, allowNotFound);
        if (allowNotFound && o == null) {
            return null;
        }
        if (type.isEnum()) {
            String savedType = o.toString();
            if (StringUtils.isNotBlank(savedType)) {
                Class<? extends Enum> x = (Class<? extends Enum>) type;
                return (T) Enum.valueOf(x, savedType);
            }
        }
        if (!o.getClass().isAssignableFrom(type)) {
            //if it cant be matched and it is a Node > JcrObject, do the conversion
            if (o instanceof Node && JcrObject.class.isAssignableFrom(type)) {
                return JcrUtil.constructNodeObject((Node) o, type, null);
            } else {
                throw new MetadataRepositoryException("Unable to convert Property " + name + " to type " + type);
            }
        } else {
            return (T) o;
        }
    }

    public void setProperty(String name, Object value) {
        try {
            JcrPropertyUtil.setProperty(this.node, name, value);
        } catch (AccessControlException e) {
            // Re-throw with possibly a better message
            throw new AccessControlException("You do not have the permission to set property \"" + name + "\"");
        }
    }


    public Node getNode() {
        return this.node;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionableIdentifier() {
        return versionableIdentifier;
    }

    public void setVersionableIdentifier(String versionableIdentifier) {
        this.versionableIdentifier = versionableIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass().isInstance(obj)) {
            JcrObject that = (JcrObject) obj;
            return this.node.equals(that.node);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.node.hashCode();
    }
}
