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

import com.thinkbiganalytics.alerts.rest.model.Alert.Level;

import java.io.Serializable;
import java.net.URI;

/**
 * Contains the details necessary to create a new alert.
 */
public class AlertCreateRequest {

    /**
     * A unique URI defining the type of this alert
     */
    private URI type;

    /**
     * a subtype
     */
    private String subtype;

    /**
     * A description of this alert
     */
    private String description;

    /**
     * The level of this alert
     */
    private Level level;

    public AlertCreateRequest() {
    }

    public AlertCreateRequest(URI type, String subtype,String description, Level level) {
        this(type, subtype,description, level, null);
    }

    public AlertCreateRequest(URI type, String subtype,String description, Level level, Serializable content) {
        super();
        this.type = type;
        this.subtype =subtype;
        this.description = description;
        this.level = level;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

}
