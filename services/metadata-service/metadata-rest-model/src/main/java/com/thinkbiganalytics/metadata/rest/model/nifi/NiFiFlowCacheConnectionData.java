package com.thinkbiganalytics.metadata.rest.model.nifi;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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
 */
public class NiFiFlowCacheConnectionData {

    private String connectionIdentifier;
    private String name;
    private String sourceIdentifier;
    private String destinationIdentifier;

    public NiFiFlowCacheConnectionData() {

    }

    public NiFiFlowCacheConnectionData(String connectionIdentifier, String name, String sourceIdentifier, String destinationIdentifier) {
        this.connectionIdentifier = connectionIdentifier;
        this.name = name;
        this.sourceIdentifier = sourceIdentifier;
        this.destinationIdentifier = destinationIdentifier;
    }

    public String getConnectionIdentifier() {
        return connectionIdentifier;
    }

    public void setConnectionIdentifier(String connectionIdentifier) {
        this.connectionIdentifier = connectionIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    public String getDestinationIdentifier() {
        return destinationIdentifier;
    }

    public void setDestinationIdentifier(String destinationIdentifier) {
        this.destinationIdentifier = destinationIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NiFiFlowCacheConnectionData that = (NiFiFlowCacheConnectionData) o;

        return connectionIdentifier != null ? connectionIdentifier.equals(that.connectionIdentifier) : that.connectionIdentifier == null;
    }

    @Override
    public int hashCode() {
        return connectionIdentifier != null ? connectionIdentifier.hashCode() : 0;
    }
}
