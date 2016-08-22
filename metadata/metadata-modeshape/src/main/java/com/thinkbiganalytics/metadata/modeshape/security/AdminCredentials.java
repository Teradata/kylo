package com.thinkbiganalytics.metadata.modeshape.security;

import java.security.Principal;

import javax.jcr.Credentials;

public class AdminCredentials implements Credentials {

    private static final long serialVersionUID = -6649298501188019301L;
    
    public static Principal getPrincipal() {
        return new ModeShapePrincipal("dladmin");
    }

    public AdminCredentials() {
    }

}
