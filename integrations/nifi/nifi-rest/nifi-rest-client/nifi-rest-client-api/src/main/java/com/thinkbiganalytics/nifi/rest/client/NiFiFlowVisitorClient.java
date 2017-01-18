package com.thinkbiganalytics.nifi.rest.client;

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
 * Created by sr186054 on 8/18/16.
 */
public interface NiFiFlowVisitorClient {


    NifiVisitableProcessGroup getFlowOrder(String processGroupId, NifiConnectionOrderVisitorCache cache);

    NifiVisitableProcessGroup getFlowOrder(ProcessGroupDTO processGroupEntity, NifiConnectionOrderVisitorCache cache);

    NifiFlowProcessGroup getFeedFlow(String processGroupId);


    NifiFlowProcessGroup getFeedFlowForCategoryAndFeed(String categoryAndFeedName);

    List<NifiFlowProcessGroup> getFeedFlows();

    List<NifiFlowProcessGroup> getFeedFlows(Collection<String> feedNames);

    Set<ProcessorDTO> getProcessorsForFlow(String processGroupId);

    Set<ProcessorDTO> getFailureProcessors(String processGroupId);

    /**
     * Walks a Template and returns the graph of connected processors
     * @param template
     * @return
     */
    NifiFlowProcessGroup getTemplateFeedFlow(TemplateDTO template);


    /**
     * Walks a Template and returns the graph of connected processors
     * @param templateId
     * @return
     */
    NifiFlowProcessGroup getTemplateFeedFlow(String templateId);




}
