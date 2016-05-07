package com.thinkbiganalytics.nifi.feedmgr;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.rest.JerseyClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;

import java.util.List;

/**
 * Created by sr186054 on 5/6/16.
 */
public class TemplateInstanceCreator {
    private String templateId;
    private NifiRestClient restClient;

    public TemplateInstanceCreator(NifiRestClient restClient, String templateId) {
        this.restClient = restClient;
        this.templateId = templateId;
    }

    public NifiProcessGroup createTemplate() throws JerseyClientException {

        NifiProcessGroup newProcessGroup = null;
        TemplateDTO template = restClient.getTemplateById(templateId);

        if (template != null) {
            TemplateCreationHelper templateCreationHelper = new TemplateCreationHelper(this.restClient);
            String processGroupId = null;
            String tmpName = template.getName() + "_" + System.currentTimeMillis();
            ProcessGroupEntity group = restClient.createProcessGroup(tmpName);
            processGroupId = group.getProcessGroup().getId();
            if (StringUtils.isNotBlank(processGroupId)) {
                //snapshot the existing controller services
                templateCreationHelper.snapshotControllerServiceReferences();
                //create the flow from the template
                templateCreationHelper.instantiateFlowFromTemplate(processGroupId, templateId);
                //mark the new services that were created as a result of creating the new flow from the template
                templateCreationHelper.identifyNewlyCreatedControllerServiceReferences();

                ProcessGroupEntity entity = restClient.getProcessGroup(processGroupId, true, true);

                //identify the various processors (first level initial processors)
                List<ProcessorDTO> inputProcessors = NifiProcessUtil.getInputProcessors(entity.getProcessGroup());

                ProcessorDTO input =null;
                List<ProcessorDTO> nonInputProcessors = NifiProcessUtil.getNonInputProcessors(entity.getProcessGroup());

                //if the input is null attempt to get the first input available on the template
                if (input == null && inputProcessors != null && !inputProcessors.isEmpty()) {
                    input = inputProcessors.get(0);
                }

                //update any references to the controller services and try to assign the value to an enabled service if it is not already
                if (input != null) {
                    templateCreationHelper.updateControllerServiceReferences(Lists.newArrayList(input));
                }
                templateCreationHelper.updateControllerServiceReferences(nonInputProcessors);
                //refetch processors for updated errors
                entity = restClient.getProcessGroup(processGroupId, true, true);
                nonInputProcessors = NifiProcessUtil.getNonInputProcessors(entity.getProcessGroup());

                newProcessGroup = new NifiProcessGroup(entity, input, nonInputProcessors);

                templateCreationHelper.cleanupControllerServices();
                newProcessGroup.setSuccess(!newProcessGroup.hasErrors());

                return newProcessGroup;

            }

        }
        return null;
    }
}
