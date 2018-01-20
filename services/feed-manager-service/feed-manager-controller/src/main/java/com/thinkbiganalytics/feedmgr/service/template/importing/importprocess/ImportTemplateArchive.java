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
import com.thinkbiganalytics.feedmgr.nifi.NifiTemplateParser;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportSection;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.NiFiTemplateImport;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.flow.ProcessGroupFlowDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Created by sr186054 on 12/11/17.
 */
public class ImportTemplateArchive extends AbstractImportTemplateRoutine {

    private static final Logger log = LoggerFactory.getLogger(ImportTemplateArchive.class);


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

    @Autowired
    MetadataService metadataService;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    ImportReusableTemplateFactory importReusableTemplateFactory;

    @Inject
    private NifiFlowCache nifiFlowCache;

    private ProcessGroupFlowDTO reusableTemplateFlow;


    List<ImportReusableTemplate> importedReusableTemplates;

    /**
     * The Template as it exists in the system prior to the import.
     * This is used for rollback purposes if the import fails.
     */
    private RegisteredTemplate existingTemplate;

    public ImportTemplateArchive(ImportTemplate importTemplate, ImportTemplateOptions importOptions) {
        super(importTemplate, importOptions);
    }

    public boolean importTemplate() {
        UploadProgressMessage statusMessage = null;
        //Get information about the import
        RegisteredTemplate template = importTemplate.getTemplateToImport();
        //  validateTemplateProperties(template, importTemplate, importOptions);
        //1 ensure this template doesnt already exist
        importTemplate.setTemplateName(template.getTemplateName());

        RegisteredTemplate existingTemplate = registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateName(template.getTemplateName()));
        if (existingTemplate != null) {
            template.setId(existingTemplate.getId());
        } else {
            template.setId(null);
        }

        //first we just import the reusable templates as flows
        this.importedReusableTemplates = importReusableTemplateInArchive(importTemplate, this.importTemplateOptions);

