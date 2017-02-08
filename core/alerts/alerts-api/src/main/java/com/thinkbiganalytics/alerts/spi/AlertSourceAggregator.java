/**
 *
 */
package com.thinkbiganalytics.alerts.spi;

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

/**
 * Service provider interface that aggregates alert sources/managers by allowing each
 * to be registered by an implementor of this interface.
 */
public interface AlertSourceAggregator {

    /**
     * @param src the AlertSource to add
     * @return true if this source had not previously been added
     */
    boolean addAlertSource(AlertSource src);

    /**
     * @param src the AlertSource to remove
     * @return true if this source existed and has been removed
     */
    boolean removeAlertSource(AlertSource src);

    /**
     * @param mgr the AlertManager to add
     * @return true if this manager had not previously been added
     */
    boolean addAlertManager(AlertManager mgr);

    /**
     * @param mgr the AlertManager to remove
     * @return true if this manager existed and has been removed
     */
    boolean removeAlertManager(AlertManager mgr);
}
