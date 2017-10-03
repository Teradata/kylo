/**
 *
 */
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

import com.thinkbiganalytics.alerts.spi.AlertSource;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

/**
 * This type defines the basic alert abstraction. Only the basic properties of an alert are defined
 * here.  This kind of object also acts as a container for the more type-specific details returned from
 * its content property.
 */
public interface Alert {

    /**
     * @return the ID of the alert
     */
    ID getId();

    /**
     * A unique URI defining the type of alert this is.  URIs allow for a more hierarchical
     * name space defining the type.
     *
     * @return the unique type URI
     */
    URI getType();

    /**
     * A separate descriptor further defining the type of this alert
     * @return the descriptor defining the type of the alert
     */
    String getSubtype();

    /**
     * @return a description of this alert
     */
    String getDescription();

    /**
     * @return the level of this alert
     */
    Level getLevel();

    /**
     * Gets the time when this alert was created.  Note that it is usually the same
     * time as the change time of the oldest event in the change event list.
     *
     * @return the time this alert was created.
     */
    DateTime getCreatedTime();

    /**
     * Gets the time when this alert was modified/updated.
     * This usually is the same as the latest change event.
     * Note if AlertChanges occur that dont modify the state of the Alert this may not reflect the latest change event.
     *
     * @return the time this alert was modified.
     */
    DateTime getModifiedTime();

    /**
     * @return the alert source that produced this alert
     */
    AlertSource getSource();

    /**
     * AlertResponders will only be invoked for actionable alerts.  Alerts whose
     * state may be changed by AlertResponders should have this method return true.
     *
     * @return whether this alert is actionable
     */
    boolean isActionable();

    /**
     * Retrieves the current state of this alert.  If this alert supports events then this is
     * the same state of the latest change event.
     *
     * @return the current state of this alert
     */
    State getState();

    /**
     * Indicates whether this alert is considered cleared or not.  Normally cleared alerts do not
     * appear in regular search results.
     */
    boolean isCleared();

    /**
     * Gets ordered list of state change events showing the state transitions this alert
     * has gone through.
     *
     * @return the list of state changes
     */
    List<AlertChangeEvent> getEvents();

    /**
     * The payload containing type-specific data for this alert.  The kind of object
     * returned from this method is defined by the type of alert this is.
     *
     * @return a particular content object based this alert type
     */
    <C extends Serializable> C getContent();

    /**
     * The states that this alert may transition through, listed within the change events.
     * For non-actionable alerts this state will never transition beyond created.
     */
    enum State {
        CREATED, UNHANDLED, IN_PROGRESS, HANDLED
    }

    /**
     * The severity level that alerts may have
     */
    enum Level {
        INFO, WARNING, MINOR, MAJOR, CRITICAL, FATAL
    }

    /**
     * The opaque identifier of this event
     */
    interface ID extends Serializable {

    }
}
