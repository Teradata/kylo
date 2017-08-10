package com.thinkbiganalytics.alerts.rest.model;

/*-
 * #%L
 * thinkbig-alerts-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.joda.time.DateTime;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an alert.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert {

    /**
     * The ID of the alert
     */
    private String id;
    /**
     * A unique URI defining the type of alert
     */
    private URI type;
    /**
     * A more detailed descriptor of this type of alert
     */
    private String subtype;
    /**
     * The level of this alert
     */
    private Level level;
    /**
     * Retrieves the current state of this alert
     */
    private State state;
    /**
     * Gets the time when this alert was created
     */
    private DateTime createdTime;
    /**
     * A description of this alert
     */
    private String description;
    /**
     * Indicates that alert responders will be invoked for this alert
     */
    private boolean actionable;
    /**
     * Indicates that this alert will appear in search results
     */
    private boolean cleared;

    /**
     * The content of the alert serialized to a string
     */
    private String content;

    /**
     * the id of the entity, or null if not an entity based alert
     */
    private String entityId;

    /**
     * the type of the entity relating to the entityId, of null of not an entity alert
     */
    private String entityType;

    /**
     * The ordered list of state change events
     */
    private List<AlertChangeEvent> events = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    public boolean isActionable() {
        return actionable;
    }

    public void setActionable(boolean actionable) {
        this.actionable = actionable;
    }

    public List<AlertChangeEvent> getEvents() {
        return events;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * The severity level that alerts may have.
     */
    public enum Level {
        INFO, WARNING, MINOR, MAJOR, CRITICAL, FATAL
    }

    /**
     * The states that this alert may transition through.
     */
    public enum State {
        CREATED, UNHANDLED, IN_PROGRESS, HANDLED
    }
}
