package com.thinkbiganalytics.feedmgr.service.template.importing.importprocess;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.NiFiTemplateImport;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.security.AccessController;

import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.flow.ProcessGroupFlowDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by sr186054 on 12/11/17.
 */
public class ImportFeedTemplateXml extends AbstractImportTemplateRoutine {

    private static final Logger log = LoggerFactory.getLogger(ImportFeedTemplateXml.class);


    @Inject
    private AccessController accessController;
    @Inject
    private LegacyNifiRestClient nifiRestClient;
    @Inject
    private UploadProgressService uploadProgressService;
    @Inject
    private RegisteredTemplateService registeredTemplateService;
    @Inject
    private TemplateConnectionUtil templateConnectionUtil;
    @Inject
    private RegisteredTemplateCache registeredTemplateCache;
    @Inject
    private PropertyExpressionResolver propertyExpressionResolver;

    @Inject
    private NifiFlowCache nifiFlowCache;

    private ProcessGroupFlowDTO reusableTemplateFlow;

    public ImportFeedTemplateXml(ImportTemplate importTemplate, ImportTemplateOptions importOptions) {
        super(importTemplate, importOptions);
    }


    @Override
    public NifiProcessGroup create(NiFiTemplateImport niFiTemplateImport, UploadProgressMessage importStatusMessage) {
        TemplateDTO dto = niFiTemplateImport.getDto();
        String templateName = importTemplate.getTemplateName();
        String fileName = importTemplate.getFileName();
        importStatusMessage.update("Importing the NiFi flow, " + templateName);
        log.info("validate NiFi Flow by creating a template instance in Nifi. Template: {} for file {}", templateName, fileName);
        Map<String, Object> configProperties = propertyExpressionResolver.getStaticConfigProperties();

        List<NifiProperty> templateProperties = importTemplate.getTemplateToImport() != null ? importTemplate.getTemplateToImport().getProperties() : Collections.emptyList();
        NifiProcessGroup
            newTemplateInstance =
            nifiRestClient.createNewTemplateInstance(dto.getId(), templateProperties, configProperties, false, null, importTemplate.getVersionIdentifier());
        importTemplate.setTemplateResults(newTemplateInstance);

        return newTemplateInstance;
    }

    @Override
    public boolean validateInstance() {
        log.info("Import finished for {}, {}... verify results", importTemplate.getTemplateName(), importTemplate.getFileName());
        boolean valid = newTemplateInstance.isSuccess();
        if (valid) {
            importTemplate.setSuccess(true);
        }
        return valid;
    }

    public boolean rollback() {
        UploadProgressMessage rollbackStatus = super.restoreOldTemplateXml();
        rollbackStatus.complete(true);
        return true;
    }


    public void cleanup() {
        removeTemporaryProcessGroup();
        log.info("Success cleanup: Successfully cleaned up '" + importTemplate.getTemplateName() + "' in NiFi ");
    }

    public boolean importTemplate() {

        if (importTemplate.isValid()) {

            UploadProgressMessage importStatusMessage = start();

            boolean createReusableFlow = importTemplateOptions.isImport(ImportComponent.REUSABLE_TEMPLATE);
            boolean overwrite = importTemplateOptions.isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE);
            log.info("Importing XML file template {}, overwrite: {}, reusableFlow: {}", importTemplate.getFileName(), overwrite, createReusableFlow);
            boolean valid = importIntoNiFiAndCreateInstance();
            if (valid) {
                valid &= connectAndValidate();
                if (valid) {
                    valid &= validateInstance();
                }

                if (valid) {
                    importStatusMessage.update("Validated " + importTemplate.getTemplateName(), true);
                    cleanup();
                } else {
                    rollback();
                    importStatusMessage.update("An error occurred importing the flow, " + importTemplate.getTemplateName(), false);
                }
            }

        }
        log.info("Import NiFi Template for {} finished", importTemplate.getFileName());
        return importTemplate.isValid() && importTemplate.isSuccess();
    }


}
