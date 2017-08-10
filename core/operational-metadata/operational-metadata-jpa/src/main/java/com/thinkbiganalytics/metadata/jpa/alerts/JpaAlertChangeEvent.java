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

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert.AlertContentConverter;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.security.Principal;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 *
 */
@Embeddable
@Table(name = "KYLO_ALERT_CHANGE")
public class JpaAlertChangeEvent implements AlertChangeEvent, Comparable<AlertChangeEvent> {

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "CHANGE_TIME")
    private DateTime changeTime;

    @Column(name = "USER_NAME", columnDefinition = "varchar(128)")
    private String username;

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
        this.changeTime = DateTime.now();
        this.state = state;
        this.content = content;
        this.username = user != null ? user.getName() : null;
        setDescription(descr);
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getChangeTime()
     */
    @Override
    public DateTime getChangeTime() {
        return this.changeTime;
    }

    public void setChangeTime(DateTime changeTime) {
        this.changeTime = changeTime;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getUser()
     */
    @Override
    public Principal getUser() {
        return new UsernamePrincipal(this.username);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getState()
     */
    @Override
    public Alert.State getState() {
        return this.state;
    }

    public void setState(Alert.State state) {
        this.state = state;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String descr) {
        this.description = descr == null || descr.length() <= 255 ? descr : descr.substring(0, 252) + "...";
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertChangeEvent#getContent()
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C extends Serializable> C getContent() {
        return (C) this.content;
    }

    public void setContent(Serializable content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String user) {
        this.username = user;
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
