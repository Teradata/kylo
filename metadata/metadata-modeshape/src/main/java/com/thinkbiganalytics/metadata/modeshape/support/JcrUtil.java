/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.support;

import com.thinkbiganalytics.metadata.api.generic.GenericType;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.UnknownPropertyException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.modeshape.jcr.api.JcrTools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

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
        try {
            Property prop = node.getProperty(name);
            return asValue(prop);
        } catch (PathNotFoundException e) {
            throw new UnknownPropertyException(name, e);
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

    public static Map<String, GenericType.PropertyType> getPropertyTypes(Node node) {
        try {
            return getPropertyTypes(node.getPrimaryNodeType());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property types", e);
        }
    }

    public static Map<String, GenericType.PropertyType> getPropertyTypes(NodeType type) {
        Map<String, GenericType.PropertyType> typeMap = new HashMap<>();
        PropertyDefinition[] propDefs = type.getDeclaredPropertyDefinitions();

        for (PropertyDefinition def : propDefs) {
            String propName = def.getName();
            GenericType.PropertyType propType = asType(def.getRequiredType());
            typeMap.put(propName, propType);
        }

        return typeMap;
    }

    public static GenericType.PropertyType getPropertyType(NodeType type, String name) {
        PropertyDefinition[] propDefs = type.getDeclaredPropertyDefinitions();

        for (PropertyDefinition def : propDefs) {
            String propName = def.getName();

            if (propName.equalsIgnoreCase(name)) {
                return asType(def.getRequiredType());
            }
        }

        // Not found
        throw new UnknownPropertyException(name);
    }

    /**
     * Return the nodes Super Type properties, own properties, and referencing node Entities
     */
    public static Map<String, GenericType.PropertyType> getAllPropertyTypes(NodeType type) {
        Map<String, GenericType.PropertyType> typeMap = new HashMap<>();
        String thisTypeName = type.getName();

        //Add the Super Types
        NodeType[] superTypes = type.getDeclaredSupertypes();
        if (superTypes != null) {
            for (NodeType superType : superTypes) {
                if (!typeMap.containsKey(superType.getName()) && !thisTypeName.equalsIgnoreCase(superType.getName())) {
                    typeMap.putAll(getAllPropertyTypes(superType));
                }
            }
        }
        //add this nodes properties
        typeMap.putAll(getPropertyTypes(type));

        //Add the child Node Entities
        NodeDefinition[] childNodes = type.getChildNodeDefinitions();

        if (childNodes != null) {
            for (NodeDefinition childNode : childNodes) {
                NodeType[] childTypes = childNode.getRequiredPrimaryTypes();

                if (childTypes != null) {
                    for (NodeType childType : childTypes) {
                        if (!typeMap.containsKey(childType.getName()) && !thisTypeName.equalsIgnoreCase(childType.getName())) {
                            typeMap.put(childType.getName(), GenericType.PropertyType.ENTITY);
                        }
                    }
                }
            }
        }
        return typeMap;
    }

    public static GenericType.PropertyType asType(Property prop) {
        // STRING, BOOLEAN, LONG, DOUBLE, PATH, ENTITY
        try {
            int code = prop.getType();

            return asType(code);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property type", e);
        }
    }

    public static GenericType.PropertyType asType(int code) {
        // STRING, BOOLEAN, LONG, DOUBLE, PATH, ENTITY
        if (code == PropertyType.BOOLEAN) {
            return GenericType.PropertyType.BOOLEAN;
        } else if (code == PropertyType.STRING) {
            return GenericType.PropertyType.STRING;
        } else if (code == PropertyType.LONG) {
            return GenericType.PropertyType.LONG;
        } else if (code == PropertyType.DOUBLE) {
            return GenericType.PropertyType.DOUBLE;
        } else if (code == PropertyType.PATH) {
            return GenericType.PropertyType.PATH;
        } else if (code == PropertyType.REFERENCE) {
//                return prop.get
            return GenericType.PropertyType.ENTITY;  // TODO look up relationship
        } else {
            // Use string by default
            return GenericType.PropertyType.STRING;
        }
    }

    public static Object asValue(Property prop) {
        // STRING, BOOLEAN, LONG, DOUBLE, PATH, ENTITY
        try {
            int code = prop.getType();

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
//                return prop.get
                return null;  // TODO look up relationship
            } else {
                return prop.getString();
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access property type", e);
        }
    }

    public static void setProperty(Node node, String name, Object value) {
        // Letting the value be converted from string.
        // TODO do proper conversion
        try {
            node.setProperty(name, value.toString());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set property value: " + name + "=" + value, e);
        }

    }

    public static int asCode(GenericType.PropertyType type) {
        switch (type) {
            case BOOLEAN:
                return PropertyType.BOOLEAN;
            case DOUBLE:
                return PropertyType.DOUBLE;
            case INTEGER:
                return PropertyType.LONG;
            case LONG:
                return PropertyType.LONG;
            case STRING:
                return PropertyType.STRING;
            case PATH:
                return PropertyType.PATH;
            case ENTITY:
                return PropertyType.REFERENCE;
            default:
                return PropertyType.STRING;
        }
    }

    public static Value asValue(ValueFactory factory, Object obj) {
        // STRING, BOOLEAN, LONG, DOUBLE, PATH, ENTITY
        try {
            if (obj instanceof String) {
                return factory.createValue((String) obj);
            } else if (obj instanceof Integer || obj instanceof Long) {
                return factory.createValue(obj.toString(), PropertyType.LONG);
            } else if (obj instanceof Float || obj instanceof Double) {
                return factory.createValue(obj.toString(), PropertyType.DOUBLE);
//        } else if (obj instanceof GenericEntity) {
//            return factory.createValue((String) obj);
            } else {
                return factory.createValue(obj.toString());
            }
        } catch (ValueFormatException e) {
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

    public static <T extends JcrObject> T getOrCreateNode(Node parentNode, String name, String nodeType, Class<T> type) {
        return getOrCreateNode(parentNode, name, nodeType, type, null);
    }

    /**
     * gets a node cast as a type
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

    public static <T extends JcrObject> T createJcrObject(Node node, Class<T> type, Object[] constructorArgs) {
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


}
