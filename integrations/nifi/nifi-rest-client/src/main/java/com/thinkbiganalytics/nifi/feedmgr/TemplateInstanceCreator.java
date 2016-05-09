package com.thinkbiganalytics.nifi.feedmgr;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.ControllerServicePropertyHolder;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
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

    private boolean createReusableFlow;

    public TemplateInstanceCreator(NifiRestClient restClient, String templateId, boolean createReusableFlow) {
        this.restClient = restClient;
        this.templateId = templateId;
        this.createReusableFlow = createReusableFlow;
    }

    public boolean isCreateReusableFlow() {
        return createReusableFlow;
    }

    private void ensureInputPortsForReuseableTemplate(String processGroupId) throws JerseyClientException {
        ProcessGroupEntity template = restClient.getProcessGroup(processGroupId, false, false);
        String categoryId = template.getProcessGroup().getParentGroupId();
        restClient.createReusableTemplateInputPort(categoryId, processGroupId);
    }



    public NifiProcessGroup createTemplate() throws JerseyClientException {

        NifiProcessGroup newProcessGroup = null;
        TemplateDTO template = restClient.getTemplateById(templateId);

        if (template != null) {
            TemplateCreationHelper templateCreationHelper = new TemplateCreationHelper(this.restClient);
            String processGroupId = null;

            ProcessGroupEntity group = null;
            if(isCreateReusableFlow()){
                //1 get/create the parent "reusable_templates" processgroup
                ProcessGroupDTO reusableParentGroup = restClient.getProcessGroupByName("root", TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME);
                if(reusableParentGroup == null){
                    reusableParentGroup = restClient.createProcessGroup("root",TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME).getProcessGroup();
                }
                ProcessGroupDTO thisGroup = restClient.getProcessGroupByName(reusableParentGroup.getId(), template.getName());
                if(thisGroup != null){
                    //version the group
                    templateCreationHelper.versionProcessGroup(thisGroup);

                }
                group = restClient.createProcessGroup(reusableParentGroup.getId(),template.getName());
            }
            else {
                String tmpName = template.getName() + "_" + System.currentTimeMillis();
             group   =restClient.createProcessGroup(tmpName);
            }
            processGroupId = group.getProcessGroup().getId();
            if (StringUtils.isNotBlank(processGroupId)) {
                //snapshot the existing controller services
                templateCreationHelper.snapshotControllerServiceReferences();
                //create the flow from the template
                templateCreationHelper.instantiateFlowFromTemplate(processGroupId, templateId);

                if (this.createReusableFlow) {
                    ensureInputPortsForReuseableTemplate(processGroupId);
                }

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



                ///make the input/output ports in the category group as running
                if(isCreateReusableFlow())
                {
                    templateCreationHelper.markConnectionPortsAsRunning(entity);
                }


                newProcessGroup = new NifiProcessGroup(entity, input, nonInputProcessors);

                if(isCreateReusableFlow()) {
                    templateCreationHelper.markProcessorsAsRunning(newProcessGroup);
                }

                 templateCreationHelper.cleanupControllerServices();
                List<NifiError> errors = templateCreationHelper.getErrors();
                    //add any global errors to the object
                    if (errors != null && !errors.isEmpty()) {
                        for (NifiError error : errors) {
                            newProcessGroup.addError(error);
                        }
                    }



                newProcessGroup.setSuccess(!newProcessGroup.hasFatalErrors());

                return newProcessGroup;

            }

        }
        return null;
    }
}
