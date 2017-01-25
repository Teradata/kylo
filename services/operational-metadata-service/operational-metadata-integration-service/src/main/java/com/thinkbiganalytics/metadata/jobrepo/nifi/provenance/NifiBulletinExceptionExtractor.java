package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;

/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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

import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiConnectionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 10/24/16.
 */
@Component
public class NifiBulletinExceptionExtractor {

    private static final Logger log = LoggerFactory.getLogger(NifiBulletinExceptionExtractor.class);

    @Autowired
    private LegacyNifiRestClient nifiRestClient;

    public String getFlowFileUUIDFromBulletinMessage(String message) {
        return StringUtils.substringBetween(message, "StandardFlowFileRecord[uuid=", ",");
    }

    public boolean addErrorMessagesToStep(BatchStepExecution stepExecution, String flowFileId, String componentId) throws NifiConnectionException {
        if (StringUtils.isNotBlank(flowFileId) && StringUtils.isNotBlank(componentId)) {
            List<BulletinDTO> bulletins = getProcessorBulletinsForComponentInFlowFile(flowFileId, componentId);
            if (bulletins != null) {
                //todo filter on level to get just ERRORS, Warns, fatals?
                String msg = bulletins.stream().map(BulletinDTO::getMessage).collect(Collectors.joining(", "));
                String exitMsg = StringUtils.isBlank(stepExecution.getExitMessage()) ? "" : stepExecution.getExitMessage() + "\n";
                stepExecution.setExitMessage(exitMsg + msg);
                return true;
            }
        }
        return false;
    }


    public List<BulletinDTO> getProcessorBulletinsForComponentInFlowFile(String flowFileId, String componentId) throws NifiConnectionException {
        List<BulletinDTO> bulletins;
        try {
            bulletins = nifiRestClient.getProcessorBulletins(componentId);
            return bulletins.stream().filter(bulletin -> flowFileId.equalsIgnoreCase(getFlowFileUUIDFromBulletinMessage(bulletin.getMessage()))).collect(Collectors.toList());
        } catch (NifiClientRuntimeException e) {
            if (e instanceof NifiConnectionException) {
                throw e;
            } else {
                log.error("Error getProcessorBulletinsForComponentInFlowFile for flowfile {} and componentId: {} ", flowFileId, componentId);
            }
        }
        return null;
    }

}
