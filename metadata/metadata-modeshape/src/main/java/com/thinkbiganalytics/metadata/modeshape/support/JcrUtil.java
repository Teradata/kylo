/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.support;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
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
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.modeshape.jcr.api.JcrTools;

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.UnknownPropertyException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

/**
 * @author Sean Felten
 */
public class JcrUtil {

    public static String getString(Node node, String name) {
        try {
            Property prop = node.getProperty(name);
            return prop.getString();
        } catch (PathNotFoundException e) {
            throw new UnknownPropertyException(name, e);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property: " + name, e);
        }
    }


    public static Object getProperty(Node node, String name) {
        return getProperty(node, name, false);
    }

    public static Object getProperty(Node node, String name, boolean allowNotFound) {
        try {
            Property prop = node.getProperty(name);
            return asValue(prop, node.getSession());
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
                propMap.put(prop.getName(), asValue(prop, node.getSession()));
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
                for (Entry<String, Object> entry : props.entrySet()) {
                    Value value = asValue(factory, entry.getValue());
                    entNode.setProperty(entry.getKey(), value);
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
    

    public static Object asValue(Value value) {
        try {
            switch (value.getType()) {
                case (PropertyType.DECIMAL):
                    return value.getDecimal();
                case (PropertyType.STRING):
                    return value.getString();
                case (PropertyType.DOUBLE):
                    return Double.valueOf(value.getDouble());
                case (PropertyType.LONG):
                    return Long.valueOf(value.getLong());
                case (PropertyType.BOOLEAN):
                    return Boolean.valueOf(value.getBoolean());
                case (PropertyType.DATE):
                    return value.getDate().getTime();
                case (PropertyType.BINARY):
                    return IOUtils.toByteArray(value.getBinary().getStream());
                default:
                    return null;
            }
        } catch (RepositoryException | IOException e) {
            throw new MetadataRepositoryException("Failed to access property type", e);
        }
    }


    public static Object asValue(Property prop) {
        return asValue(prop, null);
    }

    public static Object asValue(Property prop, Session session) {
        // STRING, BOOLEAN, LONG, DOUBLE, PATH, ENTITY
        try {
            int code = prop.getType();
            if (prop.isMultiple()) {
                List<Object> objects = new ArrayList<>();
                Value[] values = prop.getValues();
                if (values != null) {
                    for (Value value : values) {
                        Object o = asValue(value);
                        objects.add(o);
                    }
                }
                if (objects.size() == 1) {
                    return objects.get(0);
                } else if (objects.size() > 1) {
                    return objects;
                } else {
                    return null;
                }
            } else {

                if (code == PropertyType.BOOLEAN) {
                    return prop.getBoolean();
                } else if (code == PropertyType.STRING) {
                    return prop.getString();
                } else if (code == PropertyType.LONG) {
                    return prop.getLong();
                } else if (code == PropertyType.DOUBLE) {
                    return prop.getDouble();
                } else if (code == PropertyType.PATH) {
                    return prop.getPath();
                } else if (code == PropertyType.REFERENCE) {
                    String nodeIdentifier = prop.getValue().getString();
                    return lookupNodeReference(nodeIdentifier, session);
                } else if (code == PropertyType.WEAKREFERENCE) {
                    String nodeIdentifier = prop.getValue().getString();
                    return lookupNodeReference(nodeIdentifier, session);
                } else {
                    return asValue(prop.getValue());
                    //return prop.getString();
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

    public static void setProperty(Node node, String name, Object value) {

        try {
            if (node == null) {
                throw new IllegalArgumentException("Cannot set a property on a null-node!");
            }
            if (name == null) {
                throw new IllegalArgumentException("Cannot set a property without a provided name");
            }

            if (value == null) {
                node.setProperty(name, (Value) null);
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
            } else if (value instanceof Date) {
                Calendar cal = Calendar.getInstance();
                cal.setTime((Date) value);
                node.setProperty(name, cal);
            } else if (value instanceof BigDecimal) {
                node.setProperty(name, (BigDecimal) value);
            } else if (value instanceof String) {
                node.setProperty(name, (String) value);
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
                throw new MetadataRepositoryException("Cannot set property to a value of type " + value.getClass());
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set property value: " + name + "=" + value, e);
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

    public static Node getNode(Node parentNode, String name) {
        try {
            return parentNode.getNode(name);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
    }

    /**
     * get All Child nodes under a parentNode and create the wrapped JCRObject the second argument, name, can be null to get all the nodes under the parent
     */
    public static <T extends JcrObject> List<T> getNodes(Node parentNode, String name, Class<T> type) {
        List<T> list = new ArrayList<>();
        try {

            javax.jcr.NodeIterator nodeItr = null;
            if (StringUtils.isBlank(name)) {
                nodeItr = parentNode.getNodes();
            } else {
                nodeItr = parentNode.getNodes(name);
            }
            if (nodeItr != null) {
                while (nodeItr.hasNext()) {
                    Node n = nodeItr.nextNode();
                    T entity = ConstructorUtils.invokeConstructor(type, n);
                    list.add(entity);
                }
            }
        } catch (RepositoryException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
        return list;
    }

    /**
     * Get a child node relative to the parentNode and create the Wrapper object
     */
    public static <T extends JcrObject> T getNode(Node parentNode, String name, Class<T> type) {
        T entity = null;
        try {
            Node n = parentNode.getNode(name);
            entity = ConstructorUtils.invokeConstructor(type, n);
        } catch (RepositoryException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
        return entity;
    }

    /**
     * Get or Create a node relative to the Parent Node and return the Wrapper JcrObject
     */
    public static <T extends JcrObject> T getOrCreateNode(Node parentNode, String name, String nodeType, Class<T> type) {
        return getOrCreateNode(parentNode, name, nodeType, type, null);
    }

    /**
     * Get or Create a node relative to the Parent Node and return the Wrapper JcrObject
     */
    public static <T extends JcrObject> T getOrCreateNode(Node parentNode, String name, String nodeType, Class<T> type, Object[] constructorArgs) {
        T entity = null;
        try {
            JcrTools tools = new JcrTools();
            Node n = tools.findOrCreateChild(parentNode, name, nodeType);
            entity = createJcrObject(n, type, constructorArgs);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
        return entity;
    }

    /**
     * Create a new JcrObject (Wrapper Object) that invokes a constructor with at least parameter of type Node
     */
    public static <T extends JcrObject> T createJcrObject(Node node, Class<T> type) {
        return createJcrObject(node, type, null);
    }

    /**
     * Create a new JcrObject (Wrapper Object) that invokes a constructor with at least parameter of type Node
     */
    public static <T extends JcrObject> T createJcrObject(Node node, Class<T> type, Object[] constructorArgs) {
        return constructNodeObject(node, type, constructorArgs);
    }

    /**
     * Create a new Node Wrapper Object that invokes a constructor with at least parameter of type Node
     */
    public static <T extends Object> T constructNodeObject(Node node, Class<T> type, Object[] constructorArgs) {
        T entity = null;
        try {
            if (constructorArgs != null) {
                constructorArgs = ArrayUtils.add(constructorArgs, 0, node);
            } else {
                constructorArgs = new Object[]{node};
            }

            entity = ConstructorUtils.invokeConstructor(type, constructorArgs);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new MetadataRepositoryException("Failed to createJcrObject for node " + type, e);
        }
        return entity;
    }


    public static <T extends Object> List<T> find(Session session, String query, Class<T> type) {
        JcrTools tools = new JcrTools();
        try {
            QueryResult result = query(session, query);
            return queryResultToList(result, type);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to findAll for query : " + query, e);
        }
    }

    public static <T extends Object> List<T> queryResultToList(QueryResult result, Class<T> type) {
        return queryResultToList(result, type, null);
    }

    public static <T extends Object> List<T> queryResultToList(QueryResult result, Class<T> type, Integer fetchSize) {
        List<T> entities = new ArrayList<>();

        if (result != null) {
            try {
                NodeIterator nodeIterator = result.getNodes();
                int cntr = 0;
                while (nodeIterator.hasNext()) {
                    Node node = nodeIterator.nextNode();
                    T entity = constructNodeObject(node, type, null);
                    entities.add(entity);
                    cntr++;
                    if (fetchSize != null && cntr == fetchSize) {
                        break;
                    }

                }
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Unable to parse QueryResult to List for type  : " + type, e);

            }
        }
        return entities;
    }


    public static <T extends Object> T findFirst(Session session, String query, Class<T> type) {

        JcrTools tools = new JcrTools();
        try {
            QueryResult result = query(session, query);
            List<T> list = queryResultToList(result, type, 1);
            if (list != null && list.size() > 0) {
                return list.get(0);
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to findFirst for query : " + query, e);
        }
    }


    public static QueryResult query(Session session, String queryExpression) throws RepositoryException {
        return query(session, queryExpression, null);
    }

    public static QueryResult query(Session session, String queryExpression, Map<String, String> bindParams) throws RepositoryException {

        QueryResult results = null;

        Query query = session.getWorkspace().getQueryManager().createQuery(queryExpression, "JCR-SQL2");
        if (bindParams != null && !bindParams.isEmpty()) {
            Iterator e = bindParams.entrySet().iterator();

            while (e.hasNext()) {
                Entry entry = (Entry) e.next();
                String key = (String) entry.getKey();
                Value value = session.getValueFactory().createValue((String) entry.getValue());
                query.bindValue(key, value);
            }
        }

        results = query.execute();

        return results;
    }

}
