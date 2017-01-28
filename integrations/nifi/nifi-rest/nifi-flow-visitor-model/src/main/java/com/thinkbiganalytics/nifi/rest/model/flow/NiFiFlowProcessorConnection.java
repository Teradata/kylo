package com.thinkbiganalytics.nifi.rest.model.flow;

/*-
 * #%L
 * thinkbig-nifi-flow-visitor-model
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

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the ConnectionDTO id and the related Name
 */
public class NiFiFlowProcessorConnection {

    private String connectionIdentifier;
    private String name;
    private Set<String> selectedRelationships;

    public NiFiFlowProcessorConnection() {
    }

    public NiFiFlowProcessorConnection(String connectionIdentifier, String name, Set<String> selectedRelationships) {
        this.connectionIdentifier = connectionIdentifier;
        this.name = name;
        this.selectedRelationships = selectedRelationships;
    }

    public String getConnectionIdentifier() {
        return connectionIdentifier;
    }

    public void setConnectionIdentifier(String connectionIdentifier) {
        this.connectionIdentifier = connectionIdentifier;
    }

    public Set<String> getSelectedRelationships() {
        if (selectedRelationships == null) {
            this.selectedRelationships = new HashSet<>();
        }
        return selectedRelationships;
    }

    public void setSelectedRelationships(Set<String> selectedRelationships) {
        this.selectedRelationships = selectedRelationships;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NiFiFlowProcessorConnection that = (NiFiFlowProcessorConnection) o;

        return connectionIdentifier.equals(that.connectionIdentifier);

    }

    @Override
    public int hashCode() {
        return connectionIdentifier.hashCode();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NiFiFlowProcessorConnection{");
        sb.append("connectionIdentifier='").append(connectionIdentifier).append('\'');
        sb.append(", selectedRelationships='").append(selectedRelationships).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
