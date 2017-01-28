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

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convert a Nifi Connection {@link ConnectionDTO} to a simplified {@link NifiFlowConnection}
 */
public class NiFiFlowConnectionConverter {


    /**
     * Convert a Nifi Connection {@link ConnectionDTO} to a simplified {@link NifiFlowConnection}
     *
     * @param connectionDTO the Nifi connection
     * @return the converted simplified connection
     */
    public static NifiFlowConnection toNiFiFlowConnection(ConnectionDTO connectionDTO) {
        if (connectionDTO != null) {
            String name = connectionDTO.getName();
            if (StringUtils.isBlank(name)) {
                Set<String> relationships = connectionDTO.getSelectedRelationships();
                if (relationships != null) {
                    name = relationships.stream().collect(Collectors.joining(","));
                }
            }
            NifiFlowConnection
                connection =
                new NifiFlowConnection(connectionDTO.getId(), name, connectionDTO.getSource() != null ? connectionDTO.getSource().getId() : null,
                                       connectionDTO.getDestination() != null ? connectionDTO.getDestination().getId() : null);
            return connection;
        }
        return null;
    }

}
