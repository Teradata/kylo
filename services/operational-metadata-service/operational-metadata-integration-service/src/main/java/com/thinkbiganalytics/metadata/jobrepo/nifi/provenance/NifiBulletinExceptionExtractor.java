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

import com.google.common.collect.ImmutableList;
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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * process NiFi Bulletins
 */

public class NifiBulletinExceptionExtractor {

    private static final Logger log = LoggerFactory.getLogger(NifiBulletinExceptionExtractor.class);
    private static List<String> bulletinErrorLevels = ImmutableList.of("WARN", "ERROR");
    @Autowired
    private LegacyNifiRestClient nifiRestClient;

    /**
     * Extracts the identifier for the flow file from a bulletin message
     *
     * @param message The bulleting message
     * @return A UUID as string
     */
    public String getFlowFileUUIDFromBulletinMessage(String message) {
        return StringUtils.substringBetween(message, "StandardFlowFileRecord[uuid=", ",");
    }

    /**
     * associates the error message from Nifi to the step given
     *
     * @param stepExecution The step execution object
     * @param flowFileId    The UUID of the flow file to extract the error message from
     * @param componentId   The ID of the component posting the bulletin
     * @return true if error messages were added to stepExecution, false otherwise
     * @throws NifiConnectionException if cannot query Nifi
     */
    public boolean addErrorMessagesToStep(BatchStepExecution stepExecution, String flowFileId, String componentId) throws NifiConnectionException {
        if (StringUtils.isNotBlank(flowFileId) && StringUtils.isNotBlank(componentId)) {
            List<BulletinDTO> bulletins = getProcessorBulletinsForComponentInFlowFile(flowFileId, componentId);
            if (bulletins != null) {
                String
                    msg =
                    bulletins.stream().filter(bulletinDTO -> bulletinErrorLevels.contains(bulletinDTO.getLevel().toUpperCase())).map(BulletinDTO::getMessage).collect(Collectors.joining(", "));
                String exitMsg = StringUtils.isBlank(stepExecution.getExitMessage()) ? "" : stepExecution.getExitMessage() + "\n";
                stepExecution.setExitMessage(exitMsg + msg);
                return true;
            }
        }
        return false;
    }

    /**
     * queries for bulletins from component, in the flow file
     *
     * @param flowFileIds The collection UUID of the flow file to extract the error message from
     * @return a list of bulletin objects that were posted by the component to the flow file
     * @throws NifiConnectionException if cannot query Nifi
     */
    public List<BulletinDTO> getErrorBulletinsForFlowFiles(Collection<String> flowFileIds)throws NifiConnectionException {
        return getErrorBulletinsForFlowFiles(flowFileIds,-1L);
    }

    /**
     * queries for bulletins from component, in the flow file
     *
     * @param flowFileIds The collection UUID of the flow file to extract the error message from
     * @return a list of bulletin objects that were posted by the component to the flow file
     * @throws NifiConnectionException if cannot query Nifi
     */
    public List<BulletinDTO> getErrorBulletinsForFlowFiles(Collection<String> flowFileIds, Long afterId) throws NifiConnectionException {
        List<BulletinDTO> bulletins;
        try {
            String regexPattern = flowFileIds.stream().collect(Collectors.joining("|"));
            if(afterId != null && afterId !=-1L) {
                bulletins = nifiRestClient.getBulletinsMatchingMessage(regexPattern, afterId);
            }
            else {
                bulletins = nifiRestClient.getBulletinsMatchingMessage(regexPattern);
            }
            log.info("Query for {} bulletins returned {} results ", regexPattern, bulletins.size());
            if (bulletins != null && !bulletins.isEmpty()) {
                bulletins = bulletins.stream().filter(bulletinDTO -> bulletinErrorLevels.contains(bulletinDTO.getLevel().toUpperCase())).collect(Collectors.toList());
            }

            return bulletins;
        } catch (NifiClientRuntimeException e) {
            if (e instanceof NifiConnectionException) {
                throw e;
            } else {
                log.error("Error getProcessorBulletinsForFlowFiles {}, {}",flowFileIds,e.getMessage());
            }
        }
        return null;
    }

    /**
     * queries for bulletins from component, in the flow file
     *
     * @param processorIds The collection UUID of the flow file to extract the error message from
     * @return a list of bulletin objects that were posted by the component to the flow file
     * @throws NifiConnectionException if cannot query Nifi
     */
    public List<BulletinDTO> getErrorBulletinsForProcessorId(Collection<String> processorIds) throws NifiConnectionException {
        return getErrorBulletinsForProcessorId(processorIds,null);
    }

    /**
     * queries for bulletins from component, in the flow file
     *
     * @param processorIds The collection UUID of the flow file to extract the error message from
     * @return a list of bulletin objects that were posted by the component to the flow file
     * @throws NifiConnectionException if cannot query Nifi
     */
    public List<BulletinDTO> getErrorBulletinsForProcessorId(Collection<String> processorIds, Long afterId) throws NifiConnectionException {
        List<BulletinDTO> bulletins;
        try {
            String regexPattern = processorIds.stream().collect(Collectors.joining("|"));
            if(afterId != null && afterId !=-1L) {
                bulletins = nifiRestClient.getBulletinsMatchingSource(regexPattern, afterId);
            }
            else {
                bulletins = nifiRestClient.getBulletinsMatchingSource(regexPattern,null);
            }

            bulletins = nifiRestClient.getProcessorBulletins(regexPattern);
            log.info("Query for {} bulletins returned {} results ", regexPattern, bulletins.size());
            if (bulletins != null && !bulletins.isEmpty()) {
                bulletins = bulletins.stream().filter(bulletinDTO -> bulletinErrorLevels.contains(bulletinDTO.getLevel().toUpperCase())).collect(Collectors.toList());
            }

            return bulletins;
        } catch (NifiClientRuntimeException e) {
            if (e instanceof NifiConnectionException) {
                throw e;
            } else {
                log.error("Error getErrorBulletinsForProcessorId {} ,{}  ", processorIds,e.getMessage());
            }
        }
        return null;
    }


    /**
     * queries for bulletins from component, in the flow file
     *
     * @param flowFileId  The UUID of the flow file to extract the error message from
     * @param componentId The ID of the component posting the bulletin
     * @return a list of bulletin objects that were posted by the component to the flow file
     * @throws NifiConnectionException if cannot query Nifi
     */
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
