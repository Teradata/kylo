package com.thinkbiganalytics.feedmgr.service;

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
import com.thinkbiganalytics.feedmgr.nifi.NifiControllerServiceProperties;
import com.thinkbiganalytics.feedmgr.nifi.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.NifiTemplateParser;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportType;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
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


    @Inject
    NifiControllerServiceProperties nifiControllerServiceProperties;

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


    /**
     * Called when the system imports a Reusable template either from a ZIP file or an xml file uploaded in kylo.
     */
    private void importReusableTemplateSuccess(ImportTemplate importTemplate) {

    }

    public ExportTemplate exportTemplate(String templateId) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EXPORT_TEMPLATES);

        RegisteredTemplate template = registeredTemplateService.findRegisteredTemplate(new RegisteredTemplateRequest.Builder().templateId(templateId).nifiTemplateId(templateId).includeSensitiveProperties(true).build());
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

    private boolean isValidFileImport(String fileName) {
        return fileName.endsWith(".zip") || fileName.endsWith(".xml");
    }



    private void analyzeReusableTemplateForImport(ImportTemplate importTemplate, ImportOptions importOptions) throws Exception {

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
                    importTemplate.getImportOptions().addErrorMessage(ImportComponent.REUSABLE_TEMPLATE,"Additional Properties are required for input on the reusable template");
                }

            }
            importOptions.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setAnalyzed(true);
        }
    }

    private void validateNifiTemplateImport(ImportTemplate template) throws IOException {
        ImportOptions options = template.getImportOptions();
        ImportComponentOption nifiTemplateOption = options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE);
        if(nifiTemplateOption.isUserAcknowledged() && nifiTemplateOption.isShouldImport()) {
            String templateName = null;
           TemplateDTO dto = null;
            try {
                templateName = NifiTemplateParser.getTemplateName(template.getNifiTemplateXml());
                template.setTemplateName(templateName);
                dto = nifiRestClient.getTemplateByName(templateName);
                if (dto != null) {
                    template.setNifiTemplateId(dto.getId());
                    if (!options.isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE) && !options.isContinueIfExists(ImportComponent.NIFI_TEMPLATE)) {
                        template.setValid(false);
                        String msg = "Unable to import Template " + templateName
                                     + ".  It already exists in NiFi.  Please check that you wish to overwrite this template and try to import again.";
                        template.getImportOptions().addErrorMessage(ImportComponent.NIFI_TEMPLATE,msg);
                    }
                }
            } catch (ParserConfigurationException | XPathExpressionException | SAXException e) {
                template.setValid(false);
                template.getTemplateResults().addError(NifiError.SEVERITY.WARN,   "The xml file you are trying to import is not a valid NiFi template.  Please try again. " + e.getMessage(),"");
            }

        }
    }


    //validate
    public ImportTemplate validateTemplateForImport(final String fileName, byte[] content, ImportOptions importOptions ) {

            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_TEMPLATES);
            InputStream inputStream = new ByteArrayInputStream(content);

            ImportTemplateOptions options = new ImportTemplateOptions();
            ImportTemplate template = null;
            if (!isValidFileImport(fileName)) {
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

                    //validate the reusable template
                    if(template.hasConnectingReusableTemplate() ){
                        ImportComponentOption reusableTemplateOption = options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE);

                        if(reusableTemplateOption.isUserAcknowledged() && reusableTemplateOption.isShouldImport()) {
                            String templateName = NifiTemplateParser.getTemplateName(template.getNifiTemplateXml());
                            TemplateDTO dto = nifiRestClient.getTemplateByName(templateName);
                            if(dto != null && !reusableTemplateOption.isOverwrite()) {
                                //error out it exists
                                template.getImportOptions().addErrorMessage(ImportComponent.REUSABLE_TEMPLATE,"A reusable template with the same name "+templateName+". Please choose to overwrite if you want to import it");
                                template.setValid(false);
                            }
                            else if(!reusableTemplateOption.isAnalyzed()){
                                //analyze it to ensure sensitive properties are parsed
                                analyzeReusableTemplateForImport(template,options);
                            }
                        }


                    }
                    ImportComponentOption registeredTemplateOption = options.findImportComponentOption(ImportComponent.TEMPLATE_DATA);
                    //validate template

                    if(!registeredTemplateOption.isUserAcknowledged()) {
                        template.setValid(false);
                        template.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA,"A template exists.  Do you want to import it?");

                    }

                    if(registeredTemplateOption.isUserAcknowledged() && registeredTemplateOption.isShouldImport()) {
                        RegisteredTemplate registeredTemplate = ObjectMapperSerializer.deserialize(template.getTemplateJson(), RegisteredTemplate.class);
                        if (!registeredTemplateOption.isAnalyzed()) {
                            assessSensitiveProperties(registeredTemplate, template, importOptions);
                        }
                        //validate unique
                        RegisteredTemplate existingTemplate = registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateName(template.getTemplateName()));
                        if (existingTemplate != null) {
                            if (importOptions.stopProcessingAlreadyExists(ImportComponent.TEMPLATE_DATA)) {
                                template.setValid(false);
                                String msg = "Unable to import the template " + template.getTemplateName() + " because it is already registered.  Please click the overwrite box and try again";
                                template.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA,msg);
                            }
                            registeredTemplate.setId(existingTemplate.getId());
                        } else {
                            //reset it so it gets the new id upon import
                            registeredTemplate.setId(null);
                        }
                    }

                    validateNifiTemplateImport(template);
                }
                else {
                    template = nifiTemplateImport(fileName,inputStream);
                    template.setImportOptions(options);
                    validateNifiTemplateImport(template);
                }
            } catch (Exception e) {
                throw new UnsupportedOperationException("Error importing template  " + fileName + ".  " + e.getMessage());
            }

            return template;
    }


    private boolean assessSensitiveProperties( RegisteredTemplate template, ImportTemplate importTemplate, ImportOptions importOptions) {
        //detect any sensitive properties and prompt for input before proceeding
        List<NifiProperty> sensitiveProperties = template.getSensitiveProperties();
        ImportUtil.addToImportOptionsSensitiveProperties(importOptions, sensitiveProperties, ImportComponent.TEMPLATE_DATA);
       boolean valid = ImportUtil.applyImportPropertiesToTemplate(template, importTemplate, ImportComponent.TEMPLATE_DATA);
        if(!valid) {
            importTemplate.getImportOptions().addErrorMessage(ImportComponent.TEMPLATE_DATA,"Additional properties are required for the Template");
        }
        importOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setAnalyzed(true);
       return valid;
    }


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

    /**
     * Import a template string into NiFi as a template to use
     * @param importOptions
     * @param template
     * @return
     * @throws IOException
     */
    private NiFiTemplateImport importNiFiXmlTemplate(ImportTemplateOptions importOptions, ImportTemplate template) throws IOException {

        TemplateDTO dto = null;
        String templateName = null;
        String  oldTemplateXml = null;

        ImportComponentOption nifiTemplateImport = importOptions.findImportComponentOption(ImportComponent.NIFI_TEMPLATE);
        if(nifiTemplateImport.isShouldImport()) {
            try {
                templateName = NifiTemplateParser.getTemplateName(template.getNifiTemplateXml());
                template.setTemplateName(templateName);
                dto = nifiRestClient.getTemplateByName(templateName);
                if (dto != null) {
                    oldTemplateXml = nifiRestClient.getTemplateXml(dto.getId());
                    template.setNifiTemplateId(dto.getId());
                    if (importOptions.isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE)) {
                        nifiRestClient.deleteTemplate(dto.getId());
                    }
                }
            } catch (ParserConfigurationException | XPathExpressionException | SAXException e) {
                throw new UnsupportedOperationException("The xml file you are trying to import is not a valid NiFi template.  Please try again. " + e.getMessage());
            }

            boolean register = (dto == null || (importOptions.isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE)));
            //attempt to import the xml into NiFi if its new, or if the user said to overwrite
            if (register) {
                log.info("Attempting to import Nifi Template: {} for file {}", templateName, template.getFileName());
                dto = nifiRestClient.importTemplate(template.getTemplateName(), template.getNifiTemplateXml());
                template.setNifiTemplateId(dto.getId());
            }
        }

            return new NiFiTemplateImport(oldTemplateXml,dto);

    }




    public ImportTemplate importZip(String fileName, byte[] content, ImportTemplateOptions importOptions) throws IOException {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_TEMPLATES);


        ImportTemplate importTemplate = validateTemplateForImport(fileName,content,importOptions);

        log.info("Importing Zip file template {}, overwrite: {}, reusableFlow: {}", fileName, importOptions.isImportAndOverwrite(ImportComponent.TEMPLATE_DATA), importOptions.isImport(ImportComponent.REUSABLE_TEMPLATE));
        if(importTemplate.isValid()){
            List<ImportTemplate> connectingTemplates = new ArrayList<>();
            //start the import
            if (importTemplate.hasConnectingReusableTemplate() && importOptions.isImportAndOverwrite(ImportComponent.REUSABLE_TEMPLATE)) {

                //import the reusable templates
                log.info("Importing Zip file template {}. first importing reusable flow from zip");
                for (String reusableTemplateXml : importTemplate.getNifiConnectingReusableTemplateXmls()) {
                    ImportTemplate connectingTemplate = importNifiTemplateWithTemplateString(importTemplate.getFileName(), reusableTemplateXml, importOptions,
                                                                                             false);
                    if (!connectingTemplate.isSuccess()) {
                        //return with exception
                        return connectingTemplate;
                    } else {
                        connectingTemplates.add(connectingTemplate);
                    }
                }
            }

            //
            RegisteredTemplate template = ObjectMapperSerializer.deserialize(importTemplate.getTemplateJson(), RegisteredTemplate.class);
            assessSensitiveProperties(template,importTemplate,importOptions);
            //1 ensure this template doesnt already exist
            importTemplate.setTemplateName(template.getTemplateName());

            RegisteredTemplate existingTemplate =  registeredTemplateService.findRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateName(template.getTemplateName()));
            if (existingTemplate != null) {
                template.setId(existingTemplate.getId());
            } else {
                template.setId(null);
            }



            NiFiTemplateImport niFiTemplateImport = importNiFiXmlTemplate(importOptions, importTemplate);
            String oldTemplateXml = niFiTemplateImport.getOldTemplateXml();
            TemplateDTO dto = niFiTemplateImport.getDto();
            String templateName = importTemplate.getTemplateName();
            template.setNifiTemplateId(dto.getId());
            ImportComponentOption registeredTemplateImport = importOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA);
            if(registeredTemplateImport.isShouldImport()){


                //Create the new instance of the template in NiFi
                Map<String, Object> configProperties = propertyExpressionResolver.getStaticConfigProperties();
                NifiProcessGroup newTemplateInstance =
                    nifiRestClient.createNewTemplateInstance(template.getNifiTemplateId(), configProperties, false, new NifiFlowCacheReusableTemplateCreationCallback(false));
                importTemplate.setTemplateResults(newTemplateInstance);

                if (newTemplateInstance.isSuccess()) {
                    importTemplate.setSuccess(true);

                    try {
                        importTemplate.setNifiTemplateId(template.getNifiTemplateId());
                        //register it in the system
                        metadataService.registerTemplate(template);
                        //get the new template
                        template = registeredTemplateService.findRegisteredTemplate(new RegisteredTemplateRequest.Builder().templateId(template.getId()).templateName(template.getTemplateName()).build());
                        importTemplate.setTemplateId(template.getId());


                    } catch (Exception e) {
                        importTemplate.setSuccess(false);
                        Throwable root = ExceptionUtils.getRootCause(e);
                        String msg = root != null ? root.getMessage() : e.getMessage();
                        importTemplate.getTemplateResults().addError(NifiError.SEVERITY.WARN, "Error registering the template " + template.getTemplateName() + " in the Kylo metadata. " + msg, "");
                    }

                }
                if (!importTemplate.isSuccess()) {
                    rollbackTemplateImportInNifi(importTemplate, dto, oldTemplateXml);
                    //also restore existing registered template with metadata
                    //restore old registered template
                    if (existingTemplate != null) {
                        try {
                            metadataService.registerTemplate(existingTemplate);
                        } catch (Exception e) {
                            Throwable root = ExceptionUtils.getRootCause(e);
                            String msg = root != null ? root.getMessage() : e.getMessage();
                            importTemplate.getTemplateResults().addError(NifiError.SEVERITY.WARN, "Error while restoring the template " + template.getTemplateName() + " in the Kylo metadata. " + msg, "");
                        }
                    }
                }

                //remove the temporary Process Group we created
                removeTemporaryProcessGroup(importTemplate);



            }

            //if we also imported the reusable template make sure that is all running properly
            for (ImportTemplate connectingTemplate : connectingTemplates) {
                //enable it
                nifiRestClient.markConnectionPortsAsRunning(connectingTemplate.getTemplateResults().getProcessGroupEntity());
            }

        }

        return importTemplate;
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
        ImportTemplate importTemplate  = nifiTemplateImport(fileName,inputStream);
        importTemplate.setImportOptions(importOptions);

        validateNifiTemplateImport(importTemplate);
        if(importTemplate.isValid()) {
            boolean createReusableFlow = importOptions.isImport(ImportComponent.REUSABLE_TEMPLATE);
            boolean overwrite = importOptions.isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE);
            log.info("Importing XML file template {}, overwrite: {}, reusableFlow: {}", fileName, overwrite, createReusableFlow);


            NiFiTemplateImport niFiTemplateImport = importNiFiXmlTemplate(importOptions, importTemplate);
            String oldTemplateXml = niFiTemplateImport.getOldTemplateXml();
            TemplateDTO dto = niFiTemplateImport.getDto();
            String templateName = importTemplate.getTemplateName();

            //import it as a flow


            log.info("Import success... validate by creating a template instance in nifi Nifi Template: {} for file {}", templateName, fileName);
            Map<String, Object> configProperties = propertyExpressionResolver.getStaticConfigProperties();
            NifiFlowCacheReusableTemplateCreationCallback reusableTemplateCreationCallback = new NifiFlowCacheReusableTemplateCreationCallback(xmlImport);
            NifiProcessGroup newTemplateInstance = nifiRestClient.createNewTemplateInstance(dto.getId(), configProperties, createReusableFlow, reusableTemplateCreationCallback);
            importTemplate.setTemplateResults(newTemplateInstance);
            log.info("Import finished for {}, {}... verify results", templateName, fileName);
            if (newTemplateInstance.isSuccess()) {
                log.info("SUCCESS! This template is valid Nifi Template: {} for file {}", templateName, fileName);
                importTemplate.setSuccess(true);
                if (createReusableFlow) {
                    importReusableTemplateSuccess(importTemplate);
                }
            } else {
                rollbackTemplateImportInNifi(importTemplate, dto, oldTemplateXml);
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
            log.info("Import all finished");

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
        } catch (NifiClientRuntimeException e) {
            log.error("error attempting to cleanup and remove the temporary process group (" + processGroupId + " during the import of template " + importTemplate.getTemplateName());
            importTemplate.getTemplateResults().addError(NifiError.SEVERITY.WARN,
                                                         "Issues found in cleaning up the template import: " + importTemplate.getTemplateName() + ".  The Process Group : " + processGroupName + " ("
                                                         + processGroupId + ")"
                                                         + " may need to be manually cleaned up in NiFi ", "");
        }
    }

    public ImportTemplate importTemplate(final String fileName, final byte[] content, ImportTemplateOptions importOptions) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_TEMPLATES);

            ImportTemplate template = null;
            if (!isValidFileImport(fileName)) {
                throw new UnsupportedOperationException("Unable to import " + fileName + ".  The file must be a zip file or a Nifi Template xml file");
            }
            try {
                if (fileName.endsWith(".zip")) {
                    template = importZip(fileName, content, importOptions); //dont allow exported reusable flows to become registered templates
                } else if (fileName.endsWith(".xml")) {
                    template = importNifiTemplate(fileName, content, importOptions, true);
                }
            } catch (IOException e) {
                throw new UnsupportedOperationException("Error importing template  " + fileName + ".  " + e.getMessage());
            }
            template.setImportOptions(importOptions);
            return template;
        });
    }


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

    private ImportTemplate nifiTemplateImport(String fileName, InputStream inputStream) throws IOException {
        ImportTemplate  template = new ImportTemplate(fileName);
        template.setValid(true);
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        String xmlTemplate = writer.toString();
        template.setNifiTemplateXml(xmlTemplate);
        return template;
    }


    public Set<ImportComponent> inspectZipComponents(String fileName, InputStream inputStream) throws IOException {

        Set<ImportComponent> importComponents = new HashSet<>();
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            log.info("zip file entry: " + entry.getName());
            if (entry.getName().startsWith(NIFI_TEMPLATE_XML_FILE)) {
                importComponents.add(ImportComponent.NIFI_TEMPLATE);
            } else if (entry.getName().startsWith(TEMPLATE_JSON_FILE)) {
                importComponents.add(ImportComponent.TEMPLATE_DATA);
            } else if (entry.getName().startsWith(NIFI_CONNECTING_REUSABLE_TEMPLATE_XML_FILE)) {
                importComponents.add(ImportComponent.REUSABLE_TEMPLATE);
            }
        }
        zis.closeEntry();
        zis.close();

        return importComponents;
    }




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

        public ImportTemplate() {}

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
            if(templateResults == null){
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
