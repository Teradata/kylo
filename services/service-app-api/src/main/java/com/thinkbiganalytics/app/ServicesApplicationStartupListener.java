package com.thinkbiganalytics.app;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 9/21/16.
 */
public interface ServicesApplicationStartupListener {

    void onStartup(DateTime startTime);

}
