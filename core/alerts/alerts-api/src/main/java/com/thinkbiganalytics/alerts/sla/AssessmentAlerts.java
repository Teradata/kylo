/**
 *
 */
package com.thinkbiganalytics.alerts.sla;

/*-
 * #%L
 * thinkbig-alerts-api
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

import com.thinkbiganalytics.alerts.AlertConstants;
import com.thinkbiganalytics.alerts.spi.AlertDescriptor;

import java.net.URI;

/**
 *
 */
public interface AssessmentAlerts {

    static final URI SLA_ALERT_TYPE = URI.create(AlertConstants.KYLO_ALERT_TYPE_PREFIX + "/alert/sla");
    static final URI VIOLATION_ALERT_TYPE = URI.create(SLA_ALERT_TYPE + "/violation");


    static final AlertDescriptor VIOLATION_ALERT = new AlertDescriptor(VIOLATION_ALERT_TYPE,
                                                                       "application/x-java-serialized-object",
                                                                       "Violation of a service level agreement",
                                                                       true);
}
