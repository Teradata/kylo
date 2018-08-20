/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.support;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.springframework.security.authentication.LockedException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;

/**
 *
 */
public class JcrLockingUtil {
    
    
    /**
     * Generates a proxy instance around the supplied node to perform automatic session-scoped locking of the node,
     * or one of its parents, is lockable.  The proxy, when any attempt is made to modify it in some
     * way, will first attempt to acquire a lock of its underlying node, or one of its parents, before performing 
     * the update.
     * <p>
     * Providing a node that is not lockable, and none of its parents are lockable,
     * results in the same node (un-proxied) being returned.
     * @param node a node with a lockable hierarchy
     * @param isDeep whether to lock deeply, i.e. whether all child nodes should be locked as well
     * @return a proxy for the node that automatically performs locking
     */
    public static Node createAutoLockProxy(Node node, boolean isDeep) {
        if (findLockable(node).isPresent()) {
            Class<?>[] types = new Class<?>[] { Node.class };
            InvocationHandler handler = new LockableNodeInvocationHandler(node, types, isDeep);
            return (Node) Proxy.newProxyInstance(Node.class.getClassLoader(), types, handler);
        } else {
            throw new IllegalArgumentException("The supplied node is not in a lockable hierarch: " + node);
        }
    }

    /**
     * Places a session-scoped lock on the given node, with no timeout, for the current user.
     *
     * @param node            the node to be locked
     * @param isDeep          if <code>true</code> this lock will apply to this node and all its descendants; if <code>false</code>, it applies only to this node.
     * @see javax.jcr.lock.LockManager#lock(String, boolean, boolean, long, String)
     */
    public static Lock lock(Node node, boolean isDeep) {
        try {
            UsernamePrincipal user = JcrMetadataAccess.getActiveUser();
            Session session = node.getSession();
            LockManager mgr = session.getWorkspace().getLockManager();
            
            session.save();
            return findLockable(node)
                    .map(n -> lock(mgr, n, isDeep, user))
                    .orElseThrow(() -> new MetadataRepositoryException("No lockable node in hierarchy of: " + node));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not obtain lock for " + node, e);
        }
    }
    
    /**
     * Removes the lock on the given node.
     *
     * @param node the node unlock
     * @see javax.jcr.lock.LockManager#unlock(String)
     */
    public static void unlock(Node node) {
        try {
            LockManager mgr = node.getSession().getWorkspace().getLockManager();
            mgr.unlock(node.getPath());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not perform unlock", e);
        }
    }

    /**
     * Finds the first node in the supplied node's hierarchy that is lockable.  The search
     * starts with the given node and work through its parentage until a "mix:lockable" node
     * is found or the root not is reached.
     * @param node the starting node
     * @return an Optional containing the first lockable node found or empty none are found.
     */
    public static Optional<Node> findLockable(Node node) {
        if (JcrUtil.getRootNode(node).equals(node)) {
            return Optional.empty();
        } else if (isLockable(node)) {
            return Optional.of(node);
        } else {
            return findLockable(JcrUtil.getParent(node));
        }
    }
    
    public static boolean isLockable(Node node) {
        try {
            return Arrays.stream(node.getPrimaryNodeType().getDeclaredSupertypes())
                    .filter(type -> type.isNodeType("mix:lockable"))
                    .findAny()
                    .isPresent();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not determine node types of " + node, e);
        }
    }
    
    private static Lock lock(LockManager mgr, Node node, boolean isDeep, UsernamePrincipal user) {
        try {
            String path = node.getPath();
            
            try {
                Lock lock = mgr.getLock(path);
                
                if (lock.isLockOwningSession()) {
                    return lock;
                } else {
                    throw new LockException("The node is locked by another session: " + path);
                }
            } catch (LockException e) {
                return mgr.lock(node.getPath(), isDeep, true, Long.MAX_VALUE, user.getName());
            }
        } catch (LockException e) {
            throw new MetadataLockException(e);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not obtain lock for " + node, e);
        }
    }

    private JcrLockingUtil() { }
}
