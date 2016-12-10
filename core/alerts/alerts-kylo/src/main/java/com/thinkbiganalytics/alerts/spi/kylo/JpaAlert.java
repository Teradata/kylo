/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.kylo;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.jpa.JsonAttributeConverter;

/**
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
    
    @Column(name = "DESCRIPTION", length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10, nullable = false)
    private Level level;
    
    @Column(name = "ACTIONABLE", nullable = false)
    private boolean actionable;
    
    @OneToMany(targetEntity = JpaAlertChangeEvent.class, mappedBy = "alertId", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<? extends AlertChangeEvent> events;
    
    @Column(name = "COMPARABLES")
    @Convert(converter = AlertContentConverter.class)
    private Serializable content;
    
    @Transient
    private AlertSource source;

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
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getSource()
     */
    @Override
    public AlertSource getSource() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#isActionable()
     */
    @Override
    public boolean isActionable() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getEvents()
     */
    @Override
    public List<? extends AlertChangeEvent> getEvents() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.Alert#getContent()
     */
    @Override
    public <C extends Serializable> C getContent() {
        // TODO Auto-generated method stub
        return null;
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
