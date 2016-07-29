/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.auth;

import com.thinkbiganalytics.auth.BasePrincipal;

/**
 *
 * @author Sean Felten
 */
public abstract class ModeShapePrincipal extends BasePrincipal {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ModeShapePrincipal(String name) {
        super(name);
    }
}
