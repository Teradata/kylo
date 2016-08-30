/**
 * 
 */
package com.thinkbiganalytics.security;

/**
 * A principal representing membership in the general "user" group.  Generally,
 * all authenticated users will be assigned this principal when logged in.
 * 
 * @author Sean Felten
 */
public class UserGroupPrincipal extends GroupPrincipal {

    private static final long serialVersionUID = 1L;

    public UserGroupPrincipal() {
        super("user");
    }
}
