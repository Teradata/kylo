/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.role;

import java.security.Principal;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.security.RolePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 * @author Sean Felten
 */
public class JcrSecurityRole extends JcrObject implements SecurityRole {

    public static final String NAME = "tba:systemName";
    public static final String TITLE = "jcr:title";
    public static final String DESCR = "jcr:description";

    public JcrSecurityRole(Node node) {
        super(node);
    }
    
    public JcrSecurityRole(Node node, String name) {
        super(node);
        setProperty(NAME, name);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getPrincipal()
     */
    @Override
    public Principal getPrincipal() {
        return new RolePrincipal(getSystemName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getSystemName()
     */
    @Override
    public String getSystemName() {
        return getProperty(NAME, String.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getTitle()
     */
    @Override
    public String getTitle() {
        return getProperty(TITLE, String.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getDescription()
     */
    @Override
    public String getDescription() {
        return getProperty(DESCR, String.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getAllowedActions()
     */
    @Override
    public AllowedActions getAllowedActions() {
        // TODO Auto-generated method stub
        return null;
    }

}
