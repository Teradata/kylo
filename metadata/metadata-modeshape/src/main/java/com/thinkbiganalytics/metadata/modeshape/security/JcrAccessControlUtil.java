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
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.modeshape.jcr.ModeShapeRoles;
import org.modeshape.jcr.security.SimplePrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
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

    private static final String GROUP_PREFIX = "G_";
    private static final String USER_PREFIX = "U_";

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
        return addPermissions(node, principal, Arrays.asList(privilegeNames));
    }
    
    public static boolean setPermissions(Node node, Principal principal, Collection<String> privilegeNames) {
        try {
            return setPermissions(node.getSession(), node.getPath(), principal, privilegeNames);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set permission(s) on node " + node + ": " + privilegeNames, e);
        }
    }
    
    public static boolean setPermissions(Session session, String path, Principal principal, String... privilegeNames) {
        return setPermissions(session, path, principal, Arrays.asList(privilegeNames));
        
    }

    public static boolean setPermissions(Session session, String path, Principal principal, Collection<String> privilegeNames) {
        try {
            return setPermissions(session, path, principal, asPrivileges(session, privilegeNames));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set permission(s) on node " + path + ": " + privilegeNames, e);
        }
    }

    public static boolean setPermissions(Session session, String path, Principal principal, Privilege... privileges) {
        return updatePermissions(session, path, principal, true, privileges);
    }

    public static boolean addPermissions(Node node, Principal principal, Collection<String> privilegeNames) {
        try {
            return addPermissions(node.getSession(), node.getPath(), principal, privilegeNames);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + node + ": " + privilegeNames, e);
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
        return addPermissions(session, path, principal, Arrays.asList(privilegeNames));
        
    }

    public static boolean addPermissions(Session session, String path, Principal principal, Collection<String> privilegeNames) {
        try {
            // No privileges results in a no-op.
            if (privilegeNames.size() > 0) {
                return addPermissions(session, path, principal, asPrivileges(session, privilegeNames));
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + path + ": " + privilegeNames, e);
        }
    }

    public static boolean addPermissions(Session session, String path, Principal principal, Privilege... privileges) {
        // No privileges results in a no-op.
        if (privileges.length > 0) {
            return updatePermissions(session, path, principal, false, privileges);
        } else {
            return false;
        }
    }
    
    public static boolean hasAnyPermission(Node node, Principal principal, String... privileges) {
        try {
            return hasAnyPermission(node.getSession(), node.getPath(), principal, privileges);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to check permission(s) for node: " + node);
        }
    }
    
    public static boolean hasAnyPermission(Node node, Principal principal, Privilege... privileges) {
        try {
            return hasAnyPermission(node.getSession(), node.getPath(), principal, privileges);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to check permission(s) of node " + node + ": "
                                                  + Arrays.toString(privileges), e);
        }
    }
    
    public static boolean hasAnyPermission(Session session, String path, Principal principal, String... privilegeNames) {
        try {
            return hasAnyPermission(session, path, principal, asPrivileges(session, privilegeNames));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to check permission(s) for node: " + path);
        }
    }
    
    public static boolean hasAnyPermission(Session session, String path, Principal principal, Privilege... privileges) {
        return hasAnyPermission(session, path, principal, Arrays.asList(privileges));
    }

    public static boolean hasAnyPermission(Session session, String path, Principal principal, Collection<Privilege> privileges) {
        return getPrivileges(session, principal, path).stream().anyMatch(p -> privileges.contains(p));
    }
    
    public static Set<Privilege> getPrivileges(Node node, Principal principal) {
        try {
            return getPrivileges(node.getSession(), principal, node.getPath());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the privileges for node " + node, e);
        }
    }
    
    public static Set<Privilege> getPrivileges(Session session, Principal principal, String path) {
        try {
            AccessControlManager acm = session.getAccessControlManager();
            AccessControlList acl = getAccessControlList(path, acm);
            
            for (AccessControlEntry entry : acl.getAccessControlEntries()) {
                if (matchesPrincipal(principal, entry)) {
                    return new HashSet<>(Arrays.asList(entry.getPrivileges()));
                }
            }
                
            return Collections.emptySet();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the privileges for node " + path, e);
        }
    }
    
    public static Map<Principal, Set<Privilege>> getAllPrivileges(Node node) {
        try {
            return getAllPrivileges(node.getSession(), node.getPath());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the privileges for node " + node, e);
        }
    }
    
    public static Map<Principal, Set<Privilege>> getAllPrivileges(Session session, String path) {
        try {
            Map<Principal, Set<Privilege>> map = new HashMap<>();
            AccessControlManager acm = session.getAccessControlManager();
            AccessControlList acl = getAccessControlList(path, acm);
            
            for (AccessControlEntry entry : acl.getAccessControlEntries()) {
                Principal principal = derivePrincipal(entry);
                
                map.put(principal, new HashSet<>(Arrays.asList(entry.getPrivileges())));
            }
            
            return map;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to get the privileges for node " + path, e);
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
        return addHierarchyPermissions(node, principal, toNode, Arrays.asList(privilegeNames));
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
    public static boolean addHierarchyPermissions(Node node, Principal principal, Node toNode, Collection<String> privilegeNames) {
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
            throw new MetadataRepositoryException("Failed to remove permission(s) from node " + path + ": "
                                                  + Arrays.toString(privilegeNames), e);
        }
    }
    
    public static Privilege[] asPrivileges(Session session, String... privilegeNames) throws UnsupportedRepositoryOperationException, RepositoryException, AccessControlException {
        return asPrivileges(session, Arrays.asList(privilegeNames));
    }

    public static Privilege[] asPrivileges(Session session, Collection<String> privilegeNames) throws UnsupportedRepositoryOperationException, RepositoryException, AccessControlException {
        Privilege[] privs = new Privilege[privilegeNames.size()];
        AccessControlManager acm = session.getAccessControlManager();
        int i = 0;
        
        for (String name : privilegeNames) {
            privs[i++] = acm.privilegeFromName(name);
        }

        return privs;
    }

    public static boolean removePermissions(Node node, Principal principal, String... privilegeNames) {
        try {
            return removePermissions(node.getSession(), node.getPath(), principal, privilegeNames);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node " + node + ": " + Arrays.toString(privilegeNames), e);
        }
    }

    public static boolean removePermissions(Node node, Principal principal, Privilege... privileges) {
        try {
            return removePermissions(node.getSession(), node.getPath(), principal, privileges);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node " + node + ": " + Arrays.toString(privileges), e);
        }

    }

    public static boolean removePermissions(Session session, String path, Principal principal, Privilege... removes) {
        try {
            // if there are no permissions specified or the principal is "admin" then do nothing.
            // There should alwasy be an ACL entry for "admin".
            if (removes.length > 0 && ! principal.getName().equals(ModeShapeRoles.ADMIN)) {
                AccessControlManager acm = session.getAccessControlManager();
                AccessControlPolicy[] aclArray = acm.getPolicies(path);

                if (aclArray.length > 0) {
                    AccessControlList acl = (AccessControlList) aclArray[0];
                    boolean removed = false;

                    for (AccessControlEntry entry : acl.getAccessControlEntries()) {
                        if (matchesPrincipal(principal, entry)) {
                            Privilege[] newPrivs = Arrays.stream(entry.getPrivileges())
                                .filter(p -> !Arrays.stream(removes).anyMatch(r -> r.equals(p)))
                                .toArray(Privilege[]::new);

                            if (entry.getPrivileges().length != newPrivs.length) {
                                acl.removeAccessControlEntry(entry);
                                
                                if (newPrivs.length != 0) {
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
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node " + path + ": "
                                                  + Arrays.toString(removes), e);
        }

    }
    
    public static boolean removeAllPermissions(Node node, Principal principal) {
        try {
            return removeAllPermissions(node.getSession(), node.getPath(), principal);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to renove all permission(s) from node: " + node);
        }
    }
    
    public static boolean removeAllPermissions(Session session, String path, Principal principal) {
        try {
            AccessControlManager acm = session.getAccessControlManager();
            AccessControlPolicy[] aclArray = acm.getPolicies(path);
            
            // Never remove permissions for "admin".
            if (aclArray.length > 0 && ! principal.getName().equals(ModeShapeRoles.ADMIN)) {
                AccessControlList acl = (AccessControlList) aclArray[0];
                boolean removed = removeEntry(acl, principal);
                
                acm.setPolicy(path, acl);
                
                return removed;
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove all permission(s) from node " + path, e);
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
                throw new IllegalArgumentException("removeHierarchyPermissions: The \"toNode\" argument is not in the \"node\" argument's hierarchy: " + toNode);
            } else {
                removed |= removePermissions(node.getSession(), current.getPath(), principal, privilegeNames);
            }

            return removed;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from hierarch from node " + node + " up to " + toNode, e);
        }
    }
    
    public static boolean removeHierarchyAllPermissions(Node node, Principal principal, Node toNode) {
        try {
            Node current = node;
            Node rootNode = toNode.getSession().getRootNode();
            boolean removed = false;
            
            while (!current.equals(toNode) && !current.equals(rootNode)) {
                removed |= removeAllPermissions(node.getSession(), current.getPath(), principal);
                current = current.getParent();
            }
            
            if (current.equals(rootNode) && !toNode.equals(rootNode)) {
                throw new IllegalArgumentException("removeHierarchyAllPermissions: The \"toNode\" argument is not in the \"node\" argument's hierarchy: " + toNode);
            } else {
                removed |= removeAllPermissions(node.getSession(), current.getPath(), principal);
            }
            
            return removed;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove all permission(s) from hierarch from node " + node + " up to " + toNode, e);
        }
    }
    
    public static boolean clearPermissions(Node node) {
        try {
            return clearPermissions(node.getSession(), node.getPath());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove all permission(s) from node " + node, e);
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
    
    public static boolean clearRecursivePermissions(Node node, String nodeType) {
        boolean cleared = false;
        
        for (Node child : JcrUtil.getNodesOfType(node, nodeType)) {
            cleared |= clearRecursivePermissions(child, nodeType);
            cleared |= clearPermissions(child);
        }
        
        return cleared;
    }
    

    public static boolean clearHierarchyPermissions(Node node, Node toNode) {
        try {
            Node current = node;
            Node rootNode = toNode.getSession().getRootNode();
            boolean removed = false;

            while (!current.equals(toNode) && !current.equals(rootNode)) {
                removed |= clearPermissions(current);
                current = current.getParent();
            }

            if (current.equals(rootNode) && !toNode.equals(rootNode)) {
                throw new IllegalArgumentException("clearHierarchyPermissions: The \"toNode\" argument is not in the \"node\" argument's hierarchy: " + toNode);
            } else {
                removed |= clearPermissions(current);
            }

            return removed;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to hierarch from node " + node + " up to " + toNode, e);
        }
    }


    public static boolean addRecursivePermissions(Node node, String nodeType, Principal principal, String... privilegeNames) {
        try {
            // No privileges results in a no-op.
            if (privilegeNames.length > 0) {
                return addRecursivePermissions(node, nodeType, principal, asPrivileges(node.getSession(), privilegeNames));
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node tree " + node, e);
        }
    }

    public static boolean addRecursivePermissions(Node node, String nodeType, Principal principal, Privilege... privileges) {
        // No privileges results in a no-op.
        if (privileges.length > 0) {
            boolean added = addPermissions(node, principal, privileges);

            for (Node child : JcrUtil.getNodesOfType(node, nodeType)) {
                added |= addRecursivePermissions(child, nodeType, principal, privileges);
            }

            return added;
        } else {
            return false;
        }
    }

    public static boolean removeRecursivePermissions(Node node, String nodeType, Principal principal, String... privilegeNames) {
        try {
            // No privileges results in a no-op.
            if (privilegeNames.length > 0) {
                return removeRecursivePermissions(node, nodeType, principal, asPrivileges(node.getSession(), privilegeNames));
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node tree " + node, e);
        }
    }

    public static boolean removeRecursivePermissions(Node node, String nodeType, Principal principal, Privilege... privileges) {
        try {
            // No privileges results in a no-op.
            if (privileges.length > 0) {
                boolean removed = false;
    
                for (Node child : JcrUtil.getNodesOfType(node, nodeType)) {
                    removed |= removeRecursivePermissions(child, nodeType, principal, privileges);
                }
    
                return removePermissions(node.getSession(), node.getPath(), principal, privileges) || removed;
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node tree " + node, e);
        }
    }
    
    public static boolean removeRecursiveAllPermissions(Node node, String nodeType, Principal principal) {
        try {
            boolean removed = false;
            
            for (Node child : JcrUtil.getNodesOfType(node, nodeType)) {
                removed |= removeRecursiveAllPermissions(child, nodeType, principal);
            }
            
            return removeAllPermissions(node.getSession(), node.getPath(), principal) || removed;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to remove permission(s) from node tree " + node, e);
        }
    }

    public static boolean matchesRole(Principal principal, String roleName) {
        String princName = principal.getName();
        
        if (principal instanceof UsernamePrincipal) {
            return roleName.startsWith(USER_PREFIX) && roleName.startsWith(princName, USER_PREFIX.length());
        } else if (principal instanceof Group && ! roleName.equals(ModeShapeRoles.ADMIN)) {
            // The "admin" group is a special case "superuser" role in modeshape that may not have a prefix but
            // should still be match matched to a Group principal with that name, so skip this block for admin.
            return roleName.startsWith(GROUP_PREFIX) && roleName.startsWith(princName, GROUP_PREFIX.length());
        } else {
            return roleName.equals(principal.getName());
        }
    }

    private static boolean updatePermissions(Session session, String path, Principal principal, boolean replace, Privilege... privileges) {
        try {
            AccessControlManager acm = session.getAccessControlManager();
            AccessControlList acl = getAccessControlList(path, acm);
            boolean changed = false;
            
            if (replace) {
                changed |= removeEntry(acl, principal);
            }
            
            if (privileges.length > 0) {
                changed |= addEntry(session, acl, principal, privileges);
            }
            
            acm.setPolicy(path, acl);
            return changed;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to add permission(s) to node " + path + ": "
                                                  + Arrays.toString(privileges), e);
        }
    }

    private static boolean addEntry(Session session, AccessControlList acl, Principal principal, Privilege... privileges) throws RepositoryException, AccessControlException,
                                                                                                                          UnsupportedRepositoryOperationException {
        // Ensure admin is always included in the ACL
        if (acl.getAccessControlEntries().length == 0) {
            SimplePrincipal simple = SimplePrincipal.newInstance(ModeShapeRoles.ADMIN);
            acl.addAccessControlEntry(simple, asPrivileges(session, Privilege.JCR_ALL));
        }
    
        // ModeShape reads back all principals as SimplePrincipals after they are stored, so we have to use
        // the same principal type here or the entry will treated as a new one instead of adding privileges to the 
        // to an existing principal.  This can be considered a bug in ModeShape.
        SimplePrincipal simple = encodePrincipal(principal);
        boolean added = acl.addAccessControlEntry(simple, privileges);
        return added;
    }

    private static boolean removeEntry(AccessControlList acl, Principal principal) throws RepositoryException {
        boolean removed = false;
        
        for (AccessControlEntry entry : acl.getAccessControlEntries()) {
            if (matchesPrincipal(principal, entry)) {
                acl.removeAccessControlEntry(entry);
                removed = true;
            }
        }
        
        return removed;
    }

    private static AccessControlList getAccessControlList(String path, AccessControlManager acm) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        AccessControlList acl = null;
        AccessControlPolicyIterator it = acm.getApplicablePolicies(path);
    
        if (it.hasNext()) {
            acl = (AccessControlList) it.nextAccessControlPolicy();
        } else {
            acl = (AccessControlList) acm.getPolicies(path)[0];
        }
        return acl;
    }

    /**
     * Tests if the specified privilege implies, either directly or as an aggregate, the named privilege.
     * @param checked the privilege being checked
     * @param implied the name of the privilege to be implied
     * @return true if the check privilege implies the given privilege name
     */
    private static boolean implies(Privilege checked, String implied) {
        if (checked.getName().equals(implied)) {
            return true;
        } else if (checked.isAggregate()) {
            return impliesAny(checked.getAggregatePrivileges(), implied);
        } else {
            return false;
        }
    }

    /**
     * Tests if any of the specified privileges imply, either directly or as an aggregate, the named privilege.
     * @param privileges an array of privileges
     * @param implied the name of the privilege to be implied
     * @return true if the any of the privileges implies the given privilege name
     */
    private static boolean impliesAny(Privilege[] privileges, String implied) {
        for (Privilege checked : privileges) {
            if (implies(checked, implied)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Constructs a SimplePrincipal after encoding the source principal's type into the name.
     * @param principal the source principal
     * @return a SimplePrincipal derived from the source
     */
    private static SimplePrincipal encodePrincipal(Principal principal) {
        if (principal instanceof UsernamePrincipal) {
            return SimplePrincipal.newInstance(USER_PREFIX + principal.getName());
        } else if (principal instanceof Group) {
            return SimplePrincipal.newInstance(GROUP_PREFIX + principal.getName());
        } else {
            return SimplePrincipal.newInstance(principal.getName());
        }
    }
    
    /**
     * Extracts the principal of the ACL entry and converts it to the appropriate principal type.
     * @param entry the entry
     * @return the derived principal
     */
    private static Principal derivePrincipal(AccessControlEntry entry) {
        Principal principal = entry.getPrincipal();
        return principal instanceof SimplePrincipal ? derivePrincipal((SimplePrincipal) entry.getPrincipal()) : principal;
    }

    /**
     * Derives the correct principal type from the info encoded in the name of the provided
     * SimplePrincipal coming from ModeShape.  If the SimplePrincipal does not have encoded
     * type info then just return it.
     * @param principal a SimplePrincipal that may have encoded type information.
     * @return the derived Principal
     */
    private static Principal derivePrincipal(SimplePrincipal principal) {
        if (principal.getName().startsWith(USER_PREFIX)) {
            return new UsernamePrincipal(principal.getName().substring(USER_PREFIX.length()));
        } else if (principal.getName().startsWith(GROUP_PREFIX)) {
            return new GroupPrincipal(principal.getName().substring(GROUP_PREFIX.length()));
        } else {
            return principal;
        }
    }

    /**
     * Tests whether the supplied principal matches the principal associated with the ACL entry
     */
    private static boolean matchesPrincipal(Principal principal, AccessControlEntry entry) {
        String entryName = entry.getPrincipal().getName();
        String princName = principal.getName();
        
        if (principal instanceof UsernamePrincipal) {
            return entryName.startsWith(USER_PREFIX) && entryName.startsWith(princName, USER_PREFIX.length());
        } else if (principal instanceof Group) {
            return entryName.startsWith(GROUP_PREFIX) && entryName.startsWith(princName, GROUP_PREFIX.length());
        }

        // Default to matching just the principal name.  This will match users and groups during upgrade
        // but those will have been converted to their encoded types after upgrade completes.
        return entry.getPrincipal().getName().equals(principal.getName());
    }
}
