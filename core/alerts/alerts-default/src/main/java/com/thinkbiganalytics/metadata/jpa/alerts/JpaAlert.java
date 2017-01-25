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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.jpa.JsonAttributeConverter;

/**
 * Implements the JPA-based alert type managed in the Kylo alert store.
 * 
 * @author Sean Felten
 */
@Entity
@Table(name = "KYLO_ALERT")
public class JpaAlert implements Alert {
    
    @EmbeddedId
    private AlertId id;
    
    @Column(name = "TYPE", length = 128, nullable = false)
    private String typeString;
    
    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "CREATE_TIME")
    private DateTime createdTime;
    
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
    
    @Column(name="CLEARED", length=1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean cleared = false;
    
    @ElementCollection(targetClass=JpaAlertChangeEvent.class)
    @CollectionTable(name="KYLO_ALERT_CHANGE", joinColumns=@JoinColumn(name="ALERT_ID"))
    @OrderBy("changeTime DESC, state ASC")
    private List<AlertChangeEvent> events = new ArrayList<>();
    
    @Transient
    private AlertSource source;
    
    public JpaAlert() {
        super();
    }
    
    public JpaAlert(URI type, Level level, Principal user, String description, Serializable content) {
        this(type, level, user, description, State.UNHANDLED, content);
    }

    public JpaAlert(URI type, Level level, Principal user, String description, State state, Serializable content) {
        this.id = AlertId.create();
        this.typeString = type.toASCIIString();
        this.level = level;
        this.content = content;
        this.createdTime = DateTime.now();
        this.state = state;
        setDescription(description);
        
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

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getType()
     */
    @Override
    public URI getType() {
        return URI.create(this.typeString);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getLevel()
     */
    @Override
    public Level getLevel() {
        return this.level;
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
//        return this.events.get(0).getChangeTime();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getSource()
     */
    @Override
    public AlertSource getSource() {
        return this.source;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#isCleared()
     */
    @Override
    public boolean isCleared() {
        return this.cleared;
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

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getContent()
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C extends Serializable> C getContent() {
        return (C) this.content;
    }
    
    public String getTypeString() {
        return typeString;
    }
    
    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }
    
    public void setType(URI type) {
        this.typeString = type.toASCIIString();
    }

    public void setId(AlertId id) {
        this.id = id;
    }

    public void setDescription(String descr) {
        this.description = descr == null || descr.length() <= 255 ? descr : descr.substring(0, 252) + "...";
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setEvents(List<AlertChangeEvent> events) {
        this.events = events;
    }
    
    public void setContent(Serializable content) {
        this.content = content;
    }
    
    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    public void setSource(AlertSource source) {
        this.source = source;
    }

    public void addEvent(JpaAlertChangeEvent event) {
        this.state = event.getState();
        this.events.add(event);
    }

    @Embeddable
    public static class AlertId extends BaseJpaId implements Serializable, Alert.ID {

        private static final long serialVersionUID = 1L;

        @Column(name = "id", columnDefinition = "binary(16)")
        private UUID value;

        public static AlertId create() {
            return new AlertId(UUID.randomUUID());
        }


        public AlertId() {}

        public AlertId(Serializable ser) {
            super(ser);
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

    public static class AlertContentConverter extends JsonAttributeConverter<Serializable> { }

}
