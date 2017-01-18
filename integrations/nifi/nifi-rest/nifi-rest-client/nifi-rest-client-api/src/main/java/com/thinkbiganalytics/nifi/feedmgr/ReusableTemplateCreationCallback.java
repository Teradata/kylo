package com.thinkbiganalytics.nifi.feedmgr;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;

/**
 * Created by sr186054 on 1/17/17.
 */
public interface ReusableTemplateCreationCallback {


    void beforeMarkAsRunning(String templateName, ProcessGroupDTO processGroupDTO);


}
