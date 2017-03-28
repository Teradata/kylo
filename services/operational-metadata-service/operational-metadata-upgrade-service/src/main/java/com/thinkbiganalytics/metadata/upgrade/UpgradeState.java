/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade;

import java.net.URL;

import com.thinkbiganalytics.metadata.api.app.KyloVersion;

/**
 *
 */
public interface UpgradeState { 
    
    KyloVersion getStartingVersion();

    void upgradeFrom(KyloVersion startingVersion);

    default URL getResource(String name) {
        return getClass().getResource(name);
    }
    
    default KyloVersion asVersion(String major, String minor) {
        return new UpgradeKyloService.Version(major, minor);
    }

}
