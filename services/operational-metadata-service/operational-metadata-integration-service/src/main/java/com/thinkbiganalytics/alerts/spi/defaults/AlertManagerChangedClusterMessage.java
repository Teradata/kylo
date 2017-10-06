package com.thinkbiganalytics.alerts.spi.defaults;
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
 *
 */
import java.io.Serializable;

/**
 * Message sent to other kylo services in the cluster to notify them if the alert manager updated/added an alert
 */
public class AlertManagerChangedClusterMessage implements Serializable{

    private static final long serialVersionUID = 1L;

    public static final String TYPE = "AlertManagerChangedClusterMessage";

    private Long updateTime;

    public AlertManagerChangedClusterMessage() {
    }

    public AlertManagerChangedClusterMessage(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
