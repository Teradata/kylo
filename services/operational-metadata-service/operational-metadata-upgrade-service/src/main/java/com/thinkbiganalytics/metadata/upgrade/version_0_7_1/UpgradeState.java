/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade.version_0_7_1;

import com.thinkbiganalytics.metadata.api.app.KyloVersion;

/**
 *
 */
public class UpgradeState implements com.thinkbiganalytics.metadata.upgrade.UpgradeState {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#getStartingVersion()
     */
    @Override
    public KyloVersion getStartingVersion() {
        return asVersion("0.7", "1");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeFrom(KyloVersion startingVersion) {
        if (getStartingVersion().equals(startingVersion)) {
            
        }
    }

}
