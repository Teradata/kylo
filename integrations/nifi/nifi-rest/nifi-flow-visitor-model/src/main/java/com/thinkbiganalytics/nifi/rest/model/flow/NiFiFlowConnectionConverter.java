package com.thinkbiganalytics.nifi.rest.model.flow;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 1/19/17.
 */
public class NiFiFlowConnectionConverter {


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
