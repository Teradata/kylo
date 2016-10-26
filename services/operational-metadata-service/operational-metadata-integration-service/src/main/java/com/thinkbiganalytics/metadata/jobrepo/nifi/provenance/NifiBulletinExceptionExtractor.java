package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;

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
