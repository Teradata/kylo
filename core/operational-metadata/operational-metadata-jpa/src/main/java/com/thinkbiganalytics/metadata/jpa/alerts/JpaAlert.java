/**
 *
 */
package com.thinkbiganalytics.metadata.jpa.alerts;

/*-
 * #%L
 * thinkbig-alerts-default
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

import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.EntityAlert;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.alerts.spi.EntityIdentificationAlertContent;
import com.thinkbiganalytics.jpa.AuditTimestampListener;
import com.thinkbiganalytics.jpa.AuditedEntity;
import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.jpa.JsonAttributeConverter;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Implements the JPA-based alert type managed in the Kylo alert store.
 */
@Entity
@Table(name = "KYLO_ALERT")
@EntityListeners(AuditTimestampListener.class)
public class JpaAlert implements EntityAlert, AuditedEntity {

    @EmbeddedId
    private AlertId id;

    @Column(name = "TYPE", length = 128, nullable = false)
    private String typeString;

    @Column(name = "SUB_TYPE", length = 128, nullable = false)
    private String subtype;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "CREATE_TIME")
    @QueryType(PropertyType.COMPARABLE)
    private DateTime createdTime;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "modified_time")
    private DateTime modifiedTime;

    @Column(name = "modified_time", insertable = false,updatable = false)
    private Long modifiedTimeMillis;

    @Column(name = "CREATE_TIME", insertable = false, updatable = false)
    private Long createdTimeMillis;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "LEVEL", nullable = false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATE", nullable = false)
    private Alert.State state;

    @Column(name = "CONTENT")
    @Convert(converter = AlertContentConverter.class)
    private Serializable content;

    @Column(name = "CLEARED", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean cleared = false;

    @ElementCollection(targetClass = JpaAlertChangeEvent.class)
    @CollectionTable(name = "KYLO_ALERT_CHANGE", joinColumns = @JoinColumn(name = "ALERT_ID"))
    @OrderBy("changeTime DESC, state ASC")
    private List<AlertChangeEvent> events = new ArrayList<>();


    @Column(name = "ENTITY_ID")
    private AlertEntityId entityId;

    @Column(name = "ENTITY_TYPE")
    private String entityType;

    @Transient
    private AlertSource source;


    public JpaAlert() {
        super();
    }

    public JpaAlert(URI type, String subtype, Level level, Principal user, String description, Serializable content) {
        this(type, subtype, level, user, description, State.UNHANDLED, content);
    }

    public JpaAlert(URI type, String subtype, Level level, Principal user, String description, State state, Serializable content) {
        this.id = AlertId.create();
        this.typeString = type.toASCIIString();
        this.subtype = subtype;
        this.level = level;
        this.content = content;
        this.createdTime = DateTime.now();
        this.state = state;
        setDescription(description);
        if (content instanceof EntityIdentificationAlertContent) {
            this.entityId = new AlertEntityId(((EntityIdentificationAlertContent) content).getEntityId());
            this.entityType = ((EntityIdentificationAlertContent) content).getEntityType().name();
            this.content = ((EntityIdentificationAlertContent) content).getContent();
        }
        JpaAlertChangeEvent event = new JpaAlertChangeEvent(state, user);
        this.events.add(event);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getId()
     */
    @Override
    public ID getId() {
        return this.id;
    }

    public void setId(AlertId id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getType()
     */
    @Override
    public URI getType() {
        return URI.create(this.typeString);
    }

    public void setType(URI type) {
        this.typeString = type.toASCIIString();
    }

    @Override
    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    /* (non-Javadoc)
             * @see com.thinkbiganalytics.alerts.api.Alert#getDescription()
             */
    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String descr) {
        this.description = descr == null || descr.length() <= 255 ? descr : descr.substring(0, 252) + "...";
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getLevel()
     */
    @Override
    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getCurrentState()
     */
    @Override
    public Alert.State getState() {
        return this.state;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getCreatedTime()
     */
    @Override
    public DateTime getCreatedTime() {
        return this.createdTime;
    }


    public Long getCreatedTimeMillis() {
        return createdTimeMillis;
    }

    /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.Alert#getSource()
         */
    @Override
    public AlertSource getSource() {
        return this.source;
    }

    public void setSource(AlertSource source) {
        this.source = source;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#isCleared()
     */
    @Override
    public boolean isCleared() {
        return this.cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#isActionable()
     */
    @Override
    public boolean isActionable() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getEvents()
     */
    @Override
    public List<AlertChangeEvent> getEvents() {
        return Collections.unmodifiableList(this.events);
    }

    public void setEvents(List<AlertChangeEvent> events) {
        this.events = events;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getContent()
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C extends Serializable> C getContent() {
        return (C) this.content;
    }

    public void setContent(Serializable content) {
        this.content = content;
    }

    public String getTypeString() {
        return typeString;
    }

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public AlertEntityId getEntityId() {
        return entityId;
    }

    public void setEntityId(AlertEntityId entityId) {
        this.entityId = entityId;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public DateTime getModifiedTime() {
        return modifiedTime;
    }


    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Long getModifiedTimeMillis() {
        return modifiedTimeMillis;
    }

    public void addEvent(JpaAlertChangeEvent event) {
        this.state = event.getState();
        this.events.add(event);
    }

    @Embeddable
    public static class AlertId extends BaseJpaId implements Serializable, Alert.ID {

        private static final long serialVersionUID = 1L;

        @Column(name = "id")
        private UUID value;

        public AlertId() {
        }


        public AlertId(Serializable ser) {
            super(ser);
        }

        public static AlertId create() {
            return new AlertId(UUID.randomUUID());
        }

        @Override
        public UUID getUuid() {
            return this.value;
        }

        @Override
        public void setUuid(UUID uuid) {
            this.value = uuid;
        }

    }

    public static class AlertContentConverter extends JsonAttributeConverter<Serializable> {

    }


    @Embeddable
    public static class AlertEntityId extends BaseJpaId implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "entity_id")
        private UUID value;

        public AlertEntityId() {
        }


        public AlertEntityId(Serializable ser) {
            super(ser);
        }

        public static AlertEntityId create() {
            return new AlertEntityId(UUID.randomUUID());
        }

        @Override
        public UUID getUuid() {
            return this.value;
        }

        @Override
        public void setUuid(UUID uuid) {
            this.value = uuid;
        }

    }

}
