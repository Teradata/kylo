/**
 *
 */
package com.thinkbiganalytics.alerts.spi;

/*-
 * #%L
 * thinkbig-alerts-api
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

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class AlertDescriptor {

    private final URI alertType;
    private final String contentType;
    private final String description;
    private final boolean respondable;
    private final Map<Alert.State, String> stateContentTypes;

    /**
     *
     */
    public AlertDescriptor(URI type, String content, String descr, boolean respondable) {
        this(type, content, descr, respondable, Collections.<Alert.State, String>emptyMap());
    }

    /**
     *
     */
    public AlertDescriptor(URI type, String content, String descr, boolean respondable, Map<Alert.State, String> states) {
        this.alertType = type;
        this.contentType = content;
        this.description = descr;
        this.respondable = respondable;
        this.stateContentTypes = Collections.unmodifiableMap(new HashMap<>(states));
    }

    public URI getAlertType() {
        return alertType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRespondable() {
        return respondable;
    }

    public Map<Alert.State, String> getStateContentTypes() {
        return stateContentTypes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }

        AlertDescriptor that = (AlertDescriptor) obj;

        return Objects.equals(this.alertType, that.alertType) &&
               Objects.equals(this.contentType, that.contentType) &&
               Objects.equals(this.description, that.description) &&
               Objects.equals(this.respondable, that.respondable) &&
               Objects.equals(this.stateContentTypes, that.stateContentTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.alertType, this.contentType, this.description, this.respondable, this.stateContentTypes);
    }
}
