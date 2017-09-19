/**
 *
 */
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

import com.thinkbiganalytics.metadata.api.Propertied;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessControlException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

/**
 *
 */
public class JcrPropertiesEntity extends JcrEntity implements Propertied {
    
    private static final Logger log = LoggerFactory.getLogger(JcrPropertiesEntity.class);

    public static final String PROPERTIES_NAME = "tba:properties";

    /**
     *
     */
    public JcrPropertiesEntity(Node node) {
        super(node);
    }

    public Optional<JcrProperties> getPropertiesObject() {
        try {
            return Optional.ofNullable(JcrUtil.getJcrObject(this.node, PROPERTIES_NAME, JcrProperties.class));
        } catch (AccessControlException e) {
            return Optional.empty();
        }
    }
    
    public Optional<JcrProperties> ensurePropertiesObject() {
        try {
            return Optional.of(JcrUtil.getOrCreateNode(this.node, PROPERTIES_NAME, JcrProperties.NODE_TYPE, JcrProperties.class));
        } catch (AccessControlException e) {
            return Optional.empty();
        }
    }
    public void clearAdditionalProperties() {
        getPropertiesObject().ifPresent(propsObj -> {
            try {
                Node propsNode = propsObj.getNode();
                Map<String, Object> props = propsObj.getProperties();
                for (Map.Entry<String, Object> prop : props.entrySet()) {
                    if (!JcrPropertyUtil.hasProperty(propsNode.getPrimaryNodeType(), prop.getKey())) {
                        try {
                            Property property = propsNode.getProperty(prop.getKey());
                            property.remove();
                        } catch (AccessDeniedException e) {
                            // Failed remove the extra property
                            log.debug("Access denied", e);
                            throw new AccessControlException("You do not have the permission to remove property \"" + prop.getKey() + "\"");
                        }
                    }
                }
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Unable to clear the Properties for this entity. ", e);
            }
        });
    }

    @Override
    /**
     * This will return just the extra properties.
     * All primary properties should be defined as getter/setter on the base object
     * You can call the getAllProperties to return the complete set of properties as a map
     */
    public Map<String, Object> getProperties() {
        return getPropertiesObject()
                        .map(propsObj -> propsObj.getProperties())
                        .orElse(Collections.emptyMap());
    }

    public void setProperties(Map<String, Object> properties) {
        //add the properties as attrs
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            setProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get the Nodes Properties along with the extra mixin properties
     */
    public Map<String, Object> getAllProperties() {

        //first get the other extra mixin properties
        Map<String, Object> properties = new HashMap<>(getProperties());
        //merge in this nodes properties
        Map<String, Object> thisProperties = super.getProperties();
        if (thisProperties != null) {
            properties.putAll(thisProperties);
        }
        return properties;
    }

    public <T> T getProperty(String name, T defValue) {
        if (hasProperty(name)) {
            return getProperty(name, (Class<T>) defValue.getClass(), false);
        } else {
            return defValue;
        }
    }

    public boolean hasProperty(String name) {
        try {
            if (this.node.hasProperty(name)) {
                return true;
            } else {
                return getPropertiesObject()
                                .map(obj -> JcrPropertyUtil.hasProperty(obj.getNode(), name))
                                .orElse(false);
            }
        } catch (AccessDeniedException e) {
            log.debug("Unable to access property: \"{}\" from node: {}", name, this.node, e);
            return false;
        } catch (AccessControlException e) {
            return false;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to check Property " + name);
        }
    }

    public <T> T getProperty(String name, Class<T> type) {
        return getProperty(name, type, true);
    }


    @Override
    public <T> T getProperty(String name, Class<T> type, boolean allowNotFound) {
        try {
            if ("nt:frozenNode".equalsIgnoreCase(this.node.getPrimaryNodeType().getName())) {
                T item = super.getProperty(name, type, true);
                if (item == null) {
                    item = getPropertiesObject()
                                    .map(obj -> obj.getProperty(name, type, allowNotFound))
                                    .orElse(null);
                }
                return item;
            } else {
                if (JcrPropertyUtil.hasProperty(this.node.getPrimaryNodeType(), name)) {
                    return super.getProperty(name, type, allowNotFound);
                } else {
                    return getPropertiesObject()
                                    .map(obj -> obj.getProperty(name, type, allowNotFound))
                                    .orElse(null);
                }
            }
        } catch (AccessDeniedException e) {
            log.debug("Unable to access property: \"{}\" from node: {}", name, this.node, e);
            if (allowNotFound) {
                return null;
            } else {
                throw new AccessControlException("You do not have the permission to access property \"" + name + "\"");
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get Property " + name);
        }

    }

    /**
     * Override
     * if the incoming name matches that of a primary property on this Node then set it, otherwise add it the mixin bag of properties
     */
    public void setProperty(String name, Object value) {
        try {
            if (JcrPropertyUtil.hasProperty(this.node.getPrimaryNodeType(), name)) {
                super.setProperty(name, value);
            } else {
                ensurePropertiesObject().ifPresent(obj -> obj.setProperty(name, value));
            }
        } catch (AccessControlException e) {
            throw new AccessControlException("You do not have the permission to set property \"" + name + "\"");
        } catch (AccessDeniedException e) {
            log.debug("Unable to set property: \"{}\" on node: {}", name, this.node, e);
            throw new AccessControlException("You do not have the permission to set property \"" + name + "\"");
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to set Property " + name + ":" + value);
        }
    }

    /**
     * Merges any new properties in with the other Extra Properties
     */
    @Override
    public Map<String, Object> mergeProperties(Map<String, Object> props) {
        Map<String, Object> newProps = new HashMap<>();
        Map<String, Object> origProps = getProperties();
        if (origProps != null) {
            newProps.putAll(origProps);
        }
        if (props != null) {
            newProps.putAll(props);
        }
        
        Optional<JcrProperties> propsObj = ensurePropertiesObject();
        
        if (propsObj.isPresent()) {
            for (Map.Entry<String, Object> entry : newProps.entrySet()) {
                try {
                    propsObj.get().setProperty(entry.getKey(), entry.getValue());
                } catch (MetadataRepositoryException e) {
                    if (ExceptionUtils.getRootCause(e) instanceof ConstraintViolationException) {
                        //this is ok
                    } else {
                        throw e;
                    }
                }
            }
        } else {
            log.debug("Unable to set property: \"{}\" on node: {}", getNode(), this.node);
            throw new AccessControlException("You do not have the permission to set properties");
        }
        return newProps;
    }

    public Map<String, Object> replaceProperties(Map<String, Object> props) {
        clearAdditionalProperties();
        setProperties(props);
        return props;
    }

    @Override
    public void removeProperty(String key) {
        setProperty(key, null);
    }
}
