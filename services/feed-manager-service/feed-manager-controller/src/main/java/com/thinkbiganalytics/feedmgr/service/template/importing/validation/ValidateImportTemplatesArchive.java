package com.thinkbiganalytics.feedmgr.service.template.importing.validation;
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
import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportSection;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.security.AccessController;

import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 12/11/17.
 */
public class ValidateImportTemplatesArchive extends AbstractValidateImportTemplate {

    private static final Logger log = LoggerFactory.getLogger(ValidateImportTemplatesArchive.class);


    @Inject
    private RegisteredTemplateService registeredTemplateService;
    @Inject
    private TemplateConnectionUtil templateConnectionUtil;



    public ValidateImportTemplatesArchive(ImportTemplate importTemplate, ImportTemplateOptions importTemplateOptions){
       super(importTemplate,importTemplateOptions);
    }

    public Logger getLogger(){
        return log;
    }

    public boolean validate(){

    validateReusableTemplate();

    validateRegisteredTemplate();

   if (importTemplate.isValid()) {
        validateNiFiTemplateImport();
    }

    return importTemplate.isValid();

    }



    /**
     * Validate any reusable templates as part of a zip file upload are valid for importing
     *
     */
    private void validateReusableTemplate()  {
        //validate the reusable template
        if (this.importTemplate.hasConnectingReusableTemplate()) {

            ImportComponentOption reusableTemplateOption = this.importTemplateOptions.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE);
            UploadProgressMessage reusableTemplateStatusMessage = uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(), "Validating Reusable Template. ");
            if (reusableTemplateOption.isShouldImport()) {
                boolean validForImport = true;
                //for each of the connecting template
                for (String reusableTemplateXml : this.importTemplate.getNifiConnectingReusableTemplateXmls()) {
                    try {
                        String templateName = NifiTemplateParser.getTemplateName(reusableTemplateXml);
                        UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(), "Validating Reusable Template. " + templateName);
                        TemplateDTO dto = nifiRestClient.getTemplateByName(templateName);
                        //if there is a match and it has not been acknowledged by the user to overwrite or not, error out
                        if (dto != null && !reusableTemplateOption.isUserAcknowledged()) {
                            //error out it exists
                            this.importTemplate.getImportOptions().addErrorMessage(ImportComponent.REUSABLE_TEMPLATE, "A reusable template with the same name " + templateName + " exists.");
                            this.importTemplate.setValid(false);
                            statusMessage.update("Reusable template, " + templateName + ", already exists.", false);
                            validForImport = false;
                        } else if (dto != null && reusableTemplateOption.isUserAcknowledged() && !reusableTemplateOption.isOverwrite()) {
                            validForImport = false;
                            //user has asked to not import the template.
                            uploadProgressService.removeMessage(importTemplateOptions.getUploadKey(), statusMessage);
                        } else {
                            uploadProgressService.removeMessage(importTemplateOptions.getUploadKey(), statusMessage);
                            //statusMessage.update("Validated Reusable Template", true);
                            validForImport &= true;
                        }
                    }catch (Exception e){
                        log.error("Error parsing template name from file {} ",fileName,e);
                        validForImport = false;
                    }

                }
                reusableTemplateOption.setValidForImport(validForImport);
                reusableTemplateStatusMessage.update("Validated Reusable Templates ", !reusableTemplateOption.hasErrorMessages());
            } else if (!reusableTemplateOption.isUserAcknowledged()) {
                this.importTemplate.getImportOptions().addErrorMessage(ImportComponent.REUSABLE_TEMPLATE, "The file " +  this.importTemplate.getFileName() + " has a reusable template to import.");
                this.importTemplate.setValid(false);
                reusableTemplateStatusMessage.update("A reusable template was found. Additional input needed.", false);
            } else {
                reusableTemplateStatusMessage.update("Reusable template found in import, but it is not marked for importing", true);
            }


        }
        this.uploadProgressService.completeSection(this.importTemplateOptions, ImportSection.Section.VALIDATE_REUSABLE_TEMPLATE);
    }

    /**
     * Validate the Registered Template is valid for importing
     *
     */
    private void validateRegisteredTemplate() {
        ImportComponentOption registeredTemplateOption = this.importTemplateOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA);
        //validate template
        boolean validForImport = true;

        if (!registeredTemplateOption.isUserAcknowledged()) {
            this.importTemplate.setValid(false);
            this.importTemplate.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA, "A template exists.  Do you want to import it?");
        }

        if (registeredTemplateOption.isUserAcknowledged() && registeredTemplateOption.isShouldImport()) {
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(this.importTemplateOptions.getUploadKey(), "Validating feed template for import");
            RegisteredTemplate registeredTemplate = this.importTemplate.getTemplateToImport();

            //validate unique
            //1 find if the template exists in the system running as a service account
            RegisteredTemplate
                existingTemplate =
                registeredTemplateService.findRegisteredTemplate(new RegisteredTemplateRequest.Builder().templateName(registeredTemplate.getTemplateName()).isFeedEdit(true).build());
            if (existingTemplate != null) {

                if (this.importTemplateOptions.stopProcessingAlreadyExists(ImportComponent.TEMPLATE_DATA)) {
                    this.importTemplate.setValid(false);
                    String msg = "Unable to import the template " + registeredTemplate.getTemplateName() + " It is already registered.";
                    this.importTemplate.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA, msg);
                    statusMessage.update("Validation error: The template " + registeredTemplate.getTemplateName() + " is already registered", false);
                } else {
                    //skip importing if user doesnt want it.
                    if (!registeredTemplateOption.isOverwrite()) {
                        validForImport = false;
                    }
                    statusMessage.complete(true);
                }
                registeredTemplate.setId(existingTemplate.getId());

                //validate entity access
                if (accessController.isEntityAccessControlled() && registeredTemplateOption.isOverwrite()) {
                    //requery as the currently logged in user
                    statusMessage = uploadProgressService.addUploadStatus(this.importTemplateOptions.getUploadKey(), "Validating template entity access");
                    existingTemplate = registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateName(registeredTemplate.getTemplateName()));
                    //ensure the user can Edit this template
                    boolean valid = existingTemplate != null && existingTemplate.getAllowedActions().hasAction(TemplateAccessControl.EDIT_TEMPLATE.getSystemName());
                    if (!valid) {
                        this.importTemplate.setValid(false);
                        validForImport = false;
                        statusMessage.update("Access Denied: You do not have edit access for the template " + registeredTemplate.getTemplateName(), false);
                        this.importTemplate.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA, "Access Denied: You do not have edit access for the template ");
                    } else {
                        statusMessage.complete(valid);
                    }
                }

            } else {
                //template doesnt exist.. it is new ensure the user is allowed to create templates
                boolean editAccess = accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_TEMPLATES);

                if (!editAccess) {
                    statusMessage.update("Access Denied: You are not allowed to create the template " + registeredTemplate.getTemplateName(), false);
                    this.importTemplate.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA, "Access Denied: You are not allowed to create the template ");
                }
                registeredTemplate.setId(null);
                statusMessage.complete(editAccess);
            }
            validForImport &= !registeredTemplateOption.hasErrorMessages();

            if (validForImport) {
                boolean isValid = validateTemplateProperties();
                if (this.importTemplate.isValid()) {
                    this.importTemplate.setValid(isValid);
                }
                validForImport &= isValid;
            }

            registeredTemplateOption.setValidForImport(validForImport);
        }
        this.uploadProgressService.completeSection(this.importTemplateOptions, ImportSection.Section.VALIDATE_REGISTERED_TEMPLATE);
    }

    /**
     * Validate the Template doesnt have any sensitive properties needing additional user input before importing
     *
     *
     * @return true if valid, false if not
     */
    private boolean validateTemplateProperties() {
        RegisteredTemplate template = importTemplate.getTemplateToImport();
        //detect any sensitive properties and prompt for input before proceeding
        List<NifiProperty> sensitiveProperties = template.getSensitiveProperties();
        ImportUtil.addToImportOptionsSensitiveProperties(importTemplateOptions, sensitiveProperties, ImportComponent.TEMPLATE_DATA);
        boolean valid = ImportUtil.applyImportPropertiesToTemplate(template, importTemplate, ImportComponent.TEMPLATE_DATA);
        if (!valid) {
            importTemplate.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA, "Additional properties are required for the Template");
        }
        this.importTemplateOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setAnalyzed(true);
        return valid;
    }

}
