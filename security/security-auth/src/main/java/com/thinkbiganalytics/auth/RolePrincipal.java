/**
 * 
 */
package com.thinkbiganalytics.auth;

/**
 * Basic principal representing a named role.
 * @author Sean Felten
 */
public class RolePrincipal extends BasePrincipal {

    private static final long serialVersionUID = 1L;

    public RolePrincipal() {
    }

    public RolePrincipal(String name) {
        super(name.toUpperCase().startsWith("ROLE_") ? name.toUpperCase() : "ROLE_" + name.toUpperCase());
    }

}
