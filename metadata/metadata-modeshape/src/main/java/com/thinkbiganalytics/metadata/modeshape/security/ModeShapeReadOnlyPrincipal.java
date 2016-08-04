/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

/**
 *
 * @author Sean Felten
 */
public class ModeShapeReadOnlyPrincipal extends ModeShapePrincipal {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public ModeShapeReadOnlyPrincipal() {
        super("readonly");
    }

}
