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

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.api.EntityAwareAlertCriteria;
import com.thinkbiganalytics.security.role.SecurityRole;

import java.io.Serializable;
import java.net.URI;
import java.util.Iterator;

/**
 * A kind of AlertSource that provides alert creation and change management functions.
 */
public interface AlertManager extends AlertSource {

    /**
     * Adds a new descriptor that this manager will provide.
     *
     * @param descriptor the descriptor to add
     * @return true if the descriptor was not previously present
     */
    boolean addDescriptor(AlertDescriptor descriptor);

    /**
     * Creates a new alert and adds
     *
     * @param type        the type
     * @param subtype     the subtype - a high level summary string describing the object for this alert (i.e. Service name that failed).  Used in grouping alerts together
     * @param level       the level
     * @param description a description of the alert
     * @param content     optional content, the type of which is specific to the kind of alert
     */
    <C extends Serializable> Alert create(URI type, String subtype,Alert.Level level, String description, C content);

    /**
     * Creates a new alert and adds
     *
     * @param type        the type
     * @param level       the level
     * @param description a description of the alert
     * @param content     optional content, the type of which is specific to the kind of alert
     */
    <C extends Serializable> Alert createEntityAlert(URI type, Alert.Level level, String description, EntityIdentificationAlertContent<C> content);

    /**
     * Creates/wraps the content with Entity Identifiction Information
     * @param entityId the entity id
     * @param entityType the type of the entity
     * @param content
     * @param <C>
     * @return
     */
    <C extends Serializable>  EntityIdentificationAlertContent createEntityIdentificationAlertContent(String entityId, SecurityRole.ENTITY_TYPE entityType, C content);

    /**
     * Obtains an AlertResponse object through which AlertResponders will perform updates to the given alert.
     *
     * @param alert the alert that the AlertResponse will update
     * @return an AlertResponse that may be used to update the alert
     */
    AlertResponse getResponse(Alert alert);

    /**
     * Removes an alert from the manager.
     *
     * @param id the ID of the alert to remove
     * @return the alert that was removed
     */
    Alert remove(Alert.ID id);

    /**
     * Returns the latest timestamp for when this manager created/updated an alert
     * @return millis as to when the manager created/updated alerts
     */
    Long getLastUpdatedTime();

    /**
     * Update the time to Now to notify others alerts have been updated
     * Useful if alert data managed by this manager have been updated by an outside source
     * and others need to get notified of the update
     */
    void updateLastUpdatedTime();


}
