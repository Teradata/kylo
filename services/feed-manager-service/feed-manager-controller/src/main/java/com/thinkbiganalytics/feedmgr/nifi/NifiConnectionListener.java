package com.thinkbiganalytics.feedmgr.nifi;

/**
 * Created by sr186054 on 12/21/16.
 */
public interface NifiConnectionListener {

    void onConnected();

    void onDisconnected();

}
