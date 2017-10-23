package com.thinkbiganalytics.feedmgr.nifi.cache;

/**
 * Created by sr186054 on 10/23/17.
 */
public class NiFiFlowCacheException extends RuntimeException {

    public NiFiFlowCacheException() {
        super();
    }

    public NiFiFlowCacheException(String message) {
        super(message);
    }

    public NiFiFlowCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public NiFiFlowCacheException(Throwable cause) {
        super(cause);
    }

    protected NiFiFlowCacheException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
