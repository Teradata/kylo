package com.thinkbiganalytics.metadata.api.security;

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 * Created by sr186054 on 4/12/17.
 */
public class RoleNotFoundException extends MetadataException {

    public RoleNotFoundException() {
        super();
    }

    public RoleNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RoleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoleNotFoundException(String message) {
        super(message);
    }

    public RoleNotFoundException(Throwable cause) {
        super(cause);
    }
}
