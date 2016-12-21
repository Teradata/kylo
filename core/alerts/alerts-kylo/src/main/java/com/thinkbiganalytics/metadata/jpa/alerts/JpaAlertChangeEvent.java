/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.alerts;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert.AlertContentConverter;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert.AlertId;

/**
 *
 * @author Sean Felten
 */
//@Entity
@Embeddable
@Table(name = "KYLO_ALERT_CHANGE")
public class JpaAlertChangeEvent implements AlertChangeEvent, Comparable<AlertChangeEvent> {

    @Embedded
    private AlertId alertId;
    
    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "CHANGE_TIME")
    private DateTime changeTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "STATE")
    private Alert.State state;
    
    @Column(name = "CONTENT")
    @Convert(converter = AlertContentConverter.class)
    private Serializable content;
    
    public JpaAlertChangeEvent() {
        super();
    }
    
    
    public JpaAlertChangeEvent(State state) {
        this(state, null);
    }
    
    public JpaAlertChangeEvent(State state, Serializable content) {
        super();
        this.state = state;
        this.content = content;
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getChangeTime()
     */
    @Override
    public DateTime getChangeTime() {
        return this.changeTime;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getState()
     */
    @Override
    public Alert.State getState() {
        return this.state;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getContent()
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C extends Serializable> C getContent() {
        return (C) this.content;
    }
    
    public AlertId getAlertId() {
        return alertId;
    }
    
    public void setAlertId(AlertId alertId) {
        this.alertId = alertId;
    }

    public void setChangeTime(DateTime changeTime) {
        this.changeTime = changeTime;
    }

    public void setState(Alert.State state) {
        this.state = state;
    }

    public void setContent(Serializable content) {
        this.content = content;
    }


    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(AlertChangeEvent that) {
        int result = that.getChangeTime().compareTo(this.changeTime);
        return result == 0 ? this.state.compareTo(that.getState()) : result;
    }
    
}
