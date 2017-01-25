package com.thinkbiganalytics.servicemonitor.check;

import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import java.util.List;

/**
 * Marker interface to perform Service Status verification in the Kylo Operations Manager.
 * Any classes with this interface will be automatically wired in and checked for Service Health in Kylo.
 *
 * This class is used to check multiple services.  If you are checking a single service you should implement {@link ServiceStatusCheck}
 * Note:  The classes must be Spring Managed Beans
 *
 * @see ServiceStatusCheck
 */
public interface ServicesStatusCheck {

    /**
     * Check a number of services and return health information about each service.
     *
     * @return a list of the service health for each service
     */
    List<ServiceStatusResponse> healthCheck();
}
