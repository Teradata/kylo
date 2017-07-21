package com.thinkbiganalytics.alerts.api;

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

import java.net.URI;

/**
 * Created by sr186054 on 7/21/17.
 */
public interface AlertSummary {

    /**
     * A  URI defining the type of alert this is.
     *
     * @return the unique type
     */
    String getType();

    /**
     * A separate descriptor further defining the type of this alert
     * @return the descriptor defining the type of the alert
     */
    String getSubtype();

    /**
     *
     * @return A count of the alerts
     */
    Long getCount();

    /**
     * @return the level of this alert
     */
    Alert.Level getLevel();

    /**
     * The max alert time stamp for this URI,subtype, and level
     * @return
     */
    Long getLastAlertTimestamp();


}
