package com.thinkbiganalytics.servicemonitor.check;


import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

/**
 * Marker interface to perform Service Status verification in the Kylo Operations Manager.
 * Any classes with this interface will be automatically wired in and checked for Service Health in Kylo.
 *
 * This class is used to check a single services.  If you are checking a multiple services that are similar you should implement {@link ServicesStatusCheck}
 * Note:  The classes must be Spring Managed Beans
 *
 * @see ServicesStatusCheck
 */
public interface ServiceStatusCheck {

    /**
     * Check a given service and return its health information
     *
     * @return the health of a given service
     */
    ServiceStatusResponse healthCheck();
}
