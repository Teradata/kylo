package com.thinkbiganalytics.metadata.modeshape.security;

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

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;

import org.modeshape.jcr.security.SimplePrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;

/**
 * Utilities to apply JCR access control changes to nodes and node hierarchies.
 */
public final class JcrAccessControlUtil {

    private static boolean enableEntityAccessControl;

    private JcrAccessControlUtil() {
        throw new AssertionError(JcrAccessControlUtil.class + " is a static utility class");
    }

    public Optional<UsernamePrincipal> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return Optional.of(new UsernamePrincipal(auth.getName()));
        } else {
            return Optional.empty();
        }
    }

    public static boolean addPermissions(Node node, Principal principal, String... privilegeNames) {
        try {
            return addPermissions(node.getSession(), node.getPath(), principal, privilegeNames);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + node + ": "
                                                  + Arrays.toString(privilegeNames), e);
        }
    }

    public static boolean addPermissions(Node node, Principal principal, Privilege... privileges) {
        try {
            return addPermissions(node.getSession(), node.getPath(), principal, privileges);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + node + ": "
                                                  + Arrays.toString(privileges), e);
        }

    }

    public static boolean addPermissions(Session session, String path, Principal principal, String... privilegeNames) {
        try {
            return addPermissions(session, path, principal, asPrivileges(session, privilegeNames));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + path + ": "
                                                  + Arrays.toString(privilegeNames), e);
        }
    }

    public static boolean addPermissions(Session session, String path, Principal principal, Privilege... privileges) {
        try {
            AccessControlManager acm = session.getAccessControlManager();
            AccessControlList acl = null;
            AccessControlPolicyIterator it = acm.getApplicablePolicies(path);

            if (it.hasNext()) {
                acl = (AccessControlList) it.nextAccessControlPolicy();
            } else {
                acl = (AccessControlList) acm.getPolicies(path)[0];
            }

            // ModeShape reads back all principals as SimplePrincipals after they are stored, so we have to used
            // the same principal type here or the entry will treated as a new one instead of adding privileges to the 
            // to an existing principal.  This can be considered a bug in ModeShape.
            boolean added = false;
            String servicesPath = "/" + SecurityPaths.SECURITY.resolve(AllowedActions.SERVICES).toString().toString();
            boolean isServicesPath = path.startsWith(servicesPath);
            if (isServicesPath || JcrAccessControlUtil.enableEntityAccessControl) {
                SimplePrincipal simple = SimplePrincipal.newInstance(principal.getName());
                added = acl.addAccessControlEntry(simple, privileges);
                acm.setPolicy(path, acl);
            }
            return added;
            //   return added;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + path + ": "
                                                  + Arrays.toString(privileges), e);
        }

    }

    /**
     * Adds the specified privilege to the node hierarchy starting at a child node and proceeding through its parents until
     * the destination node is reached.
     *
     * @param node           the starting node on which the privilege is assigned
     * @param principal      the principal being given the privilege
     * @param toNode         the ending parent node
     * @param privilegeNames the privilege being assigned
     * @return true if any of the nodes had their privilege change for the principle (i.e. the privilege had not already existed)
     */
    public static boolean addHierarchyPermissions(Node node, Principal principal, Node toNode, String... privilegeNames) {
        try {
            Node current = node;
            Node rootNode = toNode.getSession().getRootNode();
            AtomicBoolean added = new AtomicBoolean(false);
            Deque<Node> stack = new ArrayDeque<>();

            while (!current.equals(toNode) && !current.equals(rootNode)) {
                stack.push(current);
                current = current.getParent();
            }

            if (current.equals(rootNode) && !toNode.equals(rootNode)) {
                throw new IllegalArgumentException("addHierarchyPermissions: The \"toNode\" argument is not in the \"node\" argument's hierarchy: " + toNode);
            } else {
                stack.push(current);
            }

            stack.stream().forEach((n) -> added.compareAndSet(false, addPermissions(n, principal, privilegeNames)));

            return added.get();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to hierarchy from node " + node + " up to " + toNode, e);
        }
    }

    public static boolean removePermissions(Session session, String path, Principal principal, String... privilegeNames) {
        try {
            return removePermissions(session, path, principal, asPrivileges(session, privilegeNames));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + path + ": "
                                                  + Arrays.toString(privilegeNames), e);
        }
    }

    public static Privilege[] asPrivileges(Session session, String... privilegeNames) throws UnsupportedRepositoryOperationException, RepositoryException, AccessControlException {
        Privilege[] privs = new Privilege[privilegeNames.length];
        AccessControlManager acm = session.getAccessControlManager();

        for (int i = 0; i < privilegeNames.length; i++) {
            privs[i] = acm.privilegeFromName(privilegeNames[i]);
        }

        return privs;
    }

    public static boolean removePermissions(Node node, Principal principal, String... privilegeNames) {
        try {
            return removePermissions(node.getSession(), node.getPath(), principal, privilegeNames);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node " + node + ": " + privilegeNames, e);
        }
    }

    public static boolean removePermissions(Node node, Principal principal, Privilege... privileges) {
        try {
            return removePermissions(node.getSession(), node.getPath(), principal, privileges);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node " + node + ": " + privileges, e);
        }

    }

    public static boolean removePermissions(Session session, String path, Principal principal, Privilege... removes) {
        try {
            AccessControlManager acm = session.getAccessControlManager();
            AccessControlPolicy[] aclArray = acm.getPolicies(path);

            if (aclArray.length > 0) {
                AccessControlList acl = (AccessControlList) aclArray[0];
                boolean removed = false;

                for (AccessControlEntry entry : acl.getAccessControlEntries()) {
                    if (entry.getPrincipal().getName().equals(principal.getName())) {
                        Privilege[] newPrivs = Arrays.stream(entry.getPrivileges())
                            .filter(p -> !Arrays.stream(removes).anyMatch(r -> r.equals(p)))
                            .toArray(Privilege[]::new);

                        if (entry.getPrivileges().length != newPrivs.length) {
                            if (newPrivs.length == 0) {
                                acl.removeAccessControlEntry(entry);
                            } else {
                                acl.addAccessControlEntry(entry.getPrincipal(), newPrivs);
                            }
                            removed = true;
                        }
                    }
                }

                acm.setPolicy(path, acl);

                return removed;
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + path + ": "
                                                  + Arrays.toString(removes), e);
        }

    }

    public static boolean removeHierarchyPermissions(Node node, Principal principal, Node toNode, String... privilegeNames) {
        try {
            Node current = node;
            Node rootNode = toNode.getSession().getRootNode();
            boolean removed = false;

            while (!current.equals(toNode) && !current.equals(rootNode)) {
                removed |= removePermissions(node.getSession(), current.getPath(), principal, privilegeNames);
                current = current.getParent();
            }

            if (current.equals(rootNode) && !toNode.equals(rootNode)) {
                throw new IllegalArgumentException("addHierarchyPermissions: The \"toNode\" argument is not in the \"node\" argument's hierarchy: " + toNode);
            } else {
                removed |= removePermissions(node.getSession(), current.getPath(), principal, privilegeNames);
            }

            return removed;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to hierarch from node " + node + " up to " + toNode, e);
        }
    }

    public static boolean clearPermissions(Session session, String path) {
        try {
            AccessControlManager acm = session.getAccessControlManager();
            AccessControlPolicy[] acls = acm.getPolicies(path);

            if (acls.length > 0) {
                for (AccessControlPolicy policy : acm.getPolicies(path)) {
                    acm.removePolicy(path, policy);
                }

                return true;
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove all permission(s) from node " + path, e);
        }

    }

    public static boolean addRecursivePermissions(Node node, String nodeType, Principal principal, String... privilegeNames) {
        try {
            return addRecursivePermissions(node, nodeType, principal, asPrivileges(node.getSession(), privilegeNames));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node tree " + node, e);
        }
    }

    public static boolean addRecursivePermissions(Node node, String nodeType, Principal principal, Privilege... privileges) {
        boolean added = addPermissions(node, principal, privileges);

        for (Node child : JcrUtil.getNodesOfType(node, nodeType)) {
            added |= addRecursivePermissions(child, nodeType, principal, privileges);
        }

        return added;
    }

    public static boolean removeRecursivePermissions(Node node, String nodeType, Principal principal, String... privilegeNames) {
        try {
            return removeRecursivePermissions(node, nodeType, principal, asPrivileges(node.getSession(), privilegeNames));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node tree " + node, e);
        }
    }

    public static boolean removeRecursivePermissions(Node node, String nodeType, Principal principal, Privilege... privileges) {
        try {
            boolean removed = false;

            for (Node child : JcrUtil.getNodesOfType(node, nodeType)) {
                removed |= removeRecursivePermissions(child, nodeType, principal, privileges);
            }

            return removePermissions(node.getSession(), node.getPath(), principal, privileges) || removed;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node tree " + node, e);
        }
    }

    public static void setEnableEntityAccessControl(boolean enableEntityAccessControl) {
        JcrAccessControlUtil.enableEntityAccessControl = enableEntityAccessControl;
    }
}
