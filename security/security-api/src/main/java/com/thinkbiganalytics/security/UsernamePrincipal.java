/**
 * 
 */
package com.thinkbiganalytics.security;

/**
 * Basic principal representing a user name.  All authenticated users should be
 * assigned this principal when logged in.
 * 
 * @author Sean Felten
 */
public class UsernamePrincipal extends BasePrincipal {

    private static final long serialVersionUID = 1L;

    public UsernamePrincipal() {
    }

    public UsernamePrincipal(String name) {
        super(name);
    }

}
