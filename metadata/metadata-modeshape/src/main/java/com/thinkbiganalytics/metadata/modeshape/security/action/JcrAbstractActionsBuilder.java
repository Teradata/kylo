/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import java.security.Principal;

import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAdminPrincipal;

/**
 *
 * @author Sean Felten
 */
public abstract class JcrAbstractActionsBuilder {

    protected final Principal managementPrincipal;

    public JcrAbstractActionsBuilder() {
        this(new ModeShapeAdminPrincipal());
    }
    
    public JcrAbstractActionsBuilder(Principal mgtPrincipal) {
        super();
        this.managementPrincipal = mgtPrincipal;
    }
    
    protected Principal getManagementPrincipal() {
        return this.managementPrincipal;
    }
}
