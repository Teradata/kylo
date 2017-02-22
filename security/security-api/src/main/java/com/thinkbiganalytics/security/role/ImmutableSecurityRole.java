/**
 * 
 */
package com.thinkbiganalytics.security.role;

import java.security.Principal;

import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * A immutable implementation of SecurityRole.
 */
public class ImmutableSecurityRole implements SecurityRole {

    private final String systemName;
    private final String title;
    private final String description;
    private final AllowedActions allowedActions;
    
    public ImmutableSecurityRole(SecurityRole role) {
        this(role.getSystemName(), role.getTitle(), role.getDescription(), role.getAllowedActions());
    }
   
    public ImmutableSecurityRole(String systemName, String title, String description, AllowedActions allowedActions) {
        super();
        this.systemName = systemName;
        this.title = title;
        this.description = description;
        this.allowedActions = new ImmutableAllowedActions(allowedActions);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getPrincipal()
     */
    @Override
    public Principal getPrincipal() {
        return new GroupPrincipal(getSystemName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getSystemName()
     */
    @Override
    public String getSystemName() {
        return this.systemName;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getTitle()
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getAllowedActions()
     */
    @Override
    public AllowedActions getAllowedActions() {
        return this.allowedActions;
    }

}