        //after the templates are created we then connect the templates
        if (!this.importTemplateOptions.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).hasErrorMessages()) {
            if (!importedReusableTemplates.isEmpty()) {
                connectReusableTemplates();
            } else {
                importTemplate.setSuccess(true);
            }

            if (importTemplate.isSuccess()) {
                RegisteredTemplate newTemplate = importFeedTemplate(existingTemplate);

                if (importTemplate.isSuccess()) {
                    //the 'newTemplate' could be null if the user chose to import just the feed metadata and not replace the template.

                    if (newTemplate != null) {
                        validateInstance();
                    }
                    if (!importedReusableTemplates.isEmpty()) {
                        importedReusableTemplates.stream().filter(importReusableTemplate -> importReusableTemplate.getImportTemplate().isSuccess()).map(t -> t.getImportTemplate())
                            .forEach(connectingTemplate -> {
                                nifiRestClient.markConnectionPortsAsRunning(connectingTemplate.getTemplateResults().getProcessGroupEntity());
                            });
                    }

                }
            }
        } else {
            rollback();
            //return if invalid
            return false;
        }

        //cleanup any temp process groups for this template
        if (!importTemplateOptions.isDeferCleanup()) {
            cleanup();
        }

        uploadProgressService.completeSection(importTemplateOptions, ImportSection.Section.IMPORT_REGISTERED_TEMPLATE);

        return importTemplate.isSuccess();


    }


    private void rollbackReusableTemplates() {
        if (importedReusableTemplates != null) {
            importedReusableTemplates.stream().forEach(importReusableTemplate -> importReusableTemplate.rollback());
        }
    }

    private boolean connectReusableTemplates() {
        //now connect these
        boolean validConnections = true;
        for (ImportReusableTemplate importReusableTemplate : importedReusableTemplates) {
            validConnections &= importReusableTemplate.connectAndValidate();
            if (importReusableTemplate.getImportTemplate().isReusableFlowOutputPortConnectionsNeeded()) {
                importReusableTemplate.getImportTemplate().getReusableTemplateConnections().stream().forEach(connectionInfo -> this.importTemplate.addReusableTemplateConnection(connectionInfo));
                this.importTemplate.setReusableFlowOutputPortConnectionsNeeded(true);
            }
            if (!validConnections) {
                break;
            }
        }
        if (validConnections) {
            for (ImportReusableTemplate importReusableTemplate : importedReusableTemplates) {
                validConnections &= importReusableTemplate.validateInstance();
                if (importReusableTemplate.getImportTemplate().isReusableFlowOutputPortConnectionsNeeded()) {
                    //   importReusableTemplate.getImportTemplate().getReusableTemplateConnections().stream().forEach(connectionInfo -> this.importTemplate.addReusableTemplateConnection(connectionInfo));
                    this.importTemplate.setReusableFlowOutputPortConnectionsNeeded(true);
                }
                if (!validConnections) {
                    break;
                }
            }
        }

        importTemplate.setSuccess(validConnections);
        return validConnections;
    }

    @Nullable
    private RegisteredTemplate importFeedTemplate(RegisteredTemplate existingTemplate) {
        RegisteredTemplate template = null;
        niFiTemplateImport = importIntoNiFi(this.importTemplate, this.importTemplateOptions);

        ImportComponentOption registeredTemplateImport = this.importTemplateOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA);
        if (existingTemplate != null) {
            importTemplate.setTemplateId(existingTemplate.getId());
        }
        if (registeredTemplateImport.isShouldImport() && registeredTemplateImport.isValidForImport()) {
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(), "Importing feed template");
            this.newTemplateInstance = create(niFiTemplateImport, statusMessage);

            if (newTemplateInstance.isSuccess()) {
                importTemplate.setSuccess(true);
                RegisteredTemplate savedTemplate = registerTemplate(importTemplate, importTemplateOptions);
                if (savedTemplate != null) {
                    template = savedTemplate;
                }
            } else {
                importTemplate.setSuccess(false);
            }
            statusMessage.complete(importTemplate.isSuccess());
        } else {
            importTemplate.setSuccess(true);
        }

        return template;
    }

    /**
     * Restore the previous metadata for this template
     */
    private void rollbackRegisteredTemplate(RegisteredTemplate existingTemplate) {
        if (!importTemplate.isSuccess()) {
            //restore existing registered template with metadata
            if (existingTemplate != null) {
                try {
                    metadataService.registerTemplate(existingTemplate);
                } catch (Exception e) {
                    Throwable root = ExceptionUtils.getRootCause(e);
                    String msg = root != null ? root.getMessage() : e.getMessage();
                    uploadProgressService.addUploadStatus(this.importTemplateOptions.getUploadKey(),
                                                          "An error occurred rolling back the template metadata for '" + existingTemplate.getTemplateName() + "'.  Error: " + msg, true, false);
                    importTemplate.getTemplateResults()
                        .addError(NifiError.SEVERITY.WARN, "Error while restoring the template " + importTemplate.getTemplateName() + " in the Kylo metadata. " + msg, "");
                }
            }
        }
    }

    /**
     * Register the template with the metadata and save it
     *
     * @param importTemplate the template data to import
     * @param importOptions  user options about what/how it should be imported
     * @return the registered template that was saved
     */
    private RegisteredTemplate registerTemplate(ImportTemplate importTemplate, ImportTemplateOptions importOptions) {
        RegisteredTemplate template = importTemplate.getTemplateToImport();
        ImportComponentOption registeredTemplateOption = importOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA);
        if (registeredTemplateOption.isValidForImport()) {
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Registering template " + template.getTemplateName() + " with Kylo metadata.");
            try {
                importTemplate.setNifiTemplateId(template.getNifiTemplateId());
                //register it in the system
                metadataService.registerTemplate(template);
                //get the new template
                template = registeredTemplateService.findRegisteredTemplate(new RegisteredTemplateRequest.Builder().templateId(template.getId()).templateName(template.getTemplateName()).build());
                importTemplate.setTemplateId(template.getId());
                statusMessage.update("Registered template with Kylo metadata.", true);

            } catch (Exception e) {
                importTemplate.setSuccess(false);
                Throwable root = ExceptionUtils.getRootCause(e);
                String msg = root != null ? root.getMessage() : e.getMessage();
                importTemplate.getTemplateResults().addError(NifiError.SEVERITY.WARN, "Error registering the template " + template.getTemplateName() + " in the Kylo metadata. " + msg, "");
                statusMessage.update("Error registering template with Kylo metadata. " + msg, false);
            }
        }
        return template;
    }


    /**
     * Called when another Template or Feed uses a Reusable template
     */
    private List<ImportReusableTemplate> importReusableTemplateInArchive(ImportTemplate importTemplate, ImportTemplateOptions importOptions) {
        List<ImportReusableTemplate> connectingTemplates = new ArrayList<>();
        //start the import
        ImportComponentOption reusableComponentOption = importOptions.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE);
        if (importTemplate.hasConnectingReusableTemplate() && reusableComponentOption.isShouldImport() && reusableComponentOption.isValidForImport()) {
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Import Reusable Template. Starting import");
            //import the reusable templates
            boolean valid = true;
            ImportTemplate lastReusableTemplate = null;
            log.info("Importing Zip file template {}. first importing reusable flow from zip");
            try {
                for (String reusableTemplateXml : importTemplate.getNifiConnectingReusableTemplateXmls()) {
                    String name = NifiTemplateParser.getTemplateName(reusableTemplateXml);
                    byte[] content = reusableTemplateXml.getBytes("UTF-8");
                    ImportReusableTemplate importReusableTemplate = importReusableTemplateFactory.apply(name, content, importOptions);

                    boolean validReusableTemplate = importReusableTemplate.importIntoNiFiAndCreateInstance();
                    connectingTemplates.add(importReusableTemplate);
                    if (!validReusableTemplate) {
                        ImportTemplate connectingTemplate = importReusableTemplate.importTemplate;
                        //add in the error messages
                        connectingTemplate.getTemplateResults().getAllErrors().stream().forEach(nifiError -> {
                            importTemplate.getTemplateResults().addError(nifiError);
                        });
                        //error out
                        importTemplate.setSuccess(false);
                        reusableComponentOption.getErrorMessages().add("Error importing Reusable Template");
                        //exit
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    statusMessage
                        .update("Successfully imported Reusable Templates " + (connectingTemplates.stream().map(t -> t.getImportTemplate().getTemplateName()).collect(Collectors.joining(","))), true);
                } else {
                    statusMessage.update("Errors importing reusable template: Imported Reusable Template. " + lastReusableTemplate != null ? lastReusableTemplate.getTemplateName() : "");
                }
            } catch (Exception e) {
                log.error("Error importing reusable template from archive {}.  {} ", importTemplate.getFileName(), lastReusableTemplate != null ? lastReusableTemplate.getTemplateName() : "");
                importTemplate.setSuccess(false);
            }
        }
        uploadProgressService.completeSection(importOptions, ImportSection.Section.IMPORT_REUSABLE_TEMPLATE);
        return connectingTemplates;
    }


    @Override
    public NifiProcessGroup create(NiFiTemplateImport niFiTemplateImport, UploadProgressMessage importStatusMessage) {
        log.info("Importing Zip file template {}, overwrite: {}, reusableFlow: {}", importTemplate.getFileName(), this.importTemplateOptions.isImportAndOverwrite(ImportComponent.TEMPLATE_DATA),
                 importTemplateOptions.isImport(ImportComponent.REUSABLE_TEMPLATE));
        if (importTemplate.isValid()) {

        }

        TemplateDTO dto = niFiTemplateImport.getDto();
        String templateName = importTemplate.getTemplateName();
        String fileName = importTemplate.getFileName();
        importStatusMessage.update("Importing the NiFi flow, " + templateName);
        log.info("Creating a template instance in Nifi. Template: {} for file {}", templateName, fileName);
        Map<String, Object> configProperties = propertyExpressionResolver.getStaticConfigProperties();

        List<NifiProperty> templateProperties = importTemplate.getTemplateToImport() != null ? importTemplate.getTemplateToImport().getProperties() : Collections.emptyList();
        this.newTemplateInstance =
            nifiRestClient.createNewTemplateInstance(dto.getId(), templateProperties, configProperties, false, null, importTemplate.getVersionIdentifier());
        importTemplate.setTemplateResults(newTemplateInstance);
        if (newTemplateInstance.getVersionedProcessGroup() != null && StringUtils.isNotBlank(newTemplateInstance.getVersionedProcessGroup().getVersionedProcessGroupName())) {
            uploadProgressService
                .addUploadStatus(importTemplateOptions.getUploadKey(), "Versioned off previous flow with the name: " + newTemplateInstance.getVersionedProcessGroup().getVersionedProcessGroupName(),
                                 true, true);
        }

        return newTemplateInstance;
    }

    public boolean validateInstance() {
        log.info("Import finished for {}, {}... verify results", importTemplate.getTemplateName(), importTemplate.getFileName());
        boolean valid = newTemplateInstance.isSuccess();
        if (valid) {
            importTemplate.setSuccess(true);
        }
        return valid;
    }


    public boolean rollback() {
        UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(this.importTemplateOptions.getUploadKey(), "Errors were found registering the template.  Attempting to rollback.");
        if (this.existingTemplate != null) {
            rollbackRegisteredTemplate(existingTemplate);
        }
        rollbackReusableTemplates();
        UploadProgressMessage rollbackStatus = super.restoreOldTemplateXml();
        rollbackStatus.complete(true);

        return true;
    }

    public void cleanup() {
        //cleanup any versioned process groups created with the reusable templates  TODO ONLY CLEANUP ON SUCCESS OR FIX THIS
        if (importTemplate.isSuccess()) {
            if (importedReusableTemplates != null && !importedReusableTemplates.isEmpty()) {
                importedReusableTemplates.stream().forEach(importReusableTemplate -> importReusableTemplate.cleanup());
            }
        }
        removeTemporaryProcessGroup();
        log.info("Success cleanup: Successfully cleaned up Nifi");
    }


}
