package com.thinkbiganalytics.metadata.api.app;

/**
 * Created by sr186054 on 9/19/16.
 */
public interface KyloVersionProvider {

    KyloVersion getKyloVersion();

    KyloVersion updateToCurrentVersion();
}
