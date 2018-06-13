package com.thinkbiganalytics.nifi.v2.savepoint;
/*-
 * #%L
 * kylo-nifi-core-processors
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

import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointController;
import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointProvider;

import org.apache.nifi.components.PropertyValue;

public class SavepointContextData {

    private final SavepointController controller;

    private final SavepointProvider provider;

    private final PropertyValue savepointId;

    private final String processorId;

    private final long expirationDuration;

    public SavepointContextData(final SavepointController controller, final SavepointProvider provider, final PropertyValue savepointId, final String processorId, final long expirationDuration) {
        this.controller = controller;
        this.provider = provider;
        this.savepointId = savepointId;
        this.processorId = processorId;
        this.expirationDuration = expirationDuration;
    }

    public SavepointController getController() {
        return controller;
    }

    public SavepointProvider getProvider() {
        return provider;
    }

    public PropertyValue getSavepointId() {
        return savepointId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public long getExpirationDuration() {
        return expirationDuration;
    }

}
