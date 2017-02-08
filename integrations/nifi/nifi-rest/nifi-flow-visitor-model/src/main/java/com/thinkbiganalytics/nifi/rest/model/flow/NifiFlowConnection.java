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

import org.apache.nifi.web.api.dto.ConnectionDTO;

/**
 * Representings a simplified version of the NiFi {@link org.apache.nifi.web.api.dto.ConnectionDTO}
 */
public class NifiFlowConnection {

    /**
     * The connection Id {@link ConnectionDTO#getId()}
     */
    private String connectionIdentifier;
    /**
     * the connection name, either the display name {@link ConnectionDTO#getName()}, if available or the relationship name {@link ConnectionDTO#getSelectedRelationships()}
     */
    private String name;
    /**
     * the id for the source item {@link ConnectionDTO#getSource()#getSourceIdentifier()}
     */
    private String sourceIdentifier;
    /**
     * the id for the destination item {@link ConnectionDTO#getDestination()#getDestinationIdentifier()}
     */
    private String destinationIdentifier;

    public NifiFlowConnection() {

    }

    public NifiFlowConnection(String connectionIdentifier, String name, String sourceIdentifier, String destinationIdentifier) {
        this.connectionIdentifier = connectionIdentifier;
        this.name = name;
        this.sourceIdentifier = sourceIdentifier;
        this.destinationIdentifier = destinationIdentifier;
    }

    /**
     * Return the id for this connection
     *
     * @return the id for this connection
     */
    public String getConnectionIdentifier() {
        return connectionIdentifier;
    }

    /**
     * set the connection id
     */
    public void setConnectionIdentifier(String connectionIdentifier) {
        this.connectionIdentifier = connectionIdentifier;
    }

    /**
     * Return the connection name, either the display name {@link ConnectionDTO#getName()}, if available or the relationship name {@link ConnectionDTO#getSelectedRelationships()}
     *
     * @return the connection name
     */
    public String getName() {
        return name;
    }

    /**
     * set the name of the connection
     *
     * @param name the name of the connection
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the id for the source item {@link ConnectionDTO#getSource()#getSourceIdentifier()}
     *
     * @return the id for the source item {@link ConnectionDTO#getSource()#getSourceIdentifier()}
     */
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    /**
     * Return the id for the destination item {@link ConnectionDTO#getDestination()#getDestinationIdentifier()}
     *
     * @return the id for the destination item {@link ConnectionDTO#getDestination()#getDestinationIdentifier()}
     */
    public String getDestinationIdentifier() {
        return destinationIdentifier;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NifiFlowConnection that = (NifiFlowConnection) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (sourceIdentifier != null ? !sourceIdentifier.equals(that.sourceIdentifier) : that.sourceIdentifier != null) {
            return false;
        }
        return destinationIdentifier != null ? destinationIdentifier.equals(that.destinationIdentifier) : that.destinationIdentifier == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (sourceIdentifier != null ? sourceIdentifier.hashCode() : 0);
        result = 31 * result + (destinationIdentifier != null ? destinationIdentifier.hashCode() : 0);
        return result;
    }
}
