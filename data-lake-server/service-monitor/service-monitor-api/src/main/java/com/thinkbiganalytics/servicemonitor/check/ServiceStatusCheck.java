/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.servicemonitor.check;


import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

public interface ServiceStatusCheck {
    ServiceStatusResponse healthCheck();
}
