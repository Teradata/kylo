package com.thinkbiganalytics.jobrepo;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 5/9/16.
 */
public interface ApplicationStartupListener {

    void onStartup(DateTime startTime);
}
