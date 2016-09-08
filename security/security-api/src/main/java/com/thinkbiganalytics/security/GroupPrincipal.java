/**
 * 
 */
package com.thinkbiganalytics.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A principal representing a user group.  This is an immutable implementation
 * of the {@link Group} principal.
 * 
 * @author Sean Felten
 */
public class GroupPrincipal extends BasePrincipal implements Group {

    private static final long serialVersionUID = 1L;
    
    private final Set<Principal> members;
    private final int hash; // Since this is immutable it only has to be calculated once.

    public GroupPrincipal(String name, Principal... members) {
        super(name);
        this.members = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(members)));
        this.hash = super.hashCode() ^ Objects.hash((Object[]) members);
    }

    /* (non-Javadoc)
     * @see java.security.acl.Group#addMember(java.security.Principal)
     */
    @Override
    public boolean addMember(Principal user) {
        throw new UnsupportedOperationException("Group principals of this type are immutable");
    }

    /* (non-Javadoc)
     * @see java.security.acl.Group#removeMember(java.security.Principal)
     */
    @Override
    public boolean removeMember(Principal user) {
        throw new UnsupportedOperationException("Group principals of this type are immutable");
    }

    /* (non-Javadoc)
     * @see java.security.acl.Group#isMember(java.security.Principal)
     */
    @Override
    public boolean isMember(Principal member) {
        return this.members.contains(member);
    }

    /* (non-Javadoc)
     * @see java.security.acl.Group#members()
     */
    @Override
    public Enumeration<? extends Principal> members() {
        return Collections.enumeration(this.members);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.BasePrincipal#hashCode()
     */
    @Override
    public int hashCode() {
        return this.hash;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.BasePrincipal#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof GroupPrincipal) {
            GroupPrincipal that = (GroupPrincipal) obj;
            return this.members.equals(that.members);
        } else {
            return false;
        }
    }

}
