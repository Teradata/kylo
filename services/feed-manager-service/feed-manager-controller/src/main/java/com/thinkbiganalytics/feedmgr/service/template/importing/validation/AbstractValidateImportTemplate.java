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
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportSection;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.security.AccessController;

import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Created by sr186054 on 12/11/17.
 */
public abstract class AbstractValidateImportTemplate {

    @Inject
    protected AccessController accessController;

    @Inject
    protected UploadProgressService uploadProgressService;

    @Inject
    protected LegacyNifiRestClient nifiRestClient;

    protected ImportTemplate importTemplate;
    protected ImportTemplateOptions importTemplateOptions;
    protected String fileName;
    protected byte[] file;

    public AbstractValidateImportTemplate(ImportTemplate importTemplate, ImportTemplateOptions importTemplateOptions){
        this.importTemplate = importTemplate;
        this.importTemplateOptions = importTemplateOptions;
    }

    public abstract  boolean validate();

    public abstract Logger getLogger();


    /**
     * Validate the NiFi template is valid.  This method will validate the template can be created/overwritten based upon the user supplied properties
     *
     */
    public void validateNiFiTemplateImport()  {
        //ImportOptions options = template.getImportOptions();
        ImportComponentOption nifiTemplateOption = this.importTemplateOptions.findImportComponentOption(ImportComponent.NIFI_TEMPLATE);
        //if the options of the TEMPLATE_DATA are marked to import and overwrite this should be as well
        ImportComponentOption templateData = this.importTemplateOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA);

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
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(), "Validating the NiFi template");
            String templateName = null;
            TemplateDTO dto = null;
            try {
                templateName = NifiTemplateParser.getTemplateName(this.importTemplate.getNifiTemplateXml());
                this.importTemplate.setTemplateName(templateName);
                dto = nifiRestClient.getNiFiRestClient().templates().findByName(templateName).orElse(null);
                if (dto != null) {
                    this.importTemplate.setNifiTemplateId(dto.getId());
                    //if the template incoming is an XML template and it already exists, or if its a zip file and it exists and the user has not acknowledge to overwrite then error out
                    if ((!this.importTemplateOptions.isUserAcknowledged(ImportComponent.NIFI_TEMPLATE) || this.importTemplateOptions.isUserAcknowledged(ImportComponent.NIFI_TEMPLATE) && !this.importTemplate.isZipFile()) && !this.importTemplateOptions
                        .isImportAndOverwrite(ImportComponent.NIFI_TEMPLATE) && !this.importTemplateOptions
                        .isContinueIfExists(ImportComponent.NIFI_TEMPLATE)) {
                        this.importTemplate.setValid(false);
                        String msg = "Unable to import Template " + templateName
                                     + ".  It already exists in NiFi.";
                        this.importTemplate.getImportOptions().addErrorMessage(ImportComponent.NIFI_TEMPLATE, msg);
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
            } catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e) {
                getLogger().error("Error validating the file {} for import ",fileName,e);
                this.importTemplate.setValid(false);
                this.importTemplate.getTemplateResults().addError(NifiError.SEVERITY.WARN, "The xml file you are trying to import is not a valid NiFi template.  Please try again. " + e.getMessage(), "");
                statusMessage.complete(false);
            }
            nifiTemplateOption.setValidForImport(!nifiTemplateOption.hasErrorMessages());
        }
        uploadProgressService.completeSection(importTemplateOptions, ImportSection.Section.VALIDATE_NIFI_TEMPLATE);
    }


}
