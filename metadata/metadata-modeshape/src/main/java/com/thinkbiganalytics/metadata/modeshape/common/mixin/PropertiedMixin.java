/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.thinkbiganalytics.metadata.modeshape.common.JcrProperties;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 * A mixin interface corresponding to the Kylo JCR mixin type tba:propertied.  Provides default
 * implementations for entity types which have JCR node types that include tba:propertied.
 * 
 * TODO Not currently used - the JcrObject hierarchy should be refactored to use mixin interfaces such as this one.
 */
public interface PropertiedMixin extends WrappedNodeMixin, Propertied {
    
    Logger log = LoggerFactory.getLogger(PropertiedMixin.class);

    String PROPERTIES_NAME = "tba:properties";


    default Optional<JcrProperties> getPropertiesObject() {
        try {
            return Optional.ofNullable(JcrUtil.getJcrObject(getNode(), PROPERTIES_NAME, JcrProperties.class));
        } catch (AccessControlException e) {
            return Optional.empty();
        }
    }
    
    default Optional<JcrProperties> ensurePropertiesObject() {
        try {
            return Optional.of(JcrUtil.getOrCreateNode(getNode(), PROPERTIES_NAME, JcrProperties.NODE_TYPE, JcrProperties.class));
        } catch (AccessControlException e) {
            return Optional.empty();
        }
    }
    
    default void clearAdditionalProperties() {
        getPropertiesObject().ifPresent(propsObj -> {
            try {
                Node propsNode = propsObj.getNode();
                Map<String, Object> props = propsObj.getProperties();
                for (Map.Entry<String, Object> prop : props.entrySet()) {
                    if (!JcrPropertyUtil.hasPropertyDefinition(propsNode, prop.getKey())) {
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

    /**
     * This will return just the extra properties.
     * All primary properties should be defined as getter/setter on the base object
     * You can call the getAllProperties to return the complete set of properties as a map
     */
    // TODO this seems inconsistent as it behaves differently from WrappedNodeMixin but has the same name.
    default Map<String, Object> getProperties() {
        return getPropertiesObject()
                        .map(propsObj -> propsObj.getProperties())
                        .orElse(Collections.emptyMap());
    }

    /**
     * Get the Nodes Properties along with the extra mixin properties
     */
    default Map<String, Object> getAllProperties() {

        //first get the other extra mixin properties
        Map<String, Object> properties = new HashMap<>(getProperties());
        //merge in this nodes properties
        Map<String, Object> thisProperties = WrappedNodeMixin.super.getProperties();
        if (thisProperties != null) {
            properties.putAll(thisProperties);
        }
        return properties;
    }

    /**
     * Returns true if the conditions of {@link WrappedNodeMixin#hasProperty} are met or if the internal properties object
     * contains a value for the named property. 
     * @see com.thinkbiganalytics.metadata.modeshape.common.mixin.WrappedNodeMixin#hasProperty(java.lang.String)
     */
    @Override
    default boolean hasProperty(String name) {
        try {
            if (getNode().hasProperty(name)) {
                return true;
            } else {
                return getPropertiesObject()
                                .map(obj -> JcrPropertyUtil.hasProperty(obj.getNode(), name))
                                .orElse(false);
            }
        } catch (AccessDeniedException e) {
            log.debug("Unable to access property: \"{}\" from node: {}", name, getNode(), e);
            return false;
        } catch (AccessControlException e) {
            return false;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to check Property " + name);
        }
    }

    default <T> T getProperty(String name, Class<T> type) {
        try {
            if ("nt:frozenNode".equalsIgnoreCase(getNode().getPrimaryNodeType().getName())) {
                T item = WrappedNodeMixin.super.getProperty(name, type);
                if (item == null) {
                    item = getPropertiesObject()
                                    .map(obj -> obj.getProperty(name, type))
                                    .orElse(null);
                }
                return item;
            } else {
                if (JcrPropertyUtil.hasPropertyDefinition(getNode(), name)) {
                    return WrappedNodeMixin.super.getProperty(name, type);
                } else {
                    return getPropertiesObject()
                                    .map(obj -> obj.getProperty(name, type))
                                    .orElse(null);
                }
            }
        } catch (AccessDeniedException e) {
            throw new AccessControlException("You do not have the permission to access property \"" + name + "\"");
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get Property " + name);
        }
    }

    @Override
    default <T> T getProperty(String name, Class<T> type, T defaultValue) {
        try {
            if ("nt:frozenNode".equalsIgnoreCase(getNode().getPrimaryNodeType().getName())) {
                T item = WrappedNodeMixin.super.getProperty(name, type, defaultValue);
                if (item == null) {
                    item = getPropertiesObject()
                                    .map(obj -> obj.getProperty(name, type, defaultValue))
                                    .orElse(null);
                }
                return item;
            } else {
                if (hasProperty(name)) {
                    return WrappedNodeMixin.super.getProperty(name, type, defaultValue);
                } else {
                    return getPropertiesObject()
                                    .map(obj -> obj.getProperty(name, type, defaultValue))
                                    .orElse(null);
                }
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denided for property: \"{}\"", name, e);
            // TODO is this correct? 
            return defaultValue;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get Property " + name);
        }
    }

    /**
     * Override
     * if the incoming name matches that of a primary property on this Node then set it, otherwise add it the mixin bag of properties
     */
    default void setProperty(String name, Object value) {
        try {
            if (JcrPropertyUtil.hasPropertyDefinition(getNode(), name)) {
                WrappedNodeMixin.super.setProperty(name, value);
            } else {
                ensurePropertiesObject().ifPresent(obj -> obj.setProperty(name, value));
            }
        } catch (AccessControlException e) {
            if (name.toLowerCase().contains("password")) {
                throw new AccessControlException("You do not have the permission to set property \"UNKNOWN\"");
            }
            else {
                throw new AccessControlException("You do not have the permission to set property \"" + name + "\"");
            }
        }
    }

    /**
     * Merges any new properties in with the other Extra Properties
     */
    @Override
    default Map<String, Object> mergeProperties(Map<String, Object> props) {
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
            throw new AccessControlException("You do not have the permission to set properties");
        }
        return newProps;
    }

    default Map<String, Object> replaceProperties(Map<String, Object> props) {
        clearAdditionalProperties();
        setProperties(props);
        return props;
    }
}
