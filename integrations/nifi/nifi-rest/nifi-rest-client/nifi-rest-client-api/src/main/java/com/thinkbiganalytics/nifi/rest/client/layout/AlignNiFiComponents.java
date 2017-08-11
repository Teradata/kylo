package com.thinkbiganalytics.nifi.rest.client.layout;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Align components in the Nifi Root Flow and then any components in its immediate child ProcessGroups (aka Categories)
 */
public class AlignNiFiComponents {

    @Inject
    NiFiRestClient niFiRestClient;

    /**
     * flag to indicate alignment is complete
     */
    private boolean aligned = false;
    private Integer alignedProcessGroups = 0;


    /**
     * layout the NiFi components
     */
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
