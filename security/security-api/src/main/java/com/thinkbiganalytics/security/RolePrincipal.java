/**
 * 
 */
package com.thinkbiganalytics.security;

/**
 * Basic principal representing a named role.
 * @author Sean Felten
 */
public class RolePrincipal extends BasePrincipal {

    private static final long serialVersionUID = 1L;

    public RolePrincipal(String name) {
        super(name);
    }

}
