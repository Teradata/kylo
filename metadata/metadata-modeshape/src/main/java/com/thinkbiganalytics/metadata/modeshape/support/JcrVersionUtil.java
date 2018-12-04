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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import org.modeshape.jcr.JcrLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class JcrVersionUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JcrVersionUtil.class);
    
    public static Node createAutoCheckoutProxy(Node node, boolean autoCheckin) {
        Class<?>[] types = new Class<?>[] { Node.class };
        InvocationHandler handler = new VersionableNodeInvocationHandler(node, types, autoCheckin);
        return (Node) Proxy.newProxyInstance(Node.class.getClassLoader(), types, handler);
    }

    /**
     * Returns the LockManager object from the given session.
     *
     * @param session {@link Session}
     * @return a {@link LockManager} object
     */
    public static LockManager getLockManager(Session session) throws RepositoryException {
        LockManager lockMgr = session.getWorkspace().getLockManager();
        return lockMgr;
    }

    /**
     * Returns the VersionManager object from the given session.
     *
     * @param session {@link Session}
     * @return a {@link VersionManager} object
     */
    public static VersionManager getVersionManager(Session session) throws RepositoryException {
        VersionManager versionMgr = session.getWorkspace().getVersionManager();
        return versionMgr;
    }
    
    public static boolean isCheckedOut(Node node) {
        try {
            return node.isCheckedOut();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to check if the node is checked out", e);
        }
    }
    
    /**
     * Ensures that the given node, or a versionable ancestor, is checked out with auto-checkin set.
     * Passing a node that is not in a versionable hierarch has no effect.
     * @param n the node to whose hierarchy is to be checked out
     */
    public static void ensureCheckoutNode(Node node) {
        ensureCheckoutNode(node, true);
    }
    
    /**
     * Ensures that the given node, or a versionable ancestor, is checked out, and
     * specify whether the node should be auto-checked in on commit.
     * Passing a node that is not in a versionable hierarch has no effect.
     * @param n the node to whose hierarchy is to be checked out
     */
    public static void ensureCheckoutNode(Node node, boolean autoCheckin) {
        getVersionableAncestor(node).ifPresent(a -> JcrMetadataAccess.ensureCheckoutNode(a, autoCheckin));
    }
    
    /**
     * Ensures that the given node, or a versionable ancestor, is set to automatically
     * be checked in on commit.  If the node's hierarchy is is not versionable or has not
     * been checked then no action is taken.
     * @param node the node to whose hierarchy was checked out
     */
    public static void ensureAutoCheckin(Node node) {
        getVersionableAncestor(node)
            .filter(versionable -> ! JcrMetadataAccess.hasCheckedOutNode(versionable))
            .ifPresent(versionable -> JcrMetadataAccess.ensureCheckoutNode(versionable, true));
    }
    
    /**
     * Finds the first node in the argument node's parentage that is versionable, if any.  If the argument 
     * node itself is versionable then an Optional containing it is returned. If there is no versionable
     * node in this node's hierarchy then an empty Optional is returned.
     * @param node the node who's parentage is to be searched
     * @return An Optional containing the closest versionable parent of the node if it exists, otherwise an empty Optional
     */
    public static Optional<Node> getVersionableAncestor(Node node) {
        try {
            if (node.getSession().getRootNode().getIdentifier().equals(node.getParent().getIdentifier())) {
                return Optional.empty();
            } else if (isVersionable(node)) {
                return Optional.of(node);
            } else {
                return getVersionableAncestor(node.getParent());
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to check if node is in versionable hierarchy", e);
        }
    }
    
    /**
     * Checks if the given node is within a versionable hierarchy.
     * @param node he node who's parentage is to be searched
     * @return true if this node or one of its parents is versionable, otherwise false
     */
    public static boolean inVersionableHierarchy(Node node) {
        return getVersionableAncestor(node).map(n -> true).orElse(false);
    }

    /**
     * @param node the node to test
     * @return true if this node is versionable
     */
    public static boolean isVersionable(Node node) {
        String name = "";
        boolean versionable = false;
        try {
            name = node.getName();
            versionable = JcrUtil.hasMixinType(node, "mix:versionable");
            return versionable;
        } catch (AccessDeniedException e) {
            log.debug("Access denied", e);
            throw new AccessControlException(e.getMessage());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to check if versionable for Node " + name, e);
        }
    }

    /**
     * Sets the given node to checked-out status.
     *
     * @param node node to check-out
     */
    public static void checkout(Node node) {
        try {
            getVersionManager(node.getSession()).checkout(node.getPath());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to checkout the node: " + node, e);
        }
    }


    /**
     * Saves the current session an then creates and returns a new vesion of the node.
     *
     * @param node node to checkin
     * @return the created version
     */
    public static Version checkin(Node node) {
        try {
            node.getSession().save();
            return doCheckin(node);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to checkout the node: " + node, e);
        }
    }
    
    public static void checkinRecursively(Node node) {
        try {
            node.getSession().save();

            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                checkinRecursively(it.nextNode());
            }
            if (node.isCheckedOut() && node.isNodeType(NodeType.MIX_VERSIONABLE)) {
                doCheckin(node);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not perform check-in", e);
        }
    }

    public static void checkoutRecursively(Node node) {
        try {
            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                checkoutRecursively(it.nextNode());
            }
            if (!node.isCheckedOut() && node.isNodeType(NodeType.MIX_VERSIONABLE)) {
                checkout(node);
            }

        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not perform check-out", e);
        }
    }
    
    public static void restore(Version version) {
        try {
            getVersionManager(version.getSession()).restore(version, true);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to restore version: " + version, e);
        }
    }


    public static Version getBaseVersion(Node node) {
        String nodeName = null;
        if (! isVersionable(node)) {
            return null;
        }
        try {
            nodeName = node.getName();
            return JcrVersionUtil.getVersionManager(node.getSession()).getBaseVersion(node.getPath());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Base Version for " + nodeName, e);
        }
    }


    public static List<Version> getVersions(Node node) {
        String nodeName = null;
        if (! isVersionable(node)) {
            return null;
        }
        try {
            nodeName = node.getName();
            List<Version> versions = new ArrayList<>();
            VersionHistory history = JcrVersionUtil.getVersionManager(node.getSession()).getVersionHistory(node.getPath());
            VersionIterator itr = history.getAllVersions();

            while (itr.hasNext()) {
                Version version = itr.nextVersion();
                versions.add(version);
            }
            return versions;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Version History for " + nodeName, e);
        }

    }

    public static <T extends JcrObject> T getVersionedNode(Version version, Class<T> type, Object[] constructorArgs) {
        String nodeName = null;
        String versionName = null;

        try {
            versionName = version.getName();
            Node node = version.getFrozenNode();
            nodeName = node.getName();
            String entityId = version.getContainingHistory().getVersionableIdentifier();
            T obj = JcrUtil.constructNodeObject(node, type, constructorArgs);
            obj.setVersionName(versionName);
            obj.setVersionableIdentifier(entityId);
            return obj;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Version " + versionName + " for Node " + nodeName, e);
        }
    }

    public static Version findVersion(Node node, final String versionName) {
        if (! isVersionable(node)) {
            return null;
        }
        Version version = Iterables.tryFind(getVersions(node), new Predicate<Version>() {
            @Override
            public boolean apply(Version version) {
                try {
                    String identifier = node.getIdentifier();
                    String frozenIdentifer = JcrPropertyUtil.getString(version.getFrozenNode(), "jcr:frozenUuid");
                    return version.getName().equalsIgnoreCase(versionName) && frozenIdentifer.equalsIgnoreCase(identifier);
                } catch (RepositoryException e) {

                }
                return false;
            }
        }).orNull();
        return version;
    }


    public static <T extends JcrObject> T getVersionedNode(Version version, Class<T> type) {
        return getVersionedNode(version, type, null);
    }

    public static <T extends JcrObject> T getVersionedNode(JcrObject node, String versionNumber, Class<T> type) {
        Version version = findVersion(node.getNode(), versionNumber);
        if (version != null) {
            return getVersionedNode(version, type);
        }
        throw new MetadataRepositoryException("Unable to find Version " + versionNumber + " for Node " + node.getNodeName());
    }

    public static Node getFrozenNode(Version version) {
        try {
            return version.getFrozenNode();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get frozen node of Version: " + version, e);
        }
    }

    /**
     * Performs a checkin on the node.
     */
    private static Version doCheckin(Node node) {
        try {
            // Checking if the checked-out property exists fixes a ModeShape bug where this property is not checked
            // if it is null on check-in as the node.isCheckedOut() method does.  This can be null
            // when the node being checked-in has not been saved after being just made versionable; which 
            // can occur during upgrade to 0.8.4.
            if (node.isCheckedOut() && JcrPropertyUtil.hasProperty(node, JcrLexicon.IS_CHECKED_OUT.getString())) {
                return getVersionManager(node.getSession()).checkin(node.getPath());
            } else {
                return null;
            }
        } catch (ItemNotFoundException e) {
            // Ignore if the node being checked in represents a deleted node.
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to checkout the node: " + node, e);
        }
    }


}
