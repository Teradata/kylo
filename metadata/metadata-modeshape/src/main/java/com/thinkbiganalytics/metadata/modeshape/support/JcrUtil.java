/**
 *
 */
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

import com.thinkbiganalytics.classnameregistry.ClassNameChangeRegistry;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.modeshape.jcr.api.JcrTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;

/**
 * Utility and convenience methods for accessing and manipulating nodes in the JCR API.  Some methods are duplicates of their JCR equivalents but do not throw the non-runtime RepositoryException.
 */
public class JcrUtil {

    private static final Logger log = LoggerFactory.getLogger(JcrUtil.class);

    /**
     * Creates a Path out of the arguments appropriate for JCR.
     *
     * @param first the first element
     * @param more  any remaining elements
     * @return a path string
     */
    public static Path path(String first, String... more) {
        return JcrPath.get(first, more);
    }

    /**
     * Creates a path out of the arguments appropriate for JCR.
     *
     * @param parent   the parent node on whose path will be appended the additional elements
     * @param elements the remaining elements to form the path
     * @return a path string
     */
    public static Path path(Node parent, String... elements) {
        try {
            return JcrPath.get(parent.getPath(), elements);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get the path of the node: " + parent, e);
        }
    }
    
    public static boolean isRoot(Node node) {
        try {
            return node.getSession().getRootNode().getIdentifier().equals(node.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to check if root node: " + node, e);
        }
    }

    /**
     * Checks whether the given mixin node type is in effect for the given node.
     *
     * @param node      the node
     * @param mixinType the mixin node type
     * @return <code>true</code> when the mixin node type is present, <code>false</code> instead.
     */
    public static boolean hasMixinType(Node node, String mixinType) throws RepositoryException {

        for (NodeType nodeType : node.getMixinNodeTypes()) {
            if (nodeType.getName().equals(mixinType)) {
                return true;
            }
        }
        NodeType[] types = node.getPrimaryNodeType().getSupertypes();
        if (types != null) {
            for (NodeType nt : types) {
                if (nt.getName().equals(mixinType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isVersionable(JcrObject jcrObject) {
        return JcrVersionUtil.isVersionable(jcrObject.getNode());
    }

    public static String getName(Node node) {
        try {
            return node.getName();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get name of Node " + node, e);
        }
    }

    public static String getPath(Node node) {
        try {
            return node.getPath();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get the Path", e);
        }
    }

    public static boolean isNodeType(Node node, String typeName) {
        try {
            return node.getPrimaryNodeType().isNodeType(typeName);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the type of node: " + node, e);
        }
    }

    public static Node getParent(Node node) {
        try {
            return node.getParent();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the parent of node: " + node, e);
        }
    }

    /**
     * Gets the nodes in a same-name-sibling node set with the given name and returns them as a list.
     */
    public static List<Node> getNodeList(Node parent, String name) {
        return StreamSupport
            .stream(getIterableChildren(parent, name).spliterator(), false)
            .collect(Collectors.toList());
    }

    public static Node getRootNode(Session session) {
        try {
            return session.getRootNode();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the root node", e);
        }
    }

    public static boolean hasNode(Session session, String absPath) {
        try {
            if (absPath.startsWith("/")) {
                session.getNode(absPath);
                return true;
            } else {
                return session.getRootNode().hasNode(absPath);
            }
        } catch (PathNotFoundException e) {
            return false;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to check for the existence of the node at path " + absPath, e);
        }
    }

    public static boolean hasNode(Session session, String absParentPath, String name) {
        Node parentNode = getNode(session, absParentPath);
        return hasNode(parentNode, name);
    }

    public static boolean hasNode(Node parentNode, String name) {
        try {
            return parentNode.hasNode(name);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to check for the existence of the node named " + name, e);
        }
    }

    public static Node getNode(Session session, String absPath) {
        try {
            if (absPath.startsWith("/")) {
                return session.getNode(absPath);
            } else {
                return session.getRootNode().getNode(absPath);
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node at the path: " + absPath, e);
        }
    }

    public static Node getNode(Node parentNode, String name) {
        try {
            return parentNode.getNode(name);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named " + name, e);
        }
    }

    public static Node createNode(Node parentNode, String name, String nodeType) {
        try {
            return parentNode.addNode(name, nodeType);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create the Node named " + name, e);
        }
    }

    public static void removeNode(Node node) {
        try {
            node.remove();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove the node " + node, e);
        }
    }

    public static boolean removeNode(Node parentNode, String name) {
        try {
            if (parentNode.hasNode(name)) {
                parentNode.getNode(name).remove();
                return true;
            } else {
                return false;
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove the Node named " + name, e);
        }
    }
    
    public static boolean hasNodeOfType(Node parentNode, String nodeType) {
        return StreamSupport.stream(getIterableChildren(parentNode).spliterator(), false).anyMatch(node -> isNodeType(node, nodeType));
    }

    public static List<Node> getNodesOfType(Node parentNode, String nodeType) {
        try {
            List<Node> list = new ArrayList<>();
            NodeIterator itr = parentNode.getNodes();

            while (itr.hasNext()) {
                Node node = (Node) itr.next();
                if (node.isNodeType(nodeType)) {
                    list.add(node);
                }
            }

            return list;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create set of child nodes of type: " + nodeType, e);
        }
    }

    public static Iterable<Node> getIterableChildren(Node parent) {
        return getIterableChildren(parent, null);
    }

    public static Iterable<Node> getIterableChildren(Node parent, String name) {
        @SuppressWarnings("unchecked")
        Iterable<Node> itr = () -> {
            try {
                return name != null ? parent.getNodes(name) : parent.getNodes();
            } catch (AccessDeniedException e) {
                log.debug("Access denied", e);
                throw new AccessControlException(e.getMessage());
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to retrieve the child nodes from:  " + parent, e);
            }
        };
        return itr;
    }


    public static <T extends JcrObject> List<T> getChildrenMatchingNodeType(Node parentNode, String childNodeType, Class<T> type, Object... args) {

        try {
            String
                query =
                "SELECT child.* from [" + parentNode.getPrimaryNodeType() + "] as parent inner join [" + childNodeType + "] as child ON ISCHILDNODE(child,parent) WHERE parent.[mode:id]  = '"
                + parentNode.getIdentifier() + "'";
            return JcrQueryUtil.find(parentNode.getSession(), query, type, args);

        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Children matching type " + childNodeType, e);
        }

    }

    public static <T extends JcrObject> T toJcrObject(Node node, String nodeType, Class<T> type) {
        return toJcrObject(node, nodeType, new DefaultObjectTypeResolver<T>(type));
    }

    public static <T extends JcrObject> T toJcrObject(Node node, String nodeType, JcrObjectTypeResolver<T> typeResolver, Object... args) {
        try {
            if (nodeType == null || node.isNodeType(nodeType)) {
                T entity = constructNodeObject(node, typeResolver.resolve(node), args);
                return entity;
            } else {
                throw new MetadataRepositoryException("Unable to instanciate object of node type: " + nodeType);
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to instanciate object from node: " + node, e);
        }
    }

    /**
     * get All Child nodes under a parentNode and create the wrapped JCRObject.
     */
    public static <T extends JcrObject> List<T> getJcrObjects(Node parentNode, Class<T> type) {
        return getJcrObjects(parentNode, null, new DefaultObjectTypeResolver<T>(type));
    }

    /**
     * get All Child nodes under a parentNode matching the name and type, and create the wrapped JCRObject the second argument, name, can be null to get all the nodes under the parent
     */
    public static <T extends JcrObject> List<T> getJcrObjects(Node parentNode, String name, Class<T> type) {
        return getJcrObjects(parentNode, name, null, new DefaultObjectTypeResolver<T>(type));
    }

    /**
     * get All Child nodes under a parentNode matching the type and create the wrapped JCRObject the second argument, name, can be null to get all the nodes under the parent
     */
    public static <T extends JcrObject> List<T> getJcrObjects(Node parentNode, NodeType nodeType, Class<T> type) {
        return getJcrObjects(parentNode, nodeType, new DefaultObjectTypeResolver<T>(type));
    }

    /**
     * get All Child nodes under a parentNode matching the name and type, returning a wrapped JCRObject the second argument, name, can be null to get all the nodes under the parent
     */
    public static <T extends JcrObject> List<T> getJcrObjects(Node parentNode, String name, NodeType nodeType, Class<T> type) {
        return getJcrObjects(parentNode, name, nodeType, new DefaultObjectTypeResolver<T>(type));
    }

    /**
     * get All Child nodes under a parentNode and create the wrapped JCRObject.
     */
    public static <T extends JcrObject> List<T> getJcrObjects(Node parentNode, JcrObjectTypeResolver<T> typeResolver) {
        return getJcrObjects(parentNode, null, null, typeResolver);
    }

    /**
     * get All Child nodes under a parentNode of a certain type and create the wrapped JCRObject.
     */
    public static <T extends JcrObject> List<T> getJcrObjects(Node parentNode, NodeType nodeType, JcrObjectTypeResolver<T> typeResolver) {
        return getJcrObjects(parentNode, null, nodeType, typeResolver);
    }

    /**
     * get All Child nodes under a parentNode and create the wrapped JCRObject the second argument, name, can be null to get all the nodes under the parent
     */
    public static <T extends JcrObject> List<T> getJcrObjects(Node parentNode, String name, NodeType nodeType, JcrObjectTypeResolver<T> typeResolver, Object... args) {
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

                    if (nodeType == null || n.isNodeType(nodeType.getName())) {
                        T entity = constructNodeObject(n, typeResolver.resolve(n), args);
                        list.add(entity);
                    }
                }
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
        return list;
    }

    /**
     * Get a creates a Wrapper object around the given node
     */
    public static <T extends JcrObject> T getJcrObject(Node node, Class<T> type, Object... args) {
        return constructNodeObject(node, type, args);
    }

    /**
     * Get a child node relative to the parentNode and create the Wrapper object
     */
    public static <T extends JcrObject> T getJcrObject(Node parentNode, String name, Class<T> type, Object... args) {
        try {
            Node n = parentNode.getNode(name);
            return getJcrObject(n, type, args);
        } catch (PathNotFoundException e) {
            return null;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
    }

    /**
     * Get or Create a node relative to the Parent Node.
     */
    public static Node getOrCreateNode(Node parentNode, String name, String nodeType) {
        try {
            if (parentNode.hasNode(name)) {
                return parentNode.getNode(name);
            } else {
                return addNode(parentNode, name, nodeType);
            }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
    }


    public static Node addNode(Node parentNode, String name, String nodeType) {
        try {
            return parentNode.addNode(name, nodeType);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
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
    public static <T extends JcrObject> T getOrCreateNode(Node parentNode, String name, String nodeType, Class<T> type, Object... constructorArgs) {
        T entity = null;
        try {
            JcrTools tools = new JcrTools();

            //if versionable checkout
            //   if(isVersionable(parentNode)){
            //     JcrVersionUtil.checkout(parentNode);
            //  }
            Node n = tools.findOrCreateChild(parentNode, name, nodeType);
            entity = createJcrObject(n, type, constructorArgs);
            //save ??
            //   JcrVersionUtil.checkinRecursively(n);
            //  if(isVersionable(parentNode)){
            //       JcrVersionUtil.checkin(parentNode);
            //    }
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
        return entity;
    }

    public static <T extends Serializable> T getGenericJson(Node parent, String nodeName) {
        return getGenericJson(parent, nodeName, false);
    }

    public static <T extends Serializable> T getGenericJson(Node parent, String nodeName, boolean allowClassNotFound) {
        try {
            Node jsonNode = parent.getNode(nodeName);

            return getGenericJson(jsonNode, allowClassNotFound);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to deserialize generic JSON node", e);
        }
    }


    public static <T extends Serializable> T getGenericJson(Node jsonNode) {
        return getGenericJson(jsonNode, false);
    }


    public static <T extends Serializable> T getGenericJson(Node jsonNode, boolean allowClassNotFound) {
        try {
            String className = jsonNode.getProperty("tba:type").getString();
            @SuppressWarnings("unchecked")
            Class<T> type = (Class<T>) ClassNameChangeRegistry.findClass(className);

            return JcrPropertyUtil.getJsonObject(jsonNode, "tba:json", type);
        } catch (RepositoryException | ClassNotFoundException | ClassCastException e) {
            if (e instanceof ClassNotFoundException && allowClassNotFound) {
                //swallow this exception
                return null;
            } else {
                throw new MetadataRepositoryException("Failed to deserialize generic JSON property", e);
            }
        }
    }


    public static <T extends Serializable> void addGenericJson(Node parent, String nodeName, T object) {
        final Node jsonNode = getOrCreateNode(parent, nodeName, "tba:genericJson");
        JcrPropertyUtil.setProperty(jsonNode, "tba:type", object.getClass().getName());
        JcrPropertyUtil.setJsonObject(jsonNode, "tba:json", object);
    }

    /**
     * Create a new JcrObject (Wrapper Object) that invokes a constructor with at least parameter of type Node
     */
    public static <T extends JcrObject> T addJcrObject(Node parent, String name, String nodeType, Class<T> type) {
        return addJcrObject(parent, name, nodeType, type, new Object[0]);
    }

    /**
     * Create a new JcrObject (Wrapper Object) that invokes a constructor with at least parameter of type Node
     */
    public static <T extends JcrObject> T addJcrObject(Node parent, String name, String nodeType, Class<T> type, Object... constructorArgs) {
        try {
            Node child = parent.addNode(name, nodeType);
            return createJcrObject(child, type, constructorArgs);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add new createJcrObject child node " + type, e);
        }
    }

    /**
     * Create a new JcrObject (Wrapper Object) that invokes a constructor with at least parameter of type Node
     */
    public static <T extends JcrObject> T createJcrObject(Node node, Class<T> type) {
        return createJcrObject(node, type, new Object[0]);
    }

    public static Map<String, Object> jcrObjectAsMap(JcrObject obj) {
        String nodeName = obj.getNodeName();
        String path = obj.getPath();
        String identifier = null;
        try {
            identifier = obj.getObjectId();
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        String type = obj.getTypeName();
        Map<String, Object> props = obj.getProperties();
        Map<String, Object> finalProps = new HashMap<>();
        if (props != null) {
            finalProps.putAll(finalProps);
        }
        finalProps.put("nodeName", nodeName);
        if (identifier != null) {
            finalProps.put("nodeIdentifier", identifier);
        }
        finalProps.put("nodePath", path);
        finalProps.put("nodeType", type);
        return finalProps;
    }

    public static Map<String, Object> nodeAsMap(Node obj) throws RepositoryException {

        try {
            String nodeName = obj.getName();
            String path = obj.getPath();
            String identifier = obj.getIdentifier();

            String type = obj.getPrimaryNodeType() != null ? obj.getPrimaryNodeType().getName() : "";
            Map<String, Object> props = JcrPropertyUtil.getProperties(obj);
            Map<String, Object> finalProps = new HashMap<>();
            if (props != null) {
                finalProps.putAll(finalProps);
            }
            finalProps.put("nodeName", nodeName);
            if (identifier != null) {
                finalProps.put("nodeIdentifier", identifier);
            }
            finalProps.put("nodePath", path);
            finalProps.put("nodeType", type);
            return finalProps;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        }
    }

    /**
     * Create a new JcrObject (Wrapper Object) that invokes a constructor with at least parameter of type Node
     */
    public static <T extends JcrObject> T createJcrObject(Node node, Class<T> type, Object... constructorArgs) {
        T obj = constructNodeObject(node, type, constructorArgs);
        // TODO Removed since no nodes currently are versionable (Feed versioning removed)
//        if(JcrUtil.isVersionable(obj) && !node.isNew()){
//            try {
//                String versionName = JcrVersionUtil.getBaseVersion(node).getName();
//                obj.setVersionName(versionName);
//                obj.setVersionableIdentifier(JcrVersionUtil.getBaseVersion(node).getContainingHistory().getVersionableIdentifier());
//            } catch (RepositoryException e) {
//              //this is fine... versionName is a nice to have on the object
//            }
//        }
        return obj;
    }

    /**
     * Create a new Node Wrapper Object that invokes a constructor with at least parameter of type Node
     */
    public static <T extends Object> T constructNodeObject(Node node, Class<T> type, Object... constructorArgs) {
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

    public static <T extends JcrObject> T getReferencedObject(Node node, String property, Class<T> type) {
        return getReferencedObject(node, property, new DefaultObjectTypeResolver<T>(type));
    }

    public static <T extends JcrObject> T getReferencedObject(Node node, String property, JcrObjectTypeResolver<T> typeResolver) {
        try {
            Property prop = node.getProperty(property);
            return createJcrObject(prop.getNode(), typeResolver.resolve(prop.getNode()));
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to dereference object of type using: " + typeResolver, e);
        }

    }

    /**
     * Creates an object set from the nodes of a same-name sibling property
     */
    public static <T extends JcrObject> Set<T> getPropertyObjectSet(Node parentNode, String property, Class<T> objClass, Object... args) {
        try {
            Set<T> set = new HashSet<>();
            NodeIterator itr = parentNode.getNodes(property);
            while (itr.hasNext()) {
                Node objNode = (Node) itr.next();
                T obj = constructNodeObject(objNode, objClass, args);
                set.add(obj);
            }
            return set;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create set of child objects from property: " + property, e);
        }
    }

    public static NodeType getNodeType(Session session, String typeName) {
        try {
            return session.getWorkspace().getNodeTypeManager().getNodeType(typeName);
        } catch (NoSuchNodeTypeException e) {
            throw new MetadataRepositoryException("No node type exits named: " + typeName, e);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve node type named: " + typeName, e);
        }
    }


    public static Node copy(Session session, String srcPath, String destPath) {
        try {
            session.getWorkspace().copy(srcPath, destPath);
            return session.getNode(destPath);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to copy source path: " + srcPath + " to destination path: " + destPath, e);
        }
    }

    public static Node copy(Node srcNode, Node destNode) {
        try {
            Session sess = srcNode.getSession();
            return copy(sess, srcNode.getPath(), destNode.getPath());
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to copy source node: " + srcNode + " to destination node: " + destNode, e);
        }
    }

    public static Node copy(Node srcNode, String destPath) {
        try {
            Session sess = srcNode.getSession();
            return copy(sess, srcNode.getPath(), destPath);
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to copy source node: " + srcNode + " to destination path: " + destPath, e);
        }
    }


    private static class DefaultObjectTypeResolver<T extends JcrObject> implements JcrObjectTypeResolver<T> {

        private final Class<? extends T> type;

        public DefaultObjectTypeResolver(Class<? extends T> type) {
            super();
            this.type = type;
        }

        @Override
        public Class<? extends T> resolve(Node node) {
            return this.type;
        }
    }

}
