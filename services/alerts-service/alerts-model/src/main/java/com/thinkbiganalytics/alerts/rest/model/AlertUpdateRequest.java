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

/**
 * Contains the details necessary to update the state of an alert.
 */
public class AlertUpdateRequest {

    /**
     * New state for the alert
     */
    private Alert.State state;

    /**
     * A description of the change
     */
    private String description;

    /**
     * Indicates that the alert should not appear in search results
     */
    private boolean clear = false;

    private boolean unclear = false;

    public Alert.State getState() {
        return state;
    }

    public void setState(Alert.State state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public boolean isUnclear(){
        return this.unclear;
    }
    public void setUnclear(boolean unclear) {
        this.unclear = unclear;
    }

}
