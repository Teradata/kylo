package com.thinkbiganalytics.metadata.api.datasource;

import com.thinkbiganalytics.metadata.api.SystemEntity;

/*-
 * #%L
 * thinkbig-metadata-api
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

import java.io.Serializable;
import java.util.Set;

/**
 */
public interface DatasourceDefinition extends SystemEntity {

    ID getId();

    ;

    ConnectionType getConnectionType();

    void setConnectionType(ConnectionType type);

    String getDatasourceType();

    void setDatasourceType(String dsType);

    String getProcessorType();

    void setProcessorType(String processorType);

    Set<String> getDatasourcePropertyKeys();

    void setDatasourcePropertyKeys(Set<String> properties);

    String getIdentityString();

    void setIdentityString(String identityString);

    public enum ConnectionType {
        SOURCE, DESTINATION;
    }

    interface ID extends Serializable {

    }


}
