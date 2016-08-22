/**
 * 
 */
package com.thinkbiganalytics.security;

/**
 *
 * @author Sean Felten
 */
public class UserRolePrincipal extends RolePrincipal {

    private static final long serialVersionUID = 1L;

    public UserRolePrincipal() {
        super("user");
    }
}
