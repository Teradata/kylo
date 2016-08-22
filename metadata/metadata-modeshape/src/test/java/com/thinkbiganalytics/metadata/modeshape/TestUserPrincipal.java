/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import com.thinkbiganalytics.security.BasePrincipal;

/**
 *
 * @author Sean Felten
 */
public class TestUserPrincipal extends BasePrincipal {

    private static final long serialVersionUID = 1L;

    public TestUserPrincipal() {
        super("test");
    }

}
