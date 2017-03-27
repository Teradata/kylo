package com.thinkbiganalytics.feedmgr.service.template;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.feedmgr.nifi.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.NifiTemplateParser;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportSection;
import com.thinkbiganalytics.feedmgr.rest.ImportType;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgress;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.support.ZipFileUtil;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.nifi.feedmgr.ReusableTemplateCreationCallback;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NiFiComponentErrors;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Export or Import a template
 */
public class ExportImportTemplateService {

    public static final Logger log = LoggerFactory.getLogger(ExportImportTemplateService.class);

    public static final String NIFI_CONNECTING_REUSABLE_TEMPLATE_XML_FILE = "nifiConnectingReusableTemplate";

    public static final String NIFI_TEMPLATE_XML_FILE = "nifiTemplate.xml";

    public static final String TEMPLATE_JSON_FILE = "template.json";
    @Autowired
    PropertyExpressionResolver propertyExpressionResolver;

    @Autowired
    MetadataService metadataService;

    @Inject
    MetadataAccess metadataAccess;

    @Autowired
    LegacyNifiRestClient nifiRestClient;
    @Inject
    NifiFlowCache nifiFlowCache;
    @Inject
    private AccessController accessController;

    @Inject
    private RegisteredTemplateService registeredTemplateService;

    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;

    @Inject
    private UploadProgressService uploadProgressService;

    //Export Methods

