/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.support;

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.modeshape.jcr.api.JcrTools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

/**
 * @author Sean Felten
 */
public class JcrUtil {




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
        return isVersionable(jcrObject.getNode());
    }

    public static boolean isVersionable(Node node) {
        String name = "";
        boolean versionable = false;
        try {
            name = node.getName();
            versionable = hasMixinType(node, "mix:versionable");
            return versionable;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to check if versionable for Node " + name, e);
        }
    }


    public static Node getNode(Node parentNode, String name) {
        try {
            return parentNode.getNode(name);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Node named" + name, e);
        }
    }

    public static <T extends JcrObject> List<T> getChildrenMatchingNodeType(Node parentNode, String childNodeType, Class<T> type) {

        try {
            String
                query =
                "SELECT child.* from [" + parentNode.getPrimaryNodeType() + "] as parent inner join [" + childNodeType + "] as child ON ISCHILDNODE(child,parent) WHERE parent.[jcr:uuid]  = '"
                + parentNode.getIdentifier() + "'";
            return JcrQueryUtil.find(parentNode.getSession(), query, type);

        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Children matching type " + childNodeType, e);
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
        T obj = constructNodeObject(node, type, constructorArgs);
        if(JcrUtil.isVersionable(obj) && !node.isNew()){
            try {
                String versionName = JcrVersionUtil.getBaseVersion(node).getName();
                obj.setVersionName(versionName);
                obj.setVersionableIdentifier(JcrVersionUtil.getBaseVersion(node).getContainingHistory().getVersionableIdentifier());
            } catch (RepositoryException e) {
              //this is fine... versionName is a nice to have on the object
            }
        }
        return obj;
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

}
