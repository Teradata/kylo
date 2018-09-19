package com.thinkbiganalytics.kylo.catalog.rest.model;

/*-
 * #%L
 * kylo-catalog-model
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Maps data set properties to NiFi properties
 */
public class ConnectorPluginNiFiProperties {

    /**
     * List of NiFi processor types matching this connector
     */
    private List<String> processorTypes;

    /**
     * Map of data set properties to NiFi properties
     */
    private Map<String, String> properties;

    public ConnectorPluginNiFiProperties() {
    }

    public ConnectorPluginNiFiProperties(@Nonnull final ConnectorPluginNiFiProperties other) {
        processorTypes = (other.processorTypes != null) ? new ArrayList<>(other.processorTypes) : null;
        properties = (other.properties != null) ? new HashMap<>(other.properties) : null;
    }

    public List<String> getProcessorTypes() {
        return processorTypes;
    }

    public void setProcessorTypes(List<String> processorTypes) {
        this.processorTypes = processorTypes;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
