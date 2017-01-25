/**
 * 
 */
package com.thinkbiganalytics.security;

/*-
 * #%L
 * thinkbig-security-api
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

import java.security.Principal;
import java.security.acl.Group;
import java.util.Arrays;
import java.util.Collection;
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
        this(name, new HashSet<>(Arrays.asList(members)));
    }
    
    public GroupPrincipal(String name, Set<Principal> members) {
        super(name);
        this.members = Collections.unmodifiableSet(new HashSet<>(members));
        this.hash = super.hashCode() ^ members.hashCode();
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