    public ExportTemplate exportTemplate(String templateId) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EXPORT_TEMPLATES);

        RegisteredTemplate
            template =
            registeredTemplateService.findRegisteredTemplate(new RegisteredTemplateRequest.Builder().templateId(templateId).nifiTemplateId(templateId).includeSensitiveProperties(true).build());
        if (template != null) {
            List<String> connectingReusableTemplates = new ArrayList<>();
            Set<String> connectedTemplateIds = new HashSet<>();
            //if this template uses any reusable templates then export those reusable ones as well
            if (template.usesReusableTemplate()) {
                List<ReusableTemplateConnectionInfo> reusableTemplateConnectionInfos = template.getReusableTemplateConnections();
                for (ReusableTemplateConnectionInfo reusableTemplateConnectionInfo : reusableTemplateConnectionInfos) {
                    String inputName = reusableTemplateConnectionInfo.getReusableTemplateInputPortName();
                    //find the template that has the input port name?
                    Map<String, String> map = nifiRestClient.getTemplatesAsXmlMatchingInputPortName(inputName);
                    if (map != null && !map.isEmpty()) {
                        //get the first one??
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            String portTemplateId = entry.getKey();
                            if (!connectedTemplateIds.contains(portTemplateId)) {
                                connectedTemplateIds.add(portTemplateId);
                                connectingReusableTemplates.add(entry.getValue());
                            }
                        }
                    }
                }
            }

            String templateXml = null;
            try {
                if (template != null) {
                    try {
                        templateXml = nifiRestClient.getTemplateXml(template.getNifiTemplateId());
                    } catch (NifiClientRuntimeException e) {
                        TemplateDTO templateDTO = nifiRestClient.getTemplateByName(template.getTemplateName());
                        if (templateDTO != null) {
                            templateXml = nifiRestClient.getTemplateXml(templateDTO.getId());
                        }
                    }
                }
            } catch (Exception e) {
                throw new UnsupportedOperationException("Unable to find Nifi Template for " + templateId);
            }

            //create a zip file with the template and xml
            byte[] zipFile = zip(template, templateXml, connectingReusableTemplates);

            return new ExportTemplate(SystemNamingService.generateSystemName(template.getTemplateName()) + ".template.zip", zipFile);

        } else {
            throw new UnsupportedOperationException("Unable to find Template for " + templateId);
        }
    }

    private byte[] zip(RegisteredTemplate template, String nifiTemplateXml, List<String> reusableTemplateXmls) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry = new ZipEntry(NIFI_TEMPLATE_XML_FILE);
            zos.putNextEntry(entry);
            zos.write(nifiTemplateXml.getBytes());
            zos.closeEntry();
            int reusableTemplateNumber = 0;
            for (String reusableTemplateXml : reusableTemplateXmls) {
                entry = new ZipEntry(String.format("%s_%s.xml", NIFI_CONNECTING_REUSABLE_TEMPLATE_XML_FILE, reusableTemplateNumber++));
                zos.putNextEntry(entry);
                zos.write(reusableTemplateXml.getBytes());
                zos.closeEntry();
            }
            entry = new ZipEntry(TEMPLATE_JSON_FILE);
            zos.putNextEntry(entry);
            String json = ObjectMapperSerializer.serialize(template);
            zos.write(json.getBytes());
            zos.closeEntry();

        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return baos.toByteArray();
    }

    //Validation Methods


    /**
     * Is the file a valid file for importing
     *
     * @param fileName the name of the file
     * @return true if valid, false if not valid
     */
    private boolean isValidFileImport(String fileName) {
        return fileName.endsWith(".zip") || fileName.endsWith(".xml");
    }


    //validate
    public ImportTemplate validateTemplateForImport(final String fileName, byte[] content, ImportOptions importOptions) {

        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_TEMPLATES);
        InputStream inputStream = new ByteArrayInputStream(content);
        UploadProgressMessage overallStatusMessage = uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Validating template for import");
        UploadProgressMessage statusMessage = overallStatusMessage;
        ImportTemplateOptions options = new ImportTemplateOptions();
        options.setUploadKey(importOptions.getUploadKey());
        ImportTemplate template = null;
        if (!isValidFileImport(fileName)) {
            statusMessage.complete(false);
            throw new UnsupportedOperationException("Unable to import " + fileName + ".  The file must be a zip file or a Nifi Template xml file");
        }
        try {
            if (fileName.endsWith(".zip")) {
                template = openZip(fileName, inputStream);
                template.setValid(true);
                Set<ImportComponentOption> componentOptions = ImportUtil.inspectZipComponents(content, ImportType.TEMPLATE);
                options.setImportComponentOptions(importOptions.getImportComponentOptions());
                options.addOptionsIfNotExists(componentOptions);
                template.setImportOptions(options);

                validateReusableTemplate(template, options);

                validateRegisteredTemplate(template, options);

                validateNiFiTemplateImport(template);
            } else {
                template = getNewNiFiTemplateImport(fileName, inputStream);
                template.setImportOptions(options);
                //deal with reusable templates??
                validateNiFiTemplateImport(template);
            }
        } catch (Exception e) {
            statusMessage.update("An Error occurred " + e.getMessage(), false);
            throw new UnsupportedOperationException("Error importing template  " + fileName + ".  " + e.getMessage());
        }
        overallStatusMessage.update("Validated template for import ", template.isValid());

        return template;
    }

    /**
     * Validate the NiFi template is valid.  This method will validate the {@link ImportTemplate#nifiTemplateXml}
     *
     * @param template the template data to validate before importing
     */
    private void validateNiFiTemplateImport(ImportTemplate template) throws IOException {
        ImportOptions options = template.getImportOptions();
        ImportComponentOption nifiTemplateOption = options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE);
        //if the options of the TEMPLATE_DATA are marked to import and overwrite this should be as well
        ImportComponentOption templateData = options.findImportComponentOption(ImportComponent.TEMPLATE_DATA);

        if (templateData.isUserAcknowledged()) {
            nifiTemplateOption.setUserAcknowledged(true);
        }
        if (templateData.isShouldImport()) {
            nifiTemplateOption.setShouldImport(true);
        }
        if (templateData.isOverwrite()) {
            nifiTemplateOption.setOverwrite(true);
        }

        if (nifiTemplateOption.isShouldImport()) {
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(options.getUploadKey(), "Validating the NiFi template");
            String templateName = null;
            TemplateDTO dto = null;
            try {
                templateName = NifiTemplateParser.getTemplateName(template.getNifiTemplateXml());
                template.setTemplateName(templateName);
                dto = nifiRestClient.getTemplateByName(templateName);
                if (dto != null) {
                    template.setNifiTemplateId(dto.getId());
                    if (!options.isUserAcknowledged(ImportComponent.NIFI_TEMPLATE) && !options.isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE) && !options
                        .isContinueIfExists(ImportComponent.NIFI_TEMPLATE)) {
                        template.setValid(false);
                        String msg = "Unable to import Template " + templateName
                                     + ".  It already exists in NiFi.";
                        template.getImportOptions().addErrorMessage(ImportComponent.NIFI_TEMPLATE, msg);
                        statusMessage.update("Validation Error: Unable to import Template " + templateName + ".  It already exists in NiFi.");
                        statusMessage.complete(false);
                    } else {
                        statusMessage.update("Validated the NiFi template. ");
                        statusMessage.complete(true);
                    }
                } else {
                    statusMessage.update("Validated the NiFi template.  The template " + templateName + " will be created in NiFi");
                    statusMessage.complete(true);
                }
            } catch (ParserConfigurationException | XPathExpressionException | SAXException e) {
                template.setValid(false);
                template.getTemplateResults().addError(NifiError.SEVERITY.WARN, "The xml file you are trying to import is not a valid NiFi template.  Please try again. " + e.getMessage(), "");
                statusMessage.complete(false);
            }
            nifiTemplateOption.setValidForImport(!nifiTemplateOption.hasErrorMessages());
        }
        completeSection(options, ImportSection.Section.VALIDATE_NIFI_TEMPLATE);
    }

    /**
     * Validate the Registered Template is valid for importing
     *
     * @param template the template data to validate before importing
     * @param options  user options about what/how it should be imported
     */
    private void validateRegisteredTemplate(ImportTemplate template, ImportOptions options) {
        ImportComponentOption registeredTemplateOption = options.findImportComponentOption(ImportComponent.TEMPLATE_DATA);
        //validate template
        boolean validForImport = true;

        if (!registeredTemplateOption.isUserAcknowledged()) {
            template.setValid(false);
            template.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA, "A template exists.  Do you want to import it?");
        }

        if (registeredTemplateOption.isUserAcknowledged() && registeredTemplateOption.isShouldImport()) {
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(options.getUploadKey(), "Validating feed template for import");
            RegisteredTemplate registeredTemplate = template.getTemplateToImport();

            //validate unique
            RegisteredTemplate existingTemplate = registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateName(registeredTemplate.getTemplateName()));
            if (existingTemplate != null) {
                if (options.stopProcessingAlreadyExists(ImportComponent.TEMPLATE_DATA)) {
                    template.setValid(false);
                    String msg = "Unable to import the template " + registeredTemplate.getTemplateName() + " It is already registered.";
                    template.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA, msg);
                    statusMessage.update("Validation error: The template " + registeredTemplate.getTemplateName() + " is already registered", false);
                } else {
                    //skip importing if user doesnt want it.
                    if (!registeredTemplateOption.isOverwrite()) {
                        validForImport = false;
                    }
                    statusMessage.complete(true);
                }
                registeredTemplate.setId(existingTemplate.getId());
            } else {
                //reset it so it gets the new id upon import
                registeredTemplate.setId(null);
                statusMessage.complete(true);
            }
            validForImport &= !registeredTemplateOption.hasErrorMessages();

            if (validForImport) {
                boolean isValid = validateTemplateProperties(template, options);
                if (template.isValid()) {
                    template.setValid(isValid);
                }
                validForImport &= isValid;
            }

            registeredTemplateOption.setValidForImport(validForImport);
        }
        completeSection(options, ImportSection.Section.VALIDATE_REGISTERED_TEMPLATE);
    }

    /**
     * Validate any reusable templates as part of a zip file upload are valid for importing
     *
     * @param template the template data to validate before importing
     * @param options  user options about what/how it should be imported
     */
    private void validateReusableTemplate(ImportTemplate template, ImportTemplateOptions options) throws Exception {
        //validate the reusable template
        if (template.hasConnectingReusableTemplate()) {

            ImportComponentOption reusableTemplateOption = options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE);
            UploadProgressMessage reusableTemplateStatusMessage = uploadProgressService.addUploadStatus(options.getUploadKey(), "Validating Reusable Template. ");
            if (reusableTemplateOption.isShouldImport()) {
                boolean validForImport = true;
                //for each of the connecting template
                for (String reusableTemplateXml : template.getNifiConnectingReusableTemplateXmls()) {

                    String templateName = NifiTemplateParser.getTemplateName(reusableTemplateXml);
                    UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(options.getUploadKey(), "Validating Reusable Template. " + templateName);
                    TemplateDTO dto = nifiRestClient.getTemplateByName(templateName);
                    //if there is a match and it has not been acknowledged by the user to overwrite or not, error out
                    if (dto != null && !reusableTemplateOption.isUserAcknowledged()) {
                        //error out it exists
                        template.getImportOptions().addErrorMessage(ImportComponent.REUSABLE_TEMPLATE, "A reusable template with the same name " + templateName + " exists.");
                        template.setValid(false);
                        statusMessage.update("Reusable template, " + templateName + ", already exists.", false);
                        validForImport = false;
                    } else if (dto != null && reusableTemplateOption.isUserAcknowledged() && !reusableTemplateOption.isOverwrite()) {
                        validForImport = false;
                        //user has asked to not import the template.
                        uploadProgressService.removeMessage(options.getUploadKey(), statusMessage);
                    } else {
                        uploadProgressService.removeMessage(options.getUploadKey(), statusMessage);
                        //statusMessage.update("Validated Reusable Template", true);
                        validForImport &= true;
                    }

                }
                reusableTemplateOption.setValidForImport(validForImport);
                reusableTemplateStatusMessage.update("Validated Reusable Templates ", !reusableTemplateOption.hasErrorMessages());
            } else if (!reusableTemplateOption.isUserAcknowledged()) {
                template.getImportOptions().addErrorMessage(ImportComponent.REUSABLE_TEMPLATE, "The file " + template.getFileName() + " has a reusable template to import.");
                template.setValid(false);
                reusableTemplateStatusMessage.update("A reusable template was found. Additional input needed.", false);
            } else {
                reusableTemplateStatusMessage.update("Reusable template found in import, but it is not marked for importing", true);
            }


        }
        completeSection(options, ImportSection.Section.VALIDATE_REUSABLE_TEMPLATE);
    }

    /**
     * Validate the Template doesnt have any sensitive properties needing additional user input before importing
     *
     * @param importTemplate the template data to validate before importing
     * @param importOptions  user options about what/how it should be imported
     * @return true if valid, false if not
     */
    private boolean validateTemplateProperties(ImportTemplate importTemplate, ImportOptions importOptions) {
        RegisteredTemplate template = importTemplate.getTemplateToImport();
        //detect any sensitive properties and prompt for input before proceeding
        List<NifiProperty> sensitiveProperties = template.getSensitiveProperties();
        ImportUtil.addToImportOptionsSensitiveProperties(importOptions, sensitiveProperties, ImportComponent.TEMPLATE_DATA);
        boolean valid = ImportUtil.applyImportPropertiesToTemplate(template, importTemplate, ImportComponent.TEMPLATE_DATA);
        if (!valid) {
            importTemplate.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA, "Additional properties are required for the Template");
        }
        importOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setAnalyzed(true);
        return valid;
    }

    /**
     * Validate the sensitive properties are set for the reusable templates.
     *
     * @param importTemplate the template data to validate before importing
     * @param importOptions  user options about what/how it should be imported
     * @return true if valid, false if not
     */
    private boolean analyzeReusableTemplateForImport(ImportTemplate importTemplate, ImportOptions importOptions) throws Exception {

        boolean valid = true;
        if (importTemplate.hasConnectingReusableTemplate()) {
            log.info("analyzing reusable templates in file");
            for (String reusableTemplateXml : importTemplate.getNifiConnectingReusableTemplateXmls()) {
                //create a temp instance of this template to assess if it has any sensitive properties
                String templateName = NifiTemplateParser.getTemplateName(importTemplate.getNifiTemplateXml());
                String reusableTemplateName = templateName;
                templateName += "_" + System.currentTimeMillis();
                String templateXml = NifiTemplateParser.updateTemplateName(importTemplate.getNifiTemplateXml(), templateName);

                TemplateDTO temporaryTemplate = nifiRestClient.importTemplate(templateName, templateXml);
                log.info("created temp reusable template {} ", temporaryTemplate.getName() + ", for " + reusableTemplateName);
                TemplateCreationHelper helper = new TemplateCreationHelper(nifiRestClient);
                ProcessGroupDTO flow = helper.createTemporaryTemplateFlow(temporaryTemplate.getId());
                nifiRestClient.deleteTemplate(temporaryTemplate.getId());
                List<NifiProperty>
                    sensitiveProperties =
                    NifiPropertyUtil.getProperties(flow, propertyDescriptorTransform).stream().filter(nifiProperty -> nifiProperty.isSensitive()).collect(Collectors.toList());
                if (sensitiveProperties != null && !sensitiveProperties.isEmpty()) {
                    ImportUtil.addToImportOptionsSensitiveProperties(importOptions, sensitiveProperties, ImportComponent.REUSABLE_TEMPLATE);
                    importTemplate.getImportOptions().addErrorMessage(ImportComponent.REUSABLE_TEMPLATE, "Additional Properties are required for input on the reusable template");
                    valid = false;
                }

            }
            importOptions.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setAnalyzed(true);
        }
        return valid;
    }


    private void completeSection(ImportOptions options, ImportSection.Section section) {
        UploadProgress progress = uploadProgressService.getUploadStatus(options.getUploadKey());
        progress.completeSection(section.name());
    }

    /// IMPORT methods


    /**
     * Import a xml or zip file.
     *
     * @param fileName      the name of the file
     * @param content       the file
     * @param importOptions user options about what/how it should be imported
     * @return the template data to import along with status/messages/error information if it was valid and if was successfully imported
     */
    public ImportTemplate importTemplate(final String fileName, final byte[] content, ImportTemplateOptions importOptions) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_TEMPLATES);

            ImportTemplate template = null;
            if (!isValidFileImport(fileName)) {
                throw new UnsupportedOperationException("Unable to import " + fileName + ".  The file must be a zip file or a Nifi Template xml file");
            }
            try {
                if (fileName.endsWith(".zip")) {
                    UploadProgress progress = uploadProgressService.getUploadStatus(importOptions.getUploadKey());
                    progress.setSections(ImportSection.sectionsForImportAsString(ImportType.TEMPLATE));
                    template = validateAndImportZip(fileName, content, importOptions); //dont allow exported reusable flows to become registered templates
                } else if (fileName.endsWith(".xml")) {

                    UploadProgress progress = uploadProgressService.getUploadStatus(importOptions.getUploadKey());
                    progress.setSections(ImportSection.sectionsForImportAsString(ImportType.TEMPLATE_XML));

                    template = importNifiTemplate(fileName, content, importOptions, true);
                }
            } catch (IOException e) {
                throw new UnsupportedOperationException("Error importing template  " + fileName + ".  " + e.getMessage());
            }
            template.setImportOptions(importOptions);
            return template;
        });
    }

    /**
     * Imports the data and various components based upon the supplied {@link ImportTemplate#importOptions}.
     *
     * Note.  This method will not call any validation routines.  It will just import.
     * If you want to validate before to ensure the import will be correct call this.importTemplate(final String fileName, final byte[] content, ImportTemplateOptions importOptions)
     *
     * @param importTemplate the template data to validate before importing
     * @return the template data to validate before importing
     */
    public ImportTemplate importZip(ImportTemplate importTemplate) throws Exception {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_TEMPLATES);
        ImportTemplateOptions importOptions = importTemplate.getImportOptions();

        log.info("Importing Zip file template {}, overwrite: {}, reusableFlow: {}", importTemplate.getFileName(), importOptions.isImportAndOverwrite(ImportComponent.TEMPLATE_DATA),
                 importOptions.isImport(ImportComponent.REUSABLE_TEMPLATE));
        if (importTemplate.isValid()) {
            UploadProgressMessage startingStatusMessage = uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Starting import of the template");
            startingStatusMessage.complete(true);
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

            List<ImportTemplate> connectingTemplates = importReusableTemplate(importTemplate, importOptions);
            if (importOptions.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).hasErrorMessages()) {
                //return if invalid
                return importTemplate;
            }

            NiFiTemplateImport niFiTemplateImport = importNiFiXmlTemplate(importTemplate, importOptions);

            String oldTemplateXml = niFiTemplateImport.getOldTemplateXml();
            TemplateDTO dto = niFiTemplateImport.getDto();
            template.setNifiTemplateId(dto.getId());
            importTemplate.setNifiTemplateId(dto.getId());
            ImportComponentOption registeredTemplateImport = importOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA);
            if (existingTemplate != null) {
                importTemplate.setTemplateId(existingTemplate.getId());
            }
            if (registeredTemplateImport.isShouldImport() && registeredTemplateImport.isValidForImport()) {

                NifiProcessGroup newTemplateInstance = createTemplateInstance(importTemplate, importOptions);

                if (newTemplateInstance.isSuccess()) {
                    importTemplate.setSuccess(true);
                    RegisteredTemplate savedTemplate = registerTemplate(importTemplate, importOptions);
                    if (savedTemplate != null) {
                        template = savedTemplate;
                    }

                }
                if (!importTemplate.isSuccess()) {
                    //ROLLBACK.. errrors
                    statusMessage = uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Errors were found registering the template.  Attempting to rollback.");
                    rollbackTemplateImportInNifi(importTemplate, dto, oldTemplateXml);
                    //also restore existing registered template with metadata
                    //restore old registered template
                    if (existingTemplate != null) {
                        try {
                            metadataService.registerTemplate(existingTemplate);
                        } catch (Exception e) {
                            Throwable root = ExceptionUtils.getRootCause(e);
                            String msg = root != null ? root.getMessage() : e.getMessage();
                            importTemplate.getTemplateResults()
                                .addError(NifiError.SEVERITY.WARN, "Error while restoring the template " + template.getTemplateName() + " in the Kylo metadata. " + msg, "");
                        }
                    }
                    statusMessage.update("Errors were found registering the template.  Rolled back to previous version.", true);
                }

                //remove the temporary Process Group we created
                removeTemporaryProcessGroup(importTemplate);
            } else {
                importTemplate.setSuccess(true);
            }

            //if we also imported the reusable template make sure that is all running properly
            for (ImportTemplate connectingTemplate : connectingTemplates) {
                //enable it
                nifiRestClient.markConnectionPortsAsRunning(connectingTemplate.getTemplateResults().getProcessGroupEntity());
            }

            completeSection(importOptions, ImportSection.Section.IMPORT_REGISTERED_TEMPLATE);

        }

        return importTemplate;
    }


    /**
     * Import a template string into NiFi as a template to use
     */
    private NiFiTemplateImport importNiFiXmlTemplate(ImportTemplate template, ImportTemplateOptions importOptions) throws IOException {

        TemplateDTO dto = null;
        String templateName = null;
        String oldTemplateXml = null;

        ImportComponentOption nifiTemplateImport = importOptions.findImportComponentOption(ImportComponent.NIFI_TEMPLATE);
        if (nifiTemplateImport.isValidForImport()) {
            try {
                templateName = NifiTemplateParser.getTemplateName(template.getNifiTemplateXml());
                template.setTemplateName(templateName);
                dto = nifiRestClient.getTemplateByName(templateName);
                if (dto != null) {
                    oldTemplateXml = nifiRestClient.getTemplateXml(dto.getId());
                    template.setNifiTemplateId(dto.getId());
                    if (importOptions.isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE) && nifiTemplateImport.isValidForImport()) {
                        nifiRestClient.deleteTemplate(dto.getId());
                    }
                }
            } catch (ParserConfigurationException | XPathExpressionException | SAXException e) {
                throw new UnsupportedOperationException("The xml file you are trying to import is not a valid NiFi template.  Please try again. " + e.getMessage());
            }

            boolean register = (dto == null || (importOptions.isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE) && nifiTemplateImport.isValidForImport()));
            //attempt to import the xml into NiFi if its new, or if the user said to overwrite
            if (register) {
                UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Importing " + templateName + " into NiFi");
                log.info("Attempting to import Nifi Template: {} for file {}", templateName, template.getFileName());
                dto = nifiRestClient.importTemplate(template.getTemplateName(), template.getNifiTemplateXml());
                template.setNifiTemplateId(dto.getId());
                statusMessage.update("Imported " + templateName + " into NiFi", true);
            }
        }

        completeSection(importOptions, ImportSection.Section.IMPORT_NIFI_TEMPLATE);
        return new NiFiTemplateImport(oldTemplateXml, dto);

    }


    private ImportTemplate validateAndImportZip(String fileName, byte[] content, ImportTemplateOptions importOptions) throws Exception {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_TEMPLATES);
        ImportTemplate importTemplate = validateTemplateForImport(fileName, content, importOptions);
        return importZip(importTemplate);
    }


    private NifiProcessGroup createTemplateInstance(ImportTemplate importTemplate, ImportTemplateOptions importOptions) {
        UploadProgressMessage
            statusMessage =
            uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Starting to import RegisteredTemplate.  Attempting to verify NiFi flow ");

        //Create the new instance of the template in NiFi
        Map<String, Object> configProperties = propertyExpressionResolver.getStaticConfigProperties();
        NifiProcessGroup newTemplateInstance =
            nifiRestClient.createNewTemplateInstance(importTemplate.getNifiTemplateId(), configProperties, false, new NifiFlowCacheReusableTemplateCreationCallback(false));
        importTemplate.setTemplateResults(newTemplateInstance);
        if (newTemplateInstance.isSuccess()) {
            statusMessage.update("Verified NiFi flow.", true);
        } else {
            statusMessage.update("Error creating NiFi template instance ", false);
        }
        completeSection(importOptions, ImportSection.Section.CREATE_NIFI_INSTANCE);
        return newTemplateInstance;

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

    private List<ImportTemplate> importReusableTemplate(ImportTemplate importTemplate, ImportTemplateOptions importOptions) throws Exception {
        List<ImportTemplate> connectingTemplates = new ArrayList<>();
        //start the import
        ImportComponentOption reusableComponentOption = importOptions.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE);
        if (importTemplate.hasConnectingReusableTemplate() && reusableComponentOption.isShouldImport() && reusableComponentOption.isValidForImport()) {
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Import Reusable Template. Starting import");
            //import the reusable templates
            boolean valid = true;
            ImportTemplate lastReusableTemplate = null;
            log.info("Importing Zip file template {}. first importing reusable flow from zip");
            for (String reusableTemplateXml : importTemplate.getNifiConnectingReusableTemplateXmls()) {

                String name = NifiTemplateParser.getTemplateName(reusableTemplateXml);
                ImportTemplate connectingTemplate = importNifiTemplateWithTemplateString(name, reusableTemplateXml, importOptions,
                                                                                         false);
                lastReusableTemplate = connectingTemplate;
                if (!connectingTemplate.isSuccess()) {
                    //return with exception
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
                } else {
                    connectingTemplates.add(connectingTemplate);

                }
            }
            if (valid) {
                statusMessage.update("Successfully imported Reusable Templates " + (connectingTemplates.stream().map(t -> t.getTemplateName()).collect(Collectors.joining(","))), true);
            } else {

                statusMessage.update("Errors importing reusable template: Imported Reusable Template. " + lastReusableTemplate != null ? lastReusableTemplate.getTemplateName() : "");
            }
        }
        completeSection(importOptions, ImportSection.Section.IMPORT_REUSABLE_TEMPLATE);
        return connectingTemplates;
    }

    private ImportTemplate importNifiTemplateWithTemplateString(String fileName, String xmlFile, ImportTemplateOptions importOptions, boolean xmlImport) throws IOException {
        byte[] content = xmlFile.getBytes("UTF-8");
        return importNifiTemplate(fileName, content, importOptions, xmlImport);
    }

    /**
     *
     * @param fileName
     * @param xmlFile
     * @param importOptions
     * @param xmlImport
     * @return
     * @throws IOException
     */
    private ImportTemplate importNifiTemplate(String fileName, byte[] xmlFile, ImportTemplateOptions importOptions, boolean xmlImport) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(xmlFile);
        ImportTemplate importTemplate = getNewNiFiTemplateImport(fileName, inputStream);
        importTemplate.setImportOptions(importOptions);

        validateNiFiTemplateImport(importTemplate);

        if (importTemplate.isValid()) {

            UploadProgressMessage importStatusMessage = uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Importing the NiFi flow ");

            boolean createReusableFlow = importOptions.isImport(ImportComponent.REUSABLE_TEMPLATE);
            boolean overwrite = importOptions.isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE);
            log.info("Importing XML file template {}, overwrite: {}, reusableFlow: {}", fileName, overwrite, createReusableFlow);

            NiFiTemplateImport niFiTemplateImport = importNiFiXmlTemplate(importTemplate, importOptions);
            String oldTemplateXml = niFiTemplateImport.getOldTemplateXml();
            TemplateDTO dto = niFiTemplateImport.getDto();
            String templateName = importTemplate.getTemplateName();

            importStatusMessage.update("Importing the NiFi flow, " + templateName);

            log.info("validate NiFi Flow by creating a template instance in nifi Nifi Template: {} for file {}", templateName, fileName);
            Map<String, Object> configProperties = propertyExpressionResolver.getStaticConfigProperties();
            NifiFlowCacheReusableTemplateCreationCallback reusableTemplateCreationCallback = new NifiFlowCacheReusableTemplateCreationCallback(xmlImport);
            if (createReusableFlow) {
                importStatusMessage.update("Creating reusable flow instance for " + templateName);
            }
            NifiProcessGroup newTemplateInstance = nifiRestClient.createNewTemplateInstance(dto.getId(), configProperties, createReusableFlow, reusableTemplateCreationCallback);
            importTemplate.setTemplateResults(newTemplateInstance);
            log.info("Import finished for {}, {}... verify results", templateName, fileName);
            if (newTemplateInstance.isSuccess()) {
                log.info("SUCCESS! This template is valid Nifi Template: {} for file {}", templateName, fileName);
                importTemplate.setSuccess(true);
                if (createReusableFlow) {
                    importStatusMessage.update("Finished creating reusable flow instance for " + templateName, true);
                    importReusableTemplateSuccess(importTemplate);
                } else {
                    importStatusMessage.update("Validated " + templateName, true);
                }
            } else {
                rollbackTemplateImportInNifi(importTemplate, dto, oldTemplateXml);
                importStatusMessage.update("An error occurred importing the template, " + templateName, false);
            }

            if (!createReusableFlow) {
                removeTemporaryProcessGroup(importTemplate);
                try {
                    nifiRestClient.deleteProcessGroup(newTemplateInstance.getProcessGroupEntity());
                } catch (NifiComponentNotFoundException e) {
                    //its ok if its not found
                }
                log.info("Success cleanup: Successfully cleaned up Nifi");
            }

            log.info("Import NiFi Template for {} finished", fileName);

        }

        return importTemplate;
    }

    /**
     * Restore the previous Template back to Nifi
     */
    private void rollbackTemplateImportInNifi(ImportTemplate importTemplate, TemplateDTO dto, String oldTemplateXml) throws IOException {

        log.error("ERROR! This template is NOT VALID Nifi Template: {} for file {}.  Errors are: {} ", importTemplate.getTemplateName(), importTemplate.getFileName(),
                  importTemplate.getTemplateResults().getAllErrors());
        //delete this template
        importTemplate.setSuccess(false);
        //delete the template from NiFi
        nifiRestClient.deleteTemplate(dto.getId());
        //restore old template
        if (oldTemplateXml != null) {
            log.info("Rollback Nifi: Attempt to restore old template xml ");
            nifiRestClient.importTemplate(oldTemplateXml);
            log.info("Rollback Nifi: restored old template xml ");
        }
        log.info("Rollback Nifi:  Deleted the template: {}  from Nifi ", importTemplate.getTemplateName());

    }

    /**
     * Cleanup and remove the temporary process group associated with this import
     *
     * This should be called after the import to cleanup NiFi
     */
    private void removeTemporaryProcessGroup(ImportTemplate importTemplate) {
        UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importTemplate.getImportOptions().getUploadKey(), "Cleaning up the temporary process group in NiFi");
        String processGroupName = importTemplate.getTemplateResults().getProcessGroupEntity().getName();
        String processGroupId = importTemplate.getTemplateResults().getProcessGroupEntity().getId();
        String parentProcessGroupId = importTemplate.getTemplateResults().getProcessGroupEntity().getParentGroupId();
        log.info("About to cleanup the temporary process group {} ({})", processGroupName, processGroupId);
        try {
            //Now try to remove the processgroup associated with this template import
            ProcessGroupDTO e = null;
            try {
                e = nifiRestClient.getProcessGroup(processGroupId, false, false);
            } catch (NifiComponentNotFoundException notFound) {
                //if its not there then we already cleaned up :)
            }
            if (e != null) {
                try {
                    nifiRestClient.stopAllProcessors(processGroupId, parentProcessGroupId);
                    //remove connections
                    nifiRestClient.removeConnectionsToProcessGroup(parentProcessGroupId, processGroupId);
                } catch (Exception e2) {
                    //this is ok. we are cleaning up the template so if an error occurs due to no connections it is fine since we ultimately want to remove this temp template.
                }
                try {
                    nifiRestClient.deleteProcessGroup(importTemplate.getTemplateResults().getProcessGroupEntity());
                } catch (NifiComponentNotFoundException nfe) {
                    //this is ok
                }
            }
            log.info("Successfully cleaned up Nifi and deleted the process group {} ", importTemplate.getTemplateResults().getProcessGroupEntity().getName());
            statusMessage.update("Cleaned up NiFi", true);
        } catch (NifiClientRuntimeException e) {
            log.error("error attempting to cleanup and remove the temporary process group (" + processGroupId + " during the import of template " + importTemplate.getTemplateName());
            importTemplate.getTemplateResults().addError(NifiError.SEVERITY.WARN,
                                                         "Issues found in cleaning up the template import: " + importTemplate.getTemplateName() + ".  The Process Group : " + processGroupName + " ("
                                                         + processGroupId + ")"
                                                         + " may need to be manually cleaned up in NiFi ", "");
            statusMessage.update("Error cleaning up NiFi. The Process Group : " + processGroupName + " ("
                                 + processGroupId + ")", false);
        }
    }


    /**
     * Open the zip file and populate the {@link ImportTemplate} object with the components in the file/archive
     *
     * @param fileName    the file name
     * @param inputStream the file
     * @return the template data to import
     */
    private ImportTemplate openZip(String fileName, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        ImportTemplate importTemplate = new ImportTemplate(fileName);
        while ((zipEntry = zis.getNextEntry()) != null) {
            String zipEntryContents = ZipFileUtil.zipEntryToString(buffer, zis, zipEntry);
            if (zipEntry.getName().startsWith(NIFI_TEMPLATE_XML_FILE)) {
                importTemplate.setNifiTemplateXml(zipEntryContents);
            } else if (zipEntry.getName().startsWith(TEMPLATE_JSON_FILE)) {
                importTemplate.setTemplateJson(zipEntryContents);
            } else if (zipEntry.getName().startsWith(NIFI_CONNECTING_REUSABLE_TEMPLATE_XML_FILE)) {
                importTemplate.addNifiConnectingReusableTemplateXml(zipEntryContents);
            }
        }
        zis.closeEntry();
        zis.close();
        if (!importTemplate.hasValidComponents()) {
            throw new UnsupportedOperationException(
                " The file you uploaded is not a valid archive.  Please ensure the Zip file has been exported from the system and has 2 valid files named: " + NIFI_TEMPLATE_XML_FILE + ", and "
                + TEMPLATE_JSON_FILE);
        }
        importTemplate.setZipFile(true);
        return importTemplate;

    }

    private ImportTemplate getNewNiFiTemplateImport(String fileName, InputStream inputStream) throws IOException {
        ImportTemplate template = new ImportTemplate(fileName);
        template.setValid(true);
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        String xmlTemplate = writer.toString();
        template.setNifiTemplateXml(xmlTemplate);
        return template;
    }

    /**
     * Called when the system imports a Reusable template either from a ZIP file or an xml file uploaded in kylo.
     */
    private void importReusableTemplateSuccess(ImportTemplate importTemplate) {

    }

    //Internal classes


    public static class ImportTemplate {

        String fileName;
        String templateName;
        boolean success;
        private boolean valid;

        private NifiProcessGroup templateResults;
        private List<NiFiComponentErrors> controllerServiceErrors;
        private String templateId;
        private String nifiTemplateId;
        private boolean zipFile;
        private String nifiTemplateXml;
        private String templateJson;
        private List<String> nifiConnectingReusableTemplateXmls = new ArrayList<>();
        private boolean verificationToReplaceConnectingResuableTemplateNeeded;
        private ImportTemplateOptions importOptions;

        @JsonIgnore
        private RegisteredTemplate templateToImport;

        public ImportTemplate() {
        }

        public ImportTemplate(String fileName) {
            this.fileName = fileName;
        }

        public ImportTemplate(String templateName, boolean success) {
            this.templateName = templateName;
            this.success = success;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        @JsonIgnore
        public boolean hasValidComponents() {
            return StringUtils.isNotBlank(templateJson) && StringUtils.isNotBlank(nifiTemplateXml);
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getNifiTemplateXml() {
            return nifiTemplateXml;
        }

        public void setNifiTemplateXml(String nifiTemplateXml) {
            this.nifiTemplateXml = nifiTemplateXml;
        }

        public String getTemplateJson() {
            return templateJson;
        }

        public void setTemplateJson(String templateJson) {
            this.templateJson = templateJson;
        }

        public NifiProcessGroup getTemplateResults() {
            if (templateResults == null) {
                templateResults = new NifiProcessGroup();
            }
            return templateResults;
        }

        public void setTemplateResults(NifiProcessGroup templateResults) {
            this.templateResults = templateResults;
            inspectForControllerServiceErrors();
        }


        public String getFileName() {
            return fileName;
        }

        public boolean isZipFile() {
            return zipFile;
        }

        public void setZipFile(boolean zipFile) {
            this.zipFile = zipFile;
        }

        private void inspectForControllerServiceErrors() {
            if (templateResults != null) {
                List<NiFiComponentErrors> errors = templateResults.getControllerServiceErrors();
                this.controllerServiceErrors = errors;
            }
        }

        public List<NiFiComponentErrors> getControllerServiceErrors() {
            return controllerServiceErrors;
        }

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public String getNifiTemplateId() {
            return nifiTemplateId;
        }

        public void setNifiTemplateId(String nifiTemplateId) {
            this.nifiTemplateId = nifiTemplateId;
        }

        public List<String> getNifiConnectingReusableTemplateXmls() {
            return nifiConnectingReusableTemplateXmls;
        }

        public void addNifiConnectingReusableTemplateXml(String nifiConnectingReusableTemplateXml) {
            this.nifiConnectingReusableTemplateXmls.add(nifiConnectingReusableTemplateXml);
        }

        public boolean hasConnectingReusableTemplate() {
            return !nifiConnectingReusableTemplateXmls.isEmpty();
        }

        public boolean isVerificationToReplaceConnectingResuableTemplateNeeded() {
            return verificationToReplaceConnectingResuableTemplateNeeded;
        }

        public void setVerificationToReplaceConnectingResuableTemplateNeeded(boolean verificationToReplaceConnectingResuableTemplateNeeded) {
            this.verificationToReplaceConnectingResuableTemplateNeeded = verificationToReplaceConnectingResuableTemplateNeeded;
        }

        public ImportTemplateOptions getImportOptions() {
            return importOptions;
        }

        public void setImportOptions(ImportTemplateOptions importOptions) {
            this.importOptions = importOptions;
        }

        @JsonIgnore
        public RegisteredTemplate getTemplateToImport() {
            if (templateToImport == null && StringUtils.isNotBlank(templateJson)) {
                templateToImport = ObjectMapperSerializer.deserialize(getTemplateJson(), RegisteredTemplate.class);

            }
            return templateToImport;
        }

        @JsonIgnore
        public void setTemplateToImport(RegisteredTemplate templateToImport) {
            this.templateToImport = templateToImport;
        }
    }

    public class ExportTemplate {

        private String fileName;
        private byte[] file;

        public ExportTemplate(String fileName, byte[] file) {
            this.fileName = fileName;
            this.file = file;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getFile() {
            return file;
        }
    }

    /**
     * Store data about a NiFi import
     */
    private class NiFiTemplateImport {

        private String oldTemplateXml;
        private TemplateDTO dto;

        public NiFiTemplateImport(String oldTemplateXml, TemplateDTO dto) {
            this.oldTemplateXml = oldTemplateXml;
            this.dto = dto;
        }

        public String getOldTemplateXml() {
            return oldTemplateXml;
        }

        public void setOldTemplateXml(String oldTemplateXml) {
            this.oldTemplateXml = oldTemplateXml;
        }

        public TemplateDTO getDto() {
            return dto;
        }

        public void setDto(TemplateDTO dto) {
            this.dto = dto;
        }
    }


    public class NifiFlowCacheReusableTemplateCreationCallback implements ReusableTemplateCreationCallback {

        private boolean isXmlImport;

        public NifiFlowCacheReusableTemplateCreationCallback(boolean isXmlImport) {
            this.isXmlImport = isXmlImport;
        }

        /**
         * Update the NiFi Flow Cache with the new processors information
         *
         * @param templateName    the name of the template
         * @param processGroupDTO the group where this template resides (under the reusable_templates) group
         */
        @Override
        public void beforeMarkAsRunning(String templateName, ProcessGroupDTO processGroupDTO) {
            //update the cache
            Collection<ProcessorDTO> processors = NifiProcessUtil.getProcessors(processGroupDTO);
            nifiFlowCache.updateProcessorIdNames(templateName, processors);
            nifiFlowCache.updateConnectionMap(templateName, NifiConnectionUtil.getAllConnections(processGroupDTO));
        }

    }


}
