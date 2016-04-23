/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.servicemonitor.check;

import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import java.util.List;


public interface ServicesStatusCheck {

  List<ServiceStatusResponse> healthCheck();
}
