package com.thinkbiganalytics.nifi.rest.client.layout;

import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Align components in the Nifi Root Flow and then any components in its immediate child ProcessGroups (aka Categories) Created by sr186054 on 11/9/16.
 */
@Component
public class AlignNiFiComponents {

    @Inject
    NiFiRestClient niFiRestClient;


    public void autoLayout() {

        AlignProcessGroupComponents alignProcessGroupComponents = new AlignProcessGroupComponents(niFiRestClient, "root", new AlignComponentsConfig());
        ProcessGroupDTO root = alignProcessGroupComponents.autoLayout();
        //align all of roots children
        root.getContents().getProcessGroups().stream().forEach(processGroupDTO -> {
            AlignProcessGroupComponents categoryAlign = new AlignProcessGroupComponents(niFiRestClient, processGroupDTO.getId(), new AlignComponentsConfig());
            categoryAlign.autoLayout();
        });

    }

    public void setNiFiRestClient(NiFiRestClient niFiRestClient) {
        this.niFiRestClient = niFiRestClient;
    }
}
