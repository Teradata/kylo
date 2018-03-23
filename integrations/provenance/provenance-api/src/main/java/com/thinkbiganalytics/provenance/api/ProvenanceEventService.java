package com.thinkbiganalytics.provenance.api;
/*-
 * #%L
 * kylo-provenance-api
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
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.List;
import java.util.Map;

/**
 * Service for sending custom Provenance events and stats
 */
public interface ProvenanceEventService {


    /**
     * Configure the Service.
     * Allow customers to pass in a Map of properties to configure the implementation
     * @param params Map of configuration parameters
     */
    public void configure(Map<String,String> params);

    /**
     * Send the events off
     * @param events the events to send
     * @throws ProvenanceException
     */
    public void sendEvents(List<ProvenanceEventRecordDTO> events) throws ProvenanceException;

    /**
     * Close the connection and cleanup
     */
    public void closeConnection();

}
