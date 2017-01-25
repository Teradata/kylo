package com.thinkbiganalytics.servicemonitor.check;

/*-
 * #%L
 * thinkbig-service-monitor-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
