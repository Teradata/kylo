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

    /**
     * flag to indicate alignment is complete
     */
    private boolean aligned = false;
    private Integer alignedProcessGroups = 0;


    public void autoLayout() {

        AlignProcessGroupComponents alignProcessGroupComponents = new AlignProcessGroupComponents(niFiRestClient, "root", new AlignComponentsConfig());
        ProcessGroupDTO root = alignProcessGroupComponents.autoLayout();
        //align all of roots children
        aligned = alignProcessGroupComponents.isAligned();
        root.getContents().getProcessGroups().stream().forEach(processGroupDTO -> {
            AlignProcessGroupComponents categoryAlign = new AlignProcessGroupComponents(niFiRestClient, processGroupDTO.getId(), new AlignComponentsConfig());
            ProcessGroupDTO groupDTO = categoryAlign.autoLayout();
            if (categoryAlign.isAligned() && groupDTO != null && groupDTO.getContents() != null && groupDTO.getContents().getProcessGroups() != null) {
                alignedProcessGroups += groupDTO.getContents().getProcessGroups().size();
            }
            aligned &= categoryAlign.isAligned();

        });


    }

    public void setNiFiRestClient(NiFiRestClient niFiRestClient) {
        this.niFiRestClient = niFiRestClient;
    }

    public boolean isAligned() {
        return aligned;
    }

    public Integer getAlignedProcessGroups() {
        return alignedProcessGroups;
    }
}
