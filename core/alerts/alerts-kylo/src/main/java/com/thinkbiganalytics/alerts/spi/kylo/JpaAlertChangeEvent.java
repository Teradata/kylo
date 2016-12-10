/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.kylo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.spi.kylo.JpaAlert.AlertContentConverter;
import com.thinkbiganalytics.alerts.spi.kylo.JpaAlert.AlertId;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name = "KYLO_ALERT_CHANGE")
public class JpaAlertChangeEvent implements AlertChangeEvent {

    @Embedded
    private AlertId getAlertId;
    
    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "CHANGE_TIME")
    private DateTime changeTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "STATE")
    private Alert.State state;
    
    @Column(name = "COMPARABLES")
    @Convert(converter = AlertContentConverter.class)
    private Serializable content;
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getAlertId()
     */
    @Override
    public Alert.ID getAlertId() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getChangeTime()
     */
    @Override
    public DateTime getChangeTime() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getState()
     */
    @Override
    public Alert.State getState() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getContent()
     */
    @Override
    public <C extends Serializable> C getContent() {
        // TODO Auto-generated method stub
        return null;
    }
}
