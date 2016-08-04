/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

import java.security.Principal;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;

import com.thinkbiganalytics.metadata.api.security.MetadataAccessControlException;

/**
 *
 * @author Sean Felten
 */
public class JcrAccessControlUtil {
    
    public static AccessControlList addPermissions(Node node, Principal principal, String... privilegeNames) {
        try {
            return addPermissions(node.getSession(), node.getPath(), principal, privilegeNames);
        } catch (RepositoryException e) {
            throw new MetadataAccessControlException("Failed to add permission(s) to node " + node + ": " + privilegeNames, e);
        }
    }

    public static AccessControlList addPermissions(Node node, Principal principal, Privilege... privileges) {
        try {
            return addPermissions(node.getSession(), node.getPath(), principal, privileges);
        } catch (RepositoryException e) {
            throw new MetadataAccessControlException("Failed to add permission(s) to node " + node + ": " + privileges, e);
        }

    }
    
    public static AccessControlList addPermissions(Session session, String path, Principal principal, String... privilegeNames) {
        try {
            AccessControlManager acm = session.getAccessControlManager();
            Privilege[] privs = new Privilege[privilegeNames.length];
            
            for (int i = 0; i < privilegeNames.length; i++) {
                privs[i] = acm.privilegeFromName(privilegeNames[i]);
            }
            
            return addPermissions(session, path, principal, privs);
        } catch (RepositoryException e) {
            throw new MetadataAccessControlException("Failed to add permission(s) to node " + path + ": " + privilegeNames, e);
        }
    }
    
    public static AccessControlList addPermissions(Session session, String path, Principal principal, Privilege... privileges) {
        try {
            AccessControlManager acm = session.getAccessControlManager();
            AccessControlList acl = null;
            AccessControlPolicyIterator it = acm.getApplicablePolicies(path);
            
            if (it.hasNext()) {
                acl = (AccessControlList)it.nextAccessControlPolicy();
            } else {
                acl = (AccessControlList)acm.getPolicies(path)[0];
            }
            
            acl.addAccessControlEntry(principal, privileges);
            acm.setPolicy(path, acl);
            
            return acl;
        } catch (RepositoryException e) {
            throw new MetadataAccessControlException("Failed to add permission(s) to node " + path + ": " + privileges, e);
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
            throw new MetadataAccessControlException("Failed to remove all permission(s) from node " + path, e);
        }
        
    }
}
