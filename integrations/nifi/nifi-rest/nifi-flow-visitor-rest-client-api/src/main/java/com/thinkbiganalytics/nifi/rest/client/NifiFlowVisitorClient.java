package com.thinkbiganalytics.nifi.rest.client;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

/**
 * Created by sr186054 on 8/18/16.
 */
public interface NifiFlowVisitorClient {


    ProcessGroupDTO getRootProcessGroup() throws NifiComponentNotFoundException;

    ProcessGroupDTO getProcessGroup(String processGroupId, boolean recursive, boolean verbose) throws NifiComponentNotFoundException;

    ProcessorDTO findProcessorById(String processorId);

    boolean isConnected();

}
