/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.alerts;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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
    private URI type;
    
    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "CHANGE_TIME")
    private DateTime createdTime;
    
    @Column(name = "DESCRIPTION", length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10, nullable = false)
    private Level level;
    
    @Column(name = "CONTENT")
    @Convert(converter = AlertContentConverter.class)
    private Serializable content;
    
//    @OneToMany(targetEntity = JpaAlertChangeEvent.class, mappedBy = "alertId", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @ElementCollection(targetClass=JpaAlertChangeEvent.class)
    @CollectionTable(name="KYLO_ALERT_CHANGE", joinColumns=@JoinColumn(name="ALERT_ID"))
    @OrderBy("changeTime DESC, state ASC")
    private List<AlertChangeEvent> events = new ArrayList<>();
    
    @Transient
    private AlertSource source;
    
    public JpaAlert() {
        super();
    }
    
    public JpaAlert(URI type, Level level, String description, Serializable content) {
        this(type, level, description, State.UNHANDLED, content);
    }

    public JpaAlert(URI type, Level level, String description, State state, Serializable content) {
        this.id = AlertId.create();
        this.type = type;
        this.level = level;
        this.description = description;
        this.content = content;
        this.createdTime = DateTime.now();
        
        JpaAlertChangeEvent event = new JpaAlertChangeEvent(state);
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
        return this.type;
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
        return this.events;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getContent()
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C extends Serializable> C getContent() {
        return (C) this.content;
    }

    public void setId(AlertId id) {
        this.id = id;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setSource(AlertSource source) {
        this.source = source;
    }

    @Embeddable
    public static class AlertId extends BaseJpaId implements Serializable, Alert.ID {

        private static final long serialVersionUID = 1L;

        @Column(name = "id", columnDefinition = "binary(16)")
        private UUID uuid;

        public static AlertId create() {
            return new AlertId(UUID.randomUUID());
        }


        public AlertId() {}

        public AlertId(Serializable ser) {
            super(ser);
        }

        @Override
        public UUID getUuid() {
            return this.uuid;
        }

        @Override
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
    }

    public static class AlertContentConverter extends JsonAttributeConverter<Serializable> { }

}
