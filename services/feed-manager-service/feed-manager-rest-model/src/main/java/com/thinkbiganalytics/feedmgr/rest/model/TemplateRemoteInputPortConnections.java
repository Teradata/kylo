package com.thinkbiganalytics.feedmgr.rest.model;

import org.apache.nifi.web.api.dto.ConnectionDTO;

import java.util.List;
import java.util.Set;

public class TemplateRemoteInputPortConnections {

        List<ConnectionDTO> existingRemoteConnectionsToTemplate;
        Set<String> existingRemoteInputPortNames;

        public TemplateRemoteInputPortConnections(List < ConnectionDTO > existingRemoteConnectionsToTemplate,
                                                         Set<String> existingRemoteInputPortNames) {
            this.existingRemoteConnectionsToTemplate = existingRemoteConnectionsToTemplate;
            this.existingRemoteInputPortNames = existingRemoteInputPortNames;
        }

        public List<ConnectionDTO> getExistingRemoteConnectionsToTemplate() {
            return existingRemoteConnectionsToTemplate;
        }

        public Set<String> getExistingRemoteInputPortNames() {
            return existingRemoteInputPortNames;
        }


}
