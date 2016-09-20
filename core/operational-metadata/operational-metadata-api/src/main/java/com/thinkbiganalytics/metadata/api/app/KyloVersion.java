package com.thinkbiganalytics.metadata.api.app;

/**
 * Created by sr186054 on 9/19/16.
 */
public interface KyloVersion {

    String getVersion();

    Float getMajorVersionNumber();

    String getMinorVersion();

    String getMajorVersion();


    String getDescription();

    KyloVersion update(KyloVersion v);
}
