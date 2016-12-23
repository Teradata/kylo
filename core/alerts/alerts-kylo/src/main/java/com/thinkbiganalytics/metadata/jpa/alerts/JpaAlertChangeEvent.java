/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.alerts;

import java.io.Serializable;
import java.security.Principal;

import javax.persistence.AttributeConverter;
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
import com.thinkbiganalytics.security.UsernamePrincipal;

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

    @Convert(converter = UsernamePrincipalConverter.class)
    @Column(name = "USER", columnDefinition = "varchar(128)")
    private UsernamePrincipal user;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATE", nullable = false)
    private Alert.State state;
    
    @Column(name = "DESCRIPTION", length = 255)
    private String description;
    
    @Column(name = "CONTENT")
    @Convert(converter = AlertContentConverter.class)
    private Serializable content;
    
    public JpaAlertChangeEvent() {
        super();
    }
    
    
    public JpaAlertChangeEvent(State state, Principal user) {
        this(state, user, null, null);
    }
    
    public JpaAlertChangeEvent(State state, Principal user, String descr) {
        this(state, user, descr, null);
    }
    
    public JpaAlertChangeEvent(State state, Principal user, String descr, Serializable content) {
        super();
        this.state = state;
        this.description = descr;
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
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getUser()
     */
    @Override
    public Principal getUser() {
        return this.user;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getState()
     */
    @Override
    public Alert.State getState() {
        return this.state;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
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
    
    public void setUser(UsernamePrincipal user) {
        this.user = user;
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

    
    public static class UsernamePrincipalConverter implements AttributeConverter<Principal, String> {
        @Override
        public String convertToDatabaseColumn(Principal principal) {
            return principal.getName();
        }

        @Override
        public Principal convertToEntityAttribute(String dbData) {
            return new UsernamePrincipal(dbData);
        }
    }

}
