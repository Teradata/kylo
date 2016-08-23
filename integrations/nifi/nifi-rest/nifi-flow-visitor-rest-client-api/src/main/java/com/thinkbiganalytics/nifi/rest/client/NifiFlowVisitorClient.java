package com.thinkbiganalytics.nifi.rest.client;

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;

/**
 * Created by sr186054 on 8/18/16.
 */
public interface NifiFlowVisitorClient {


    ProcessGroupEntity getRootProcessGroup() throws NifiComponentNotFoundException;

    ProcessGroupEntity getProcessGroup(String processGroupId, boolean recursive, boolean verbose) throws NifiComponentNotFoundException;

    ProcessorDTO findProcessorById(String processorId);

}
