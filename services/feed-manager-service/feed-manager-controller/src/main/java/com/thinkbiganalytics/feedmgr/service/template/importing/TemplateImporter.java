package com.thinkbiganalytics.feedmgr.service.template.importing;
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
import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportSection;
import com.thinkbiganalytics.feedmgr.rest.ImportType;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgress;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.ImportException;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportTemplateRoutine;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportTemplateRoutineFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.validation.AbstractValidateImportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.validation.ValidateImportTemplateFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.validation.ValidateImportTemplatesArchive;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.feedmgr.util.UniqueIdentifier;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.security.AccessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Created by sr186054 on 12/11/17.
 */
public class TemplateImporter {

    private static final Logger log = LoggerFactory.getLogger(ValidateImportTemplatesArchive.class);

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
    private ValidateImportTemplateFactory validateImportTemplateFactory;

    @Inject
    private ImportTemplateRoutineFactory importTemplateRoutineFactory;

    private ImportTemplate.TYPE importType;


    protected ImportTemplate importTemplate;
    protected ImportTemplateOptions importTemplateOptions;
    /**
     * User supplied import options
     */
    protected ImportOptions importOptions;
    protected String fileName;
    protected byte[] file;
    protected UploadProgressMessage overallStatusMessage;


    public TemplateImporter(String fileName, byte[] file, ImportOptions importOptions) {
        this.fileName = fileName;
        this.file = file;
        this.importOptions = importOptions;
    }


    public ImportTemplate validate() {

        try {
            init();
            AbstractValidateImportTemplate validateImportTemplate = validateImportTemplateFactory.apply(this.importTemplate, this.importTemplateOptions, importType);
            validateImportTemplate.validate();
        } catch (Exception e) {
            this.overallStatusMessage.update("An Error occurred " + e.getMessage(), false);
            throw new UnsupportedOperationException("Error importing template  " + fileName + ".  " + e.getMessage());
        }
        overallStatusMessage.update("Validated template for import ", this.importTemplate.isValid());

        return this.importTemplate;
    }

    public ImportTemplate validateAndImport() {
        validate();
        if (this.importTemplate.isValid()) {

            ImportTemplateRoutine routine = importTemplateRoutineFactory.apply(importTemplate, importTemplateOptions, importType);
            routine.importTemplate();
        }
        return importTemplate;

    }


    private void init() throws ImportException {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.IMPORT_TEMPLATES);

        overallStatusMessage = uploadProgressService.addUploadStatus(this.importOptions.getUploadKey(), "Validating template for import");

        //copy the options to ImportTemplateOptions
        ImportTemplateOptions options = new ImportTemplateOptions();
        options.setUploadKey(this.importOptions.getUploadKey());
        options.setImportComponentOptions(this.importOptions.getImportComponentOptions());

        if (!isValidFileImport(fileName)) {
            this.overallStatusMessage.complete(false);
            throw new ImportException("Unable to import " + fileName + ".  The file must be a zip file or a Nifi Template xml file");
        }
        this.importTemplateOptions = options;
        setImportType();

        if (importType == ImportTemplate.TYPE.ARCHIVE) {
            initializeImportTemplateArchive();
        } else {
            initializeImportTemplateFromXml();
        }


    }

    private void initializeImportTemplateArchive() throws ImportException {
        try {
            UploadProgress progress = uploadProgressService.getUploadStatus(importOptions.getUploadKey());
            progress.setSections(ImportSection.sectionsForImportAsString(ImportType.TEMPLATE));

            InputStream inputStream = new ByteArrayInputStream(file);
            this.importTemplate = ImportUtil.openZip(fileName, inputStream);
            this.importTemplate.setValid(true);
            Set<ImportComponentOption> componentOptions = ImportUtil.inspectZipComponents(file, ImportType.TEMPLATE);
            importTemplateOptions.addOptionsIfNotExists(componentOptions);
            importTemplateOptions.findImportComponentOption(ImportComponent.TEMPLATE_CONNECTION_INFORMATION).addConnectionInfo(importTemplate.getReusableTemplateConnections());
            importTemplate.setImportOptions(this.importTemplateOptions);
        } catch (Exception e) {
            throw new ImportException("Unable to open template archive " + fileName + " for import. ", e);
        }
    }

    private void initializeImportTemplateFromXml() throws ImportException {
        try {
            UploadProgress progress = uploadProgressService.getUploadStatus(importOptions.getUploadKey());
            progress.setSections(ImportSection.sectionsForImportAsString(ImportType.TEMPLATE_XML));

            InputStream inputStream = new ByteArrayInputStream(file);
            this.importTemplate = ImportUtil.getNewNiFiTemplateImport(fileName, inputStream);
            importTemplate.setImportOptions(this.importTemplateOptions);
        } catch (IOException e) {
            throw new ImportException(e);
        }
    }

    private void setImportType() {

        if (isZip()) {
            importType = ImportTemplate.TYPE.ARCHIVE;
        } else {
            ImportComponentOption registeredTemplateOption = importTemplateOptions.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE);
            if (registeredTemplateOption.isUserAcknowledged() && registeredTemplateOption.isShouldImport()) {
                importType = ImportTemplate.TYPE.REUSABLE_TEMPLATE;
            } else {
                importType = ImportTemplate.TYPE.XML;
            }
        }
    }


    private boolean isZip() {
        return fileName.endsWith(".zip");
    }


    /**
     * Is the file a valid file for importing
     *
     * @param fileName the name of the file
     * @return true if valid, false if not valid
     */
    private boolean isValidFileImport(String fileName) {
        return fileName.endsWith(".zip") || fileName.endsWith(".xml");
    }


}
