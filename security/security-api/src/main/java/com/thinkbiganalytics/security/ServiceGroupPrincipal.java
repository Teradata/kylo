/**
 * 
 */
package com.thinkbiganalytics.security;

/**
 * A principal representing a privileged service operating with administrative rights.
 * 
 * @author Sean Felten
 */
public class ServiceGroupPrincipal extends GroupPrincipal {

    private static final long serialVersionUID = 1L;

    public ServiceGroupPrincipal() {
        super("admin");
    }
}
