/**
 * 
 */
package com.thinkbiganalytics.security;

/**
 * Basic principal representing a username.
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
