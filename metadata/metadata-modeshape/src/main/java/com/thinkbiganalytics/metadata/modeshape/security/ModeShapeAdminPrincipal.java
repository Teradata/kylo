/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

/**
 *
 * @author Sean Felten
 */
public class ModeShapeAdminPrincipal extends ModeShapePrincipal {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public ModeShapeAdminPrincipal() {
        super("admin");
    }

}
