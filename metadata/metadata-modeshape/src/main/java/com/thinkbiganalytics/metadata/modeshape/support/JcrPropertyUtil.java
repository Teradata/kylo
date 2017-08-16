package com.thinkbiganalytics.metadata.modeshape.support;

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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.thinkbiganalytics.classnameregistry.ClassNameChangeRegistry;
import com.thinkbiganalytics.metadata.api.MissingUserPropertyException;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeBuilder;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptorBuilder;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.UnknownPropertyException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensiblePropertyCollection;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrUserFieldDescriptor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * Utility functions for JCR properties.
 */
public class JcrPropertyUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JcrPropertyUtil.class);

    /**
     * Encoding for user-defined property names
     */
    public static final String USER_PROPERTY_ENCODING = "UTF-8";

    protected static final ObjectWriter writer;
    protected static final ObjectReader reader;

    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(Include.NON_NULL);

        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                                 .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                 .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                 .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));

        reader = mapper.reader();
        writer = mapper.writer();
    }


    /**
     * Instances of {@code JcrPropertyUtil} may not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private JcrPropertyUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getIdentifier(Node node) {
        try {
            return node.getIdentifier();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access identifier property of node: " + node, e);
        }
    }

    public static String getName(Node node) {
        try {
            return node.getName();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access name property of node: " + node, e);
        }
    }

    public static String getName(Property prop) {
        try {
            return prop.getName();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the name of property: " + prop, e);
        }
    }

    public static String getString(Node node, String name) {
        try {
            Property prop = node.getProperty(name);
            return prop.getString();
        } catch (PathNotFoundException e) {
            return null;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property: " + name, e);
        }
    }

    /**
     * Returns the boolean value of the property. if the property is a String type it will try to determine the boolean value. If it cannot get a boolean value, it will return false.
     */
    public static boolean getBoolean(Node node, String name) {
        return getBoolean(node, name, false);
    }

    private static boolean getBoolean(Node node, String name, boolean notFoundValue) {
        try {
            Property prop = node.getProperty(name);
            if (PropertyType.STRING == prop.getType()) {
                return BooleanUtils.toBoolean(prop.getString());
            } else if (PropertyType.BOOLEAN == prop.getType()) {
                return prop.getBoolean();
            }
            return notFoundValue;
        } catch (PathNotFoundException e) {
            return notFoundValue;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property: " + name, e);
        }
    }

    public static boolean getBooleanOrDefault(Node node, String name, boolean notFoundValue) {
        boolean val = notFoundValue;
        try {
            val = getBoolean(node, name);
        } catch (Exception e) {
            //swallow exception and return the default value
        }
        return val;
    }

    public static Long getLong(Node node, String name) {
        try {
            Property prop = node.getProperty(name);
            return prop.getLong();
        } catch (PathNotFoundException e) {
            return null;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property: " + name, e);
        }
    }

    public static <E extends Enum<E>> E getEnum(Node node, String name, Class<E> enumType, E defaultValue) {
        try {
            Property prop = node.getProperty(name);
            return Enum.valueOf(enumType, prop.getString());
        } catch (PathNotFoundException e) {
            return defaultValue;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property: " + name, e);
        }
    }

    private static <T> T readJsonValue(String name, Class<T> type, String json) {
        try {
            return reader.forType(type).readValue(json);
        } catch (IOException e) {
            if (ExceptionUtils.getRootCause(e) instanceof ClassNotFoundException) {
                //attempt to find the old class name and replace it with the new one
                ClassNotFoundException classNotFoundException = (ClassNotFoundException) ExceptionUtils.getRootCause(e);
                String msg = classNotFoundException.getMessage();
                msg = StringUtils.remove(msg, "java.lang.ClassNotFound:");
                String oldName = StringUtils.trim(msg);
                try {
                    Class newName = ClassNameChangeRegistry.findClass(oldName);
                    String newNameString = newName.getName();
                    if (StringUtils.contains(json, oldName)) {
                        //replace and try again
                        json = StringUtils.replace(json, oldName, newNameString);
                        return readJsonValue(name, type, json);
                    }
                } catch (ClassNotFoundException c) {

                }


            }
            throw new MetadataRepositoryException("Failed to deserialize JSON property: " + name, e);
        }
    }

    public static <T> T getJsonObject(Node node, String name, Class<T> type) {
        String json = getString(node, name);
        return readJsonValue(name, type, json);
    }

    public static <T> void setJsonObject(Node node, String name, Object value) {
        try {
            String json = writer.forType(value.getClass()).writeValueAsString(value);

            setProperty(node, name, json);
        } catch (IOException e) {
            throw new MetadataRepositoryException("Failed to serialize JSON property: " + value, e);
        }
    }

    public static Optional<Property> findProperty(Node node, String name) {
        try {
            if (node.hasProperty(name)) {
                return Optional.of(node.getProperty(name));
            } else {
                return Optional.empty();
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed attempting to locate a property: " + name, e);
        }
    }

    public static <T> T getProperty(Node node, String name) {
        return getProperty(node, name, false);
    }

    public static <T> T getProperty(Node node, String name, boolean allowNotFound) {
        try {
            Property prop = node.getProperty(name);
            return asValue(prop);
        } catch (PathNotFoundException e) {
            if (allowNotFound) {
                return null;
            } else {
                throw new UnknownPropertyException(name, e);
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property: " + name, e);
        }
    }

    public static Map<String, Object> getProperties(Node node) {
        try {
            Map<String, Object> propMap = new HashMap<>();
            PropertyIterator itr = node.getProperties();

            while (itr.hasNext()) {
                try {
                    Property prop = (Property) itr.next();
                    Object value = asValue(prop);
                    propMap.put(prop.getName(), value);
                } catch (AccessDeniedException e) {
                    log.debug("Access denied - skipping property", e);
                }
            }

            return propMap;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access properties", e);
        }
    }

    public static Node setProperties(Session session, Node entNode, Map<String, Object> props) {
        ValueFactory factory;
        try {
            factory = session.getValueFactory();

            if (props != null) {
//                JcrVersionUtil.ensureCheckoutNode(entNode);
                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    if (entry.getValue() instanceof JcrExtensiblePropertyCollection) {
                        JcrExtensiblePropertyCollection propertyCollection = ((JcrExtensiblePropertyCollection) entry.getValue());
                        propertyCollection.getCollectionType();
                        Value[] values = new Value[propertyCollection.getCollection().size()];
                        int i = 0;
                        for (Object o : propertyCollection.getCollection()) {
                            boolean weak = false;
                            if (propertyCollection.getCollectionType() == PropertyType.WEAKREFERENCE) {
                                weak = true;
                            }
                            Value value = createValue(session, o, weak);
                            values[i] = value;
                            i++;
                        }
                        entNode.setProperty(entry.getKey(), values);
                    } else {
                        Value value = asValue(factory, entry.getValue());
                        entNode.setProperty(entry.getKey(), value);
                    }
                }
            }

            return entNode;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set properties", e);
        }
    }
    
    public static boolean hasProperty(Node node, String propName) {
        try {
            return node.hasProperty(propName);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to test for property", e);
        }
    }

    public static boolean hasProperty(NodeType type, String propName) {
        for (PropertyDefinition propDef : type.getPropertyDefinitions()) {
            if (propDef.getName().equals(propName)) {
                return true;
            }
        }

        return false;
    }

//
//    public static <T> T asValue(Property prop) {
//        return asValue(prop, null);
//    }

    @SuppressWarnings("unchecked")
    public static <T> T asValue(Value value, Session session) throws AccessDeniedException {
        try {
            switch (value.getType()) {
                case PropertyType.DECIMAL:
                    return (T) value.getDecimal();
                case PropertyType.STRING:
                    return (T) value.getString();
                case PropertyType.DOUBLE:
                    return (T) Double.valueOf(value.getDouble());
                case PropertyType.LONG:
                    return (T) Long.valueOf(value.getLong());
                case PropertyType.BOOLEAN:
                    return (T) Boolean.valueOf(value.getBoolean());
                case PropertyType.DATE:
                    return (T) new DateTime(value.getDate().getTime());
                case PropertyType.BINARY:
                    return (T) IOUtils.toByteArray(value.getBinary().getStream());
                case PropertyType.REFERENCE:
                    String ref = value.getString();
                    return (T) session.getNodeByIdentifier(ref);
                case PropertyType.WEAKREFERENCE:
                    String wref = value.getString();
                    try {
                        return (T) session.getNodeByIdentifier(wref);
                    } catch (ItemNotFoundException e) {
                        return null;
                    }
                default:
                    return (T) value.getString();
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RepositoryException | IOException e) {
            throw new MetadataRepositoryException("Failed to access property type", e);
        }
    }

    public static String toString(Property prop) {
        try {
            return prop.getString();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get string value of property: " + prop, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T asValue(Property prop) {
        // STRING, BOOLEAN, LONG, DOUBLE, PATH, ENTITY
        try {
            int code = prop.getType();
            if (prop.isMultiple()) {
                List<T> list = new ArrayList<>();
                Value[] values = prop.getValues();
                if (values != null) {
                    for (Value value : values) {
                        try {
                            T o = asValue(value, prop.getSession());
                            if (o != null) {
                                list.add(o);
                            }
                        } catch (AccessDeniedException e) {
                            // We are not allowd to see the value (likely a node reference) then
                            // just ignore this value in the result list.
                        }
                    }
                }
                if (list.size() > 0) {
                    return (T) list;
                } else {
                    return (T) Collections.emptyList();
                }
            } else {
                if (code == PropertyType.BOOLEAN) {
                    return (T) Boolean.valueOf(prop.getBoolean());
                } else if (code == PropertyType.STRING) {
                    return (T) prop.getString();
                } else if (code == PropertyType.LONG) {
                    return (T) Long.valueOf(prop.getLong());
                } else if (code == PropertyType.DOUBLE) {
                    return (T) Double.valueOf(prop.getDouble());
                } else if (code == PropertyType.PATH) {
                    return (T) prop.getPath();
                } else if (code == PropertyType.REFERENCE || code == PropertyType.WEAKREFERENCE) {
                    try {
                        return (T) prop.getNode();
                    } catch (AccessDeniedException e) {
                        // We are not allowd to see the referenced node so return null;
                        return null;
                    }
                } else {
                    return (T) asValue(prop.getValue(), prop.getSession());
                }
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property type", e);
        }
    }

    public static Node lookupNodeReference(String nodeIdentifier, Session session) {
        Node n = null;
        if (session != null) {
            try {
                n = session.getNodeByIdentifier(nodeIdentifier);
            } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {

            }
        }
        return n;
    }

    public static void setWeakReferenceProperty(Node node, String name, Node ref) {
        try {
            //ensure checked out
//            JcrVersionUtil.ensureCheckoutNode(node);

            if (node == null) {
                throw new IllegalArgumentException("Cannot set a property on a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot set a property without a provided name");
            }

            Value weakRef = node.getSession().getValueFactory().createValue(ref, true);
            node.setProperty(name, weakRef);

        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set weak ref property value: " + name + "=" + ref, e);
        }

    }

    public static void setProperty(Node node, String name, Object value) {
        try {
            //ensure checked out
//            JcrVersionUtil.ensureCheckoutNode(node);

            if (node == null) {
                throw new IllegalArgumentException("Cannot set a property on a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot set a property without a provided name");
            }

            if (value == null) {
                node.setProperty(name, (Value) null);
            } else if (value instanceof Enum) {
                node.setProperty(name, ((Enum) value).name());
            } else if (value instanceof JcrObject) {
                node.setProperty(name, ((JcrObject) value).getNode());
            } else if (value instanceof Value) {
                node.setProperty(name, (Value) value);
            } else if (value instanceof Node) {
                node.setProperty(name, (Node) value);
            } else if (value instanceof Binary) {
                node.setProperty(name, (Binary) value);
            } else if (value instanceof Calendar) {
                node.setProperty(name, (Calendar) value);
            } else if (value instanceof DateTime) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(((DateTime) value).toDate());
                node.setProperty(name, cal);
            } else if (value instanceof Date) {
                Calendar cal = Calendar.getInstance();
                cal.setTime((Date) value);
                node.setProperty(name, cal);
            } else if (value instanceof BigDecimal) {
                node.setProperty(name, (BigDecimal) value);
            } else if (value instanceof Long) {
                node.setProperty(name, ((Long) value).longValue());
            } else if (value instanceof Double) {
                node.setProperty(name, (Double) value);
            } else if (value instanceof Boolean) {
                node.setProperty(name, (Boolean) value);
            } else if (value instanceof InputStream) {
                node.setProperty(name, (InputStream) value);
            } else if (value instanceof Collection) {
                String[] list = new String[((Collection<Object>) value).size()];
                int pos = 0;
                for (Object cal : (Collection<Object>) value) {
                    list[pos] = cal.toString();
                    pos += 1;
                }
                node.setProperty(name, list);
            } else {
                node.setProperty(name, value.toString());
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set property value: " + name + "=" + value, e);
        }
    }

    /**
     * Used to retrieve the referenced nodes from a multi-valued property of type (WEAK)REFERENCE
     */
    public static Set<Node> getReferencedNodeSet(Node node, String propName) {
        try {
            if (node == null) {
                throw new IllegalArgumentException("Cannot set a property on a null-node!");
            }
            if (propName == null) {
                throw new IllegalArgumentException("Cannot set a property without a provided name");
            }

            final Session session = node.getSession();
//            JcrVersionUtil.ensureCheckoutNode(node);

            if (node.hasProperty(propName)) {
                return Arrays.stream(node.getProperty(propName).getValues())
                    .map(v -> {
                        try {
                            return (Node) JcrPropertyUtil.asValue(v, session);
                        } catch (AccessDeniedException e) {
                            // Not allowed to see the referenced node so return null.
                            return null;
                        }
                    })
                    .filter(n -> n != null)  // weak refs can produce null nodes
                    .collect(Collectors.toSet());
            } else {
                return new HashSet<>();
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the node property set: " + propName, e);
        }
    }

    public static boolean addToSetProperty(Node node, String name, Object value) {
        return addToSetProperty(node, name, value, false);
    }

    public static boolean addToSetProperty(Node node, String name, Object value, boolean weakReference) {
        try {
//            JcrVersionUtil.ensureCheckoutNode(node);

            if (node == null) {
                throw new IllegalArgumentException("Cannot set a property on a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot set a property without a provided name");
            }

            Set<Value> values = null;

            if (node.hasProperty(name)) {
                values = Arrays.stream(node.getProperty(name).getValues()).map(v -> {
                    if (PropertyType.REFERENCE == v.getType() && weakReference) {
                        try {
                            Node n = JcrPropertyUtil.asValue(v, node.getSession());
                            return n.getSession().getValueFactory().createValue(n, true);
                        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
                            throw new MetadataRepositoryException("Failed to add to set property: " + name + "->" + value, e);
                        }
                    } else {
                        return v;
                    }
                }).collect(Collectors.toSet());
            } else {
                values = new HashSet<>();
            }

            Value newVal = createValue(node.getSession(), value, weakReference);

            boolean result = values.add(newVal);
            if (weakReference) {
                Property property = node.setProperty(name, (Value[]) values.stream().toArray(size -> new Value[size]), PropertyType.WEAKREFERENCE);
            } else {
                Property property = node.setProperty(name, (Value[]) values.stream().toArray(size -> new Value[size]));
            }

            return result;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add to set property: " + name + "->" + value, e);
        }
    }

    public static boolean removeAllFromSetProperty(Node node, String name) {
        try {
//            JcrVersionUtil.ensureCheckoutNode(node);
            if (node == null) {
                throw new IllegalArgumentException("Cannot remove a property from a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot remove a property without a provided name");
            }

            Set<Value> values = new HashSet<>();
            node.setProperty(name, (Value[]) values.stream().toArray(size -> new Value[size]));
            return true;

        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove set property: " + name, e);
        }
    }


    public static boolean removeFromSetProperty(Node node, String name, Object value) {
        return removeFromSetProperty(node, name, value, false);
    }

    public static boolean removeFromSetProperty(Node node, String name, Object value, boolean weakRef) {
        try {
//            JcrVersionUtil.ensureCheckoutNode(node);

            if (node == null) {
                throw new IllegalArgumentException("Cannot remove a property from a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot remove a property without a provided name");
            }

            Set<Value> values = new HashSet<>();

            if (node.hasProperty(name)) {
                values = Arrays.stream(node.getProperty(name).getValues()).collect(Collectors.toSet());
            } else {
                values = new HashSet<>();
            }

            Value existingVal = createValue(node.getSession(), value, values.stream().anyMatch(v -> v.getType() == PropertyType.WEAKREFERENCE));
            boolean result = values.remove(existingVal);
            node.setProperty(name, (Value[]) values.stream().toArray(size -> new Value[size]));
            return result;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove from set property: " + name + "->" + value, e);
        }
    }

    public static <T> Set<T> getSetProperty(Node node, String name) {
        try {
            if (node == null) {
                throw new IllegalArgumentException("Cannot set a property on a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot set a property without a provided name");
            }

            if (node.hasProperty(name)) {
                Set<T> result = new HashSet<T>((Collection<T>) getProperty(node, name));
                return result;
            } else {
                return Collections.emptySet();
            }
        } catch (ClassCastException e) {
            throw new MetadataRepositoryException("Wrong property data type for set", e);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get set property: " + name, e);
        }
    }

    public static Value createValue(Session session, Object value) {
        return createValue(session, value, false);
    }

    public static Value createValue(Session session, Object value, boolean weakRef) {
        try {
            ValueFactory factory = session.getValueFactory();
            if (value == null) {
                throw new IllegalArgumentException("Cannot create a value from null");
            } else if (value instanceof Enum) {
                return factory.createValue(((Enum) value).name());
            } else if (value instanceof JcrObject) {
                return factory.createValue(((JcrObject) value).getNode(), weakRef);
//                return factory.createValue(((JcrObject) value).getNode().getIdentifier(), weakRef ? PropertyType.WEAKREFERENCE : PropertyType.REFERENCE);
            } else if (value instanceof Value) {
                return (Value) value;
            } else if (value instanceof Node) {
//                return factory.createValue((Node) value, weakRef);
                return factory.createValue(((Node) value).getIdentifier(), weakRef ? PropertyType.WEAKREFERENCE : PropertyType.REFERENCE);
            } else if (value instanceof Binary) {
                return factory.createValue((Binary) value);
            } else if (value instanceof Calendar) {
                return factory.createValue((Calendar) value);
            } else if (value instanceof DateTime) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(((DateTime) value).toDate());
                return factory.createValue(cal);
            } else if (value instanceof Date) {
                Calendar cal = Calendar.getInstance();
                cal.setTime((Date) value);
                return factory.createValue(cal);
            } else if (value instanceof BigDecimal) {
                return factory.createValue((BigDecimal) value);
            } else if (value instanceof Long) {
                return factory.createValue(((Long) value).longValue());
            } else if (value instanceof Double) {
                return factory.createValue((Double) value);
            } else if (value instanceof Boolean) {
                return factory.createValue((Boolean) value);
            } else if (value instanceof InputStream) {
                return factory.createValue((InputStream) value);
//        } else if (value instanceof Collection) {
//            String[] list = new String[((Collection<Object>) value).size()];
//            int pos = 0;
//            for (Object cal : (Collection<Object>) value) {
//                list[pos] = cal.toString();
//                pos += 1;
//            }
//            return factory.createValue(list);
            } else {
                return factory.createValue(value.toString());
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create value frpm: " + value, e);
        }
    }

    public static int getJCRPropertyType(Object obj) {
        if (obj instanceof String) {
            return PropertyType.STRING;
        }
        if (obj instanceof Double) {
            return PropertyType.DOUBLE;
        }
        if (obj instanceof Float) {
            return PropertyType.DOUBLE;
        }
        if (obj instanceof Long) {
            return PropertyType.LONG;
        }
        if (obj instanceof Integer) {
            return PropertyType.LONG;
        }
        if (obj instanceof Boolean) {
            return PropertyType.BOOLEAN;
        }
        if (obj instanceof Calendar) {
            return PropertyType.DATE;
        }
        if (obj instanceof Binary) {
            return PropertyType.BINARY;
        }
        if (obj instanceof InputStream) {
            return PropertyType.BINARY;
        }
        if (obj instanceof JcrExtensiblePropertyCollection) {
            return JcrExtensiblePropertyCollection.COLLECTION_TYPE;
        }
        if (obj instanceof Node) {
            return PropertyType.REFERENCE;
        }
        return PropertyType.UNDEFINED;
    }

    public static Value asValue(ValueFactory factory, Object obj) {
        // STRING, BOOLEAN, LONG, DOUBLE, PATH, ENTITY
        try {
            switch (getJCRPropertyType(obj)) {
                case PropertyType.STRING:
                    return factory.createValue((String) obj);
                case PropertyType.BOOLEAN:
                    return factory.createValue((Boolean) obj);
                case PropertyType.DATE:
                    return factory.createValue((Calendar) obj);
                case PropertyType.LONG:
                    return obj instanceof Long ? factory.createValue(((Long) obj).longValue()) : factory.createValue(((Integer) obj).longValue());
                case PropertyType.DOUBLE:
                    return obj instanceof Double ? factory.createValue((Double) obj) : factory.createValue(((Float) obj).doubleValue());
                case PropertyType.BINARY:
                    return factory.createValue((InputStream) obj);
                case PropertyType.REFERENCE:
                    return factory.createValue((Node) obj);
                default:
                    return (obj != null ? factory.createValue(obj.toString()) : factory.createValue(StringUtils.EMPTY));
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Invalid value format", e);
        }
    }

    /**
     * Assuming the specified property is a (WEAK)REFERENCE type, returns whether it is pointing at the specified node.
     */
    public static boolean isReferencing(Node node, String refProp, Node targetNode) {
        try {
            return node.getProperty(refProp).getNode().isSame(targetNode);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to check reference property against node: " + node, e);
        }
    }

    /**
     * Assuming the specified property is a (WEAK)REFERENCE type, returns whether it is pointing at the specified node ID.
     */
    public static boolean isReferencing(Node node, String refProp, String nodeId) {
        try {
            return node.getProperty(refProp).getNode().getIdentifier().equals(nodeId);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to check reference property against node ID: " + nodeId, e);
        }
    }

    /**
     * Gets the user-defined fields for the specified type.
     *
     * @param name                   the type name
     * @param extensibleTypeProvider the type provider
     * @return the user-defined fields
     */
    @Nonnull
    public static Set<UserFieldDescriptor> getUserFields(@Nonnull final String name, @Nonnull final ExtensibleTypeProvider extensibleTypeProvider) {
        final ExtensibleType type = extensibleTypeProvider.getType(name);
        return (type != null) ? type.getUserFieldDescriptors() : Collections.emptySet();
    }

    /**
     * Sets the user-defined fields for the specified type.
     *
     * @param name                   the type name
     * @param fields                 the user-defined fields
     * @param extensibleTypeProvider the type provider
     */
    public static void setUserFields(@Nonnull final String name, @Nonnull final Set<UserFieldDescriptor> fields, @Nonnull final ExtensibleTypeProvider extensibleTypeProvider) {
        // Get type builder
        final ExtensibleTypeBuilder builder;
        final ExtensibleType type = extensibleTypeProvider.getType(name);

        if (type == null) {
            builder = extensibleTypeProvider.buildType(name);
        } else {
            builder = extensibleTypeProvider.updateType(type.getId());
        }

        // Add fields to type
        final String prefix = JcrMetadataAccess.USR_PREFIX + ":";

        fields.forEach(field -> {
            // Encode field name
            final String systemName;
            try {
                systemName = prefix + URLEncoder.encode(field.getSystemName(), USER_PROPERTY_ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e.toString(), e);
            }

            // Create field descriptor
            final FieldDescriptorBuilder fieldBuilder = builder.field(systemName);
            fieldBuilder.description(field.getDescription());
            fieldBuilder.displayName(field.getDisplayName());
            fieldBuilder.type(FieldDescriptor.Type.STRING);
            fieldBuilder.property(JcrUserFieldDescriptor.ORDER, Integer.toString(field.getOrder()));
            fieldBuilder.property(JcrUserFieldDescriptor.REQUIRED, Boolean.toString(field.isRequired()));
            fieldBuilder.add();
        });
        builder.build();
    }

    /**
     * Gets the user-defined property names and values for the specified node.
     *
     * @param node the node to be searched
     * @return a map of property names to values
     * @throws IllegalStateException       if a property name is encoded incorrectly
     * @throws MetadataRepositoryException if the metadata repository is unavailable
     */
    @Nonnull
    public static Map<String, String> getUserProperties(@Nonnull final Node node) {
        // Get node properties
        final PropertyIterator iterator;
        try {
            iterator = node.getProperties();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get properties for node: " + node, e);
        }

        // Convert iterator to map
        final String prefix = JcrMetadataAccess.USR_PREFIX + ":";
        final int prefixLength = prefix.length();
        final Map<String, String> properties = new HashMap<>((int) Math.min(iterator.getSize(), Integer.MAX_VALUE));

        while (iterator.hasNext()) {
            final Property property = iterator.nextProperty();
            try {
                if (property.getName().startsWith(prefix)) {
                    properties.put(URLDecoder.decode(property.getName().substring(prefixLength), USER_PROPERTY_ENCODING), property.getString());
                }
            } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to access property \"" + property + "\" on node: " + node, e);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Unsupported encoding for property \"" + property + "\" on node: " + node, e);
            }
        }

        return properties;
    }

    /**
     * Sets the specified user-defined properties on the specified node.
     *
     * @param node       the target node
     * @param fields     the predefined user fields
     * @param properties the map of user-defined property names to values
     * @throws IllegalStateException       if a property name is encoded incorrectly
     * @throws MetadataRepositoryException if the metadata repository is unavailable
     */
    public static void setUserProperties(@Nonnull final Node node, @Nonnull final Set<UserFieldDescriptor> fields, @Nonnull final Map<String, String> properties) {
        // Verify required properties are not empty
        for (final UserFieldDescriptor field : fields) {
            if (field.isRequired() && StringUtils.isEmpty(properties.get(field.getSystemName()))) {
                throw new MissingUserPropertyException("Missing required property: " + field.getSystemName());
            }
        }

        // Set properties on node
        final Set<String> newProperties = new HashSet<>(properties.size());
        final String prefix = JcrMetadataAccess.USR_PREFIX + ":";

        properties.forEach((key, value) -> {
            try {
                final String name = prefix + URLEncoder.encode(key, USER_PROPERTY_ENCODING);
                newProperties.add(name);
                node.setProperty(name, value);
            } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to set user property \"" + key + "\" on node: " + node, e);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e.toString(), e);
            }
        });

        // Get node properties
        final PropertyIterator iterator;
        try {
            iterator = node.getProperties();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get properties for node: " + node, e);
        }

        // Remove properties from node
        while (iterator.hasNext()) {
            final Property property = iterator.nextProperty();
            try {
                final String name = property.getName();
                if (name.startsWith(prefix) && !newProperties.contains(name)) {
                    property.remove();
                }
            } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to remove property \"" + property + "\" on node: " + node, e);
            }
        }
    }

    /**
     * Copies a property, if present, from the source node to the destination node.
     *
     * @param src  source node
     * @param dest destination node
     * @param name the name of the property
     * @return whether the source had a value to copy
     */
    public static boolean copyProperty(Node src, Node dest, String name) {
        try {
            if (src.hasProperty(name)) {
                Property prop = src.getProperty(name);

                if (prop.isMultiple()) {
                    Value[] values = prop.getValues();
                    dest.setProperty(name, values, prop.getType());
                } else {
                    Value value = prop.getValue();
                    dest.setProperty(name, value, prop.getType());
                }

                return true;
            } else {
                return false;
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to copy property \"" + name + "\" from node " + src + " to " + dest, e);
        }
    }

    public static Node getParent(Property prop) {
        try {
            return prop.getParent();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the parent node of the property: " + prop, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Iterable<Property> propertiesIterable(Node node) {
        return () -> {
            try {
                return node.getProperties();
            } catch (AccessDeniedException e) {
                log.debug("Access denied", e);
                throw new AccessControlException(e.getMessage());
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to get the properties of node: " + node, e);
            }
        };
    }

    /**
     * @param node
     */
    public static Stream<Property> streamProperties(Node node) {
        return StreamSupport.stream(propertiesIterable(node).spliterator(), false);
    }
}
