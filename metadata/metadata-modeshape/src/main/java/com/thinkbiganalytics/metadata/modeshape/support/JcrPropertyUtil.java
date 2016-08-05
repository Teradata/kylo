package com.thinkbiganalytics.metadata.modeshape.support;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.UnknownPropertyException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensiblePropertyCollection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
import java.util.Set;
import java.util.stream.Collectors;

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
 * Created by sr186054 on 6/13/16.
 */
public class JcrPropertyUtil {

    protected static final ObjectWriter writer;
    protected static final ObjectReader reader;

    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(Include.NON_NULL);
        
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                 .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                 .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                 .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE) );
        
        reader = mapper.reader();
        writer = mapper.writer();
    }

    
    public static String getString(Node node, String name) {
        try {
            Property prop = node.getProperty(name);
            return prop.getString();
        } catch (PathNotFoundException e) {
            return null;
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
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property: " + name, e);
        }
    }

    
    public static <T> T getJsonObject(Node node, String name, Class<T> type) {
        try {
            String json = getString(node, name);
            
            return reader.forType(type).readValue(json);
        } catch (IOException e) {
            throw new MetadataRepositoryException("Failed to deserialize JSON property: " + name, e);
        }
    }
    
    public static <T> void setJsonObject(Node node, String name, Object value) {
        try {
            String json = writer.forType(value.getClass()).writeValueAsString(value);
            
            setProperty(node, name, json);
        } catch (IOException e) {
            throw new MetadataRepositoryException("Failed to serialize JSON property: " + value, e);
        }
    }

    public static Object getProperty(Node node, String name) {
        return getProperty(node, name, false);
    }

    public static Object getProperty(Node node, String name, boolean allowNotFound) {
        try {
            Property prop = node.getProperty(name);
            return asValue(prop);
        } catch (PathNotFoundException e) {
            if (allowNotFound) {
                return null;
            } else {
                throw new UnknownPropertyException(name, e);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property: " + name, e);
        }
    }

    public static Map<String, Object> getProperties(Node node) {
        try {
            Map<String, Object> propMap = new HashMap<>();
            PropertyIterator itr = node.getProperties();

            while (itr.hasNext()) {
                Property prop = (Property) itr.next();
                propMap.put(prop.getName(), asValue(prop));
            }

            return propMap;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access properties", e);
        }
    }

    public static Node setProperties(Session session, Node entNode, Map<String, Object> props) {
        ValueFactory factory;
        try {
            factory = session.getValueFactory();

            if (props != null) {
                JcrMetadataAccess.ensureCheckoutNode(entNode);
                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    if(entry.getValue() instanceof JcrExtensiblePropertyCollection){
                        JcrExtensiblePropertyCollection propertyCollection = ((JcrExtensiblePropertyCollection)entry.getValue());
                        propertyCollection.getCollectionType();
                        Value[] values = new Value[propertyCollection.getCollection().size()];
                        int i = 0;
                        for(Object o: propertyCollection.getCollection()){
                            boolean weak = false;
                            if(propertyCollection.getCollectionType() == PropertyType.WEAKREFERENCE){
                                weak = true;
                            }
                           Value value = createValue(session,o,weak);
                            values[i] = value;
                            i++;
                        }
                        entNode.setProperty(entry.getKey(),values);
                    }
                    else {
                        Value value = asValue(factory, entry.getValue());
                        entNode.setProperty(entry.getKey(), value);
                    }
                }
            }

            return entNode;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set properties", e);
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


    @SuppressWarnings("unchecked")
    public static <T> T asValue(Value value, Session session) {
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
        } catch (RepositoryException | IOException e) {
            throw new MetadataRepositoryException("Failed to access property type", e);
        }
    }

//
//    public static <T> T asValue(Property prop) {
//        return asValue(prop, null);
//    }

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
                        T o = asValue(value, prop.getSession()); 
                        list.add(o);
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
                    return (T) prop.getNode();
                } else {
                    return (T) asValue(prop.getValue(), prop.getSession());
                }
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property type", e);
        }
    }

    public static Node lookupNodeReference(String nodeIdentifier, Session session) {
        Node n = null;
        if (session != null) {
            try {
                n = session.getNodeByIdentifier(nodeIdentifier);
            } catch (RepositoryException e) {

            }
        }
        return n;
    }

    public static void setWeakReferenceProperty(Node node, String name, Node ref) {
        try {
            //ensure checked out
            JcrMetadataAccess.ensureCheckoutNode(node);

            if (node == null) {
                throw new IllegalArgumentException("Cannot set a property on a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot set a property without a provided name");
            }

            Value weakRef = node.getSession().getValueFactory().createValue(ref, true);
            node.setProperty(name, weakRef);

        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set weak ref property value: " + name + "=" + ref, e);
        }

    }


    public static void setProperty(Node node, String name, Object value) {
        try {
            //ensure checked out
            JcrMetadataAccess.ensureCheckoutNode(node);
            
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
            JcrMetadataAccess.ensureCheckoutNode(node);
            
            if (node.hasProperty(propName)) {
                return Arrays.stream(node.getProperty(propName).getValues())
                                .map(v -> (Node) JcrPropertyUtil.asValue(v, session))
                                .filter(n -> n != null)  // weak refs can produce null nodes
                                .collect(Collectors.toSet());
            } else {
                return new HashSet<>();
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the node property set: " + propName, e);
        }
    }
    
    public static boolean addToSetProperty(Node node, String name, Object value) {
        return addToSetProperty(node, name, value, false);
    }

    public static boolean addToSetProperty(Node node, String name, Object value, boolean weakReference) {
        try {
            JcrMetadataAccess.ensureCheckoutNode(node);

            if (node == null) {
                throw new IllegalArgumentException("Cannot set a property on a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot set a property without a provided name");
            }

            Set<Value> values = new HashSet<>();

            if (node.hasProperty(name)) {
                values = Arrays.stream(node.getProperty(name).getValues()).map(v -> {
                    if (PropertyType.REFERENCE == v.getType() && weakReference) {
                        try {
                            Node n = JcrPropertyUtil.asValue(v, node.getSession());
                            return n.getSession().getValueFactory().createValue(n, true);
                        } catch (RepositoryException e) {
                            throw new MetadataRepositoryException("Failed to add to set property: " + name + "->" + value, e);
                        }
                    }
                    return v;
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

            /**
             * .map(v -> {
             if (v instanceof Node && PropertyType.REFERENCE == v.getType() && weakReference) {
             JcrValue vv = (JcrValue) v;
             Node n = (Node) v;
             try {
             return n.getSession().getValueFactory().createValue(n, true);
             } catch (RepositoryException e) {
             throw new MetadataRepositoryException("Failed to add to set property: " + name + "->" + value, e);
             }
             }
             return v;
             })
             */
            return result;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add to set property: " + name + "->" + value, e);
        }
    }

    public static boolean removeAllFromSetProperty(Node node, String name) {
        try {
            JcrMetadataAccess.ensureCheckoutNode(node);
            if (node == null) {
                throw new IllegalArgumentException("Cannot remove a property from a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot remove a property without a provided name");
            }

            Set<Value> values = new HashSet<>();
            node.setProperty(name, (Value[]) values.stream().toArray(size -> new Value[size]));
            return true;

        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove set property: " + name, e);
        }
    }
    
    public static boolean removeFromSetProperty(Node node, String name, Object value) {
        try {
            JcrMetadataAccess.ensureCheckoutNode(node);
            
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
            
            Value existingVal = createValue(node.getSession(), value);
            boolean result = values.remove(existingVal);
            node.setProperty(name, (Value[]) values.stream().toArray(size -> new Value[size]));
            return result;
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
                Set<Node> result = new HashSet<Node>((List<Node>) getProperty(node, name));
                return (Set<T>) result;
            } else {
                return Collections.emptySet();
            }
        } catch (ClassCastException e) {
            throw new MetadataRepositoryException("Wrong property data type for set", e);
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
            } else if (value instanceof Value) {
                return (Value) value;
            } else if (value instanceof Node) {
                return factory.createValue((Node) value, weakRef);
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
        if(obj instanceof JcrExtensiblePropertyCollection){
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
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Invalid value format", e);
        }
    }
    
    /**
     * Assuming the specified property is a (WEAK)REFERENCE type, returns whether it is pointing at the specified node.
     */
    public static boolean isReferencing(Node node, String refProp, Node targetNode) {
        try {
            return isReferencing(node, refProp, targetNode.getIdentifier());
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
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to check reference property against node ID: " + nodeId, e);
        }
    }

}
