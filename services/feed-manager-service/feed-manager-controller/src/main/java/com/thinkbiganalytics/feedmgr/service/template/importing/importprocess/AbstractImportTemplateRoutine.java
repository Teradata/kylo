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
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportSection;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.importing.ImportException;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.NiFiTemplateImport;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;

import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Created by sr186054 on 12/11/17.
 */
public abstract class AbstractImportTemplateRoutine implements ImportTemplateRoutine {

    private static final Logger log = LoggerFactory.getLogger(AbstractImportTemplateRoutine.class);

    @Inject
    protected UploadProgressService uploadProgressService;

    @Inject
    private LegacyNifiRestClient nifiRestClient;

    protected ImportTemplate importTemplate;

    protected ImportTemplateOptions importTemplateOptions;

    protected NifiProcessGroup newTemplateInstance;

    /**
     * NiFi TemplateDTO with the name;
     */
    protected NiFiTemplateImport niFiTemplateImport;

    public abstract boolean importTemplate();


    public AbstractImportTemplateRoutine(ImportTemplate importTemplate, ImportTemplateOptions importTemplateOptions) {
        this.importTemplate = importTemplate;
        this.importTemplateOptions = importTemplateOptions;
        this.importTemplate.setImportOptions(importTemplateOptions);
    }

    public AbstractImportTemplateRoutine(String fileName, byte[] xmlFile, ImportTemplateOptions importTemplateOptions) {
        this.importTemplateOptions = importTemplateOptions;
        initializeImportTemplateFromXml(fileName, xmlFile);
    }

    private void initializeImportTemplateFromXml(String fileName, byte[] xmlFile) throws ImportException {
        try {
            InputStream inputStream = new ByteArrayInputStream(xmlFile);
            this.importTemplate = ImportUtil.getNewNiFiTemplateImport(fileName, inputStream);
            importTemplate.setImportOptions(this.importTemplateOptions);
        } catch (IOException e) {
            throw new ImportException(e);
        }
    }

    public UploadProgressMessage start() {
        return uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(), "Importing the NiFi flow ");
    }


    public boolean importIntoNiFiAndCreateInstance() {
        UploadProgressMessage importStatusMessage = start();
        this.niFiTemplateImport = importIntoNiFi(importTemplate, importTemplateOptions);
        uploadProgressService.completeSection(importTemplateOptions, ImportSection.Section.IMPORT_NIFI_TEMPLATE);
        this.newTemplateInstance = create(this.niFiTemplateImport, importStatusMessage);
        importTemplate.setSuccess(newTemplateInstance.isSuccess());
        importStatusMessage.update("Created template instance for " + importTemplate.getTemplateName(), importTemplate.isSuccess());
        uploadProgressService.completeSection(importTemplateOptions, ImportSection.Section.CREATE_NIFI_INSTANCE);
        return importTemplate.isSuccess();
    }

    public boolean connectAndValidate() {
        return importTemplate.isSuccess();
    }


    /**
     * Import a template string into NiFi as a template to use
     */
    public NiFiTemplateImport importIntoNiFi(ImportTemplate template, ImportTemplateOptions importOptions) throws ImportException {

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
                    } else if (!template.isZipFile()) {
                        //if its not a zip file we need to error out if the user has decided not to overwrite when it already exists
                        uploadProgressService
                            .addUploadStatus(importOptions.getUploadKey(), "The template " + templateName + " already exists in NiFi. Please choose the option to replace the template and try again.",
                                             true, false);
                        template.setValid(false);
                    }
                }
            } catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e) {
                throw new ImportException("The xml file you are trying to import is not a valid NiFi template.  Please try again. " + e.getMessage());
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

        uploadProgressService.completeSection(importOptions, ImportSection.Section.IMPORT_NIFI_TEMPLATE);
        return new NiFiTemplateImport(oldTemplateXml, dto);

    }

    protected UploadProgressMessage restoreOldTemplateXml() {
        UploadProgressMessage
            rollbackMessage =
            uploadProgressService.addUploadStatus(importTemplate.getImportOptions().getUploadKey(), "Attempting to rollback the template xml in NiFi " + importTemplate.getTemplateName());
        if (niFiTemplateImport != null) {
            TemplateDTO dto = niFiTemplateImport.getDto();
            String oldTemplateXml = niFiTemplateImport.getOldTemplateXml();

            log.error("ERROR! This template is NOT VALID Nifi Template: {} for file {}.  Errors are: {} ", importTemplate.getTemplateName(), importTemplate.getFileName(),
                      importTemplate.getTemplateResults().getAllErrors());
            //delete this template
            importTemplate.setSuccess(false);
            //delete the template from NiFi
            nifiRestClient.deleteTemplate(dto.getId());
            //restore old template
            if (oldTemplateXml != null) {
                UploadProgressMessage
                    progressMessage =
                    uploadProgressService.addUploadStatus(importTemplate.getImportOptions().getUploadKey(), "Attempting to restore old template xml for: " + importTemplate.getTemplateName());
                log.info("Rollback Nifi: Attempt to restore old template xml ");
                nifiRestClient.importTemplate(oldTemplateXml);
                log.info("Rollback Nifi: restored old template xml ");
                progressMessage.update("Rollback Status: Restored old template xml for " + importTemplate.getTemplateName(), true);
            }

        }
        return rollbackMessage;

    }

    /**
     * Cleanup and remove the temporary process group associated with this import
     *
     * This should be called after the import to cleanup NiFi
     */
    public void removeTemporaryProcessGroup() {
        UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importTemplate.getImportOptions().getUploadKey(), "Cleaning up the temporary process group in NiFi");
        String processGroupName = importTemplate.getTemplateResults().getProcessGroupEntity().getName();
        String processGroupId = importTemplate.getTemplateResults().getProcessGroupEntity().getId();
        String parentProcessGroupId = importTemplate.getTemplateResults().getProcessGroupEntity().getParentGroupId();
        log.info("About to cleanup the temporary process group {} ({})", processGroupName, processGroupId);
        try {
            nifiRestClient.removeProcessGroup(processGroupId, parentProcessGroupId);

            log.info("Successfully cleaned up Nifi and deleted the process group {} ", importTemplate.getTemplateResults().getProcessGroupEntity().getName());
            statusMessage.update("Removed the temporary process group " + processGroupName + " in NiFi", true);
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


    public ImportTemplate getImportTemplate() {
        return importTemplate;
    }

    public ImportTemplateOptions getImportTemplateOptions() {
        return importTemplateOptions;
    }
}
