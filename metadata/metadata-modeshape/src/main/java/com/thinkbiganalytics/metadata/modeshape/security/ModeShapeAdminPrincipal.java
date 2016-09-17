/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

/**
 *
 * @author Sean Felten
 */
public class ModeShapeAdminPrincipal extends ModeShapePrincipal {
    
    public static final ModeShapeAdminPrincipal INSTANCE = new ModeShapeAdminPrincipal();

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public ModeShapeAdminPrincipal() {
        super("admin");
    }

}
