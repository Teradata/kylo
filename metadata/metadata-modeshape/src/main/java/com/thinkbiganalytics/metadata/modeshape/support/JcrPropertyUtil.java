package com.thinkbiganalytics.metadata.modeshape.support;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.UnknownPropertyException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Binary;
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

    static MetadataAccess metadataAccess;


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
                ((JcrMetadataAccess) metadataAccess).checkoutNode(entNode);
                for (Map.Entry<String, Object> entry : props.entrySet()) {
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
                    return new DateTime(value.getDate().getTime());
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
        //ensure checked out
        JcrMetadataAccess jcrMetadataAccess = (JcrMetadataAccess) metadataAccess;

        try {
            jcrMetadataAccess.checkoutNode(node);

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
        //save it

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

    public static void setMetadataAccess(MetadataAccess metadataAccess) {
        JcrPropertyUtil.metadataAccess = metadataAccess;
    }
}
