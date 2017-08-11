package com.thinkbiganalytics.alerts.api;

/**
 * Created by sr186054 on 8/11/17.
 */
public interface SourceAlert {

    Alert getWrappedAlert();
    Alert.ID getSourceAlertId();
}
