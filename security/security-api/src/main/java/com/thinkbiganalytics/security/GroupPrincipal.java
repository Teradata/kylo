/**
 * 
 */
package com.thinkbiganalytics.security;

/**
 * Basic principal representing a named user group.
 * 
 * @author Sean Felten
 */
public class GroupPrincipal extends BasePrincipal {

    private static final long serialVersionUID = 1L;

    public GroupPrincipal(String name) {
        super(name);
    }

}
