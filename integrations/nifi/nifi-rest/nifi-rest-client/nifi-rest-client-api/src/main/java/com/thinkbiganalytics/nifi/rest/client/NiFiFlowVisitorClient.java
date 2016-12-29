package com.thinkbiganalytics.nifi.rest.client;

import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitorCache;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

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

    Set<ProcessorDTO> getProcessorsForFlow(String processGroupId);

    Set<ProcessorDTO> getFailureProcessors(String processGroupId);


}
