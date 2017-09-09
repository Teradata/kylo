package com.thinkbiganalytics.nifi.rest.client;

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

import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitorCache;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * REST client to get a NiFi flow as a graph of connected processors
 */
public interface NiFiFlowVisitorClient {


    NifiVisitableProcessGroup getFlowOrder(String processGroupId, NifiConnectionOrderVisitorCache cache);

    NifiVisitableProcessGroup getFlowOrder(ProcessGroupDTO processGroupEntity, NifiConnectionOrderVisitorCache cache);

    NifiFlowProcessGroup getFeedFlow(String processGroupId);


    NifiFlowProcessGroup getFeedFlowForCategoryAndFeed(String categoryAndFeedName);

    List<NifiFlowProcessGroup> getFeedFlows();

    List<NifiFlowProcessGroup> getFeedFlowsWithCache(NifiConnectionOrderVisitorCache cache);

    List<NifiFlowProcessGroup> getFeedFlows(Collection<String> feedNames);

    Set<ProcessorDTO> getProcessorsForFlow(String processGroupId);

    /**
     * Walks a Template and returns the graph of connected processors
     */
    NifiFlowProcessGroup getTemplateFeedFlow(TemplateDTO template);


    /**
     * Walks a Template and returns the graph of connected processors
     */
    NifiFlowProcessGroup getTemplateFeedFlow(String templateId);


}
