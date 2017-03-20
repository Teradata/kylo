/**
 *
 */
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
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.security.Principal;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
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
 *
 */
public final class JcrAccessControlUtil {

    private JcrAccessControlUtil() {
        throw new AssertionError(JcrAccessControlUtil.class + " is a static utility class");
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

            boolean added = acl.addAccessControlEntry(principal, privileges);
            acm.setPolicy(path, acl);

            return added;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + path + ": "
                    + Arrays.toString(privileges), e);
        }

    }

    public static boolean addHierarchyPermissions(Node node, Principal principal, Node toNode, String... privilegeNames) {
        try {
            Node endNode = toNode;
            Node current = node;
            Deque<Node> stack = new ArrayDeque<>();
            AtomicBoolean added = new AtomicBoolean(false);

            do {
                stack.push(current);
                current = current.getParent();
            } while (!current.equals(endNode) && !current.equals(toNode.getSession().getRootNode()));

            stack.stream().forEach((n) -> added.set(addPermissions(n, principal, privilegeNames) || added.get()));

            return added.get();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to hierarch from node " + node + " up to " + toNode, e);
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
                            .filter(p -> Arrays.stream(removes).anyMatch(r -> !r.equals(p)))
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
            Node endNode = toNode;
            Node current = node;
            boolean removed = false;

            do {
                removed |= removePermissions(node.getSession(), node.getPath(), principal, privilegeNames);
                current = current.getParent();
            } while (!current.equals(endNode) && !current.equals(toNode.getSession().getRootNode()));

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
}
