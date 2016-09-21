/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

/**
 *
 * @author Sean Felten
 */
public class ModeShapeReadOnlyPrincipal extends ModeShapePrincipal {
    
    public static final ModeShapeReadOnlyPrincipal INSTANCE = new ModeShapeReadOnlyPrincipal();

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public ModeShapeReadOnlyPrincipal() {
        super("readonly");
    }

}
