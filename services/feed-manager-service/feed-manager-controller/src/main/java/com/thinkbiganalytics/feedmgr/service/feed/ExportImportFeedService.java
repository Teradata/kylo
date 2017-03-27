package com.thinkbiganalytics.feedmgr.service.feed;

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
import com.google.common.collect.Sets;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportSection;
import com.thinkbiganalytics.feedmgr.rest.ImportType;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportFeedOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgress;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.support.ZipFileUtil;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

/**
 * Service used to export and import feeds
 */
public class ExportImportFeedService {

    private static final Logger log = LoggerFactory.getLogger(ExportImportFeedService.class);

    public static final String FEED_JSON_FILE = "feed.json";

    @Inject
    MetadataService metadataService;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    ExportImportTemplateService exportImportTemplateService;

    @Inject
    private AccessController accessController;

    @Inject
    private UploadProgressService uploadProgressService;

    //Export

    /**
     * Export a feed as a zip file
     *
     * @param feedId the id {@link Feed#getId()} of the feed to export
     * @return object containing the zip file with data about the feed.
     */
    public ExportFeed exportFeed(String feedId) throws IOException {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EXPORT_FEEDS);

        FeedMetadata feed = metadataService.getFeedById(feedId);
        RegisteredTemplate template = feed.getRegisteredTemplate();
        ExportImportTemplateService.ExportTemplate exportTemplate = exportImportTemplateService.exportTemplate(feed.getTemplateId());
        //merge zip files
        String feedJson = ObjectMapperSerializer.serialize(feed);
        byte[] zipFile = ZipFileUtil.addToZip(exportTemplate.getFile(), feedJson, FEED_JSON_FILE);
        return new ExportFeed(feed.getSystemFeedName() + ".feed.zip", zipFile);

    }

    //Validate

    /**
     * Validate a feed for importing
     *
     * @param fileName the name of the file to import
     * @param content  the contents of the feed zip file
     * @param options  user options about what/how it should be imported
     * @return the feed data to import
     */
    public ImportFeed validateFeedForImport(final String fileName, byte[] content, ImportFeedOptions options) throws IOException {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_FEEDS);
        ImportFeed importFeed = null;
        UploadProgressMessage feedImportStatusMessage = uploadProgressService.addUploadStatus(options.getUploadKey(), "Validating Feed import.");
        boolean isValid = ZipFileUtil.validateZipEntriesWithRequiredEntries(content, getValidZipFileEntries(), Sets.newHashSet(FEED_JSON_FILE));
        if (!isValid) {
            feedImportStatusMessage.update("Validation error. Feed import error. The zip file you uploaded is not valid feed export.", false);
            throw new ImportFeedException("The zip file you uploaded is not valid feed export.");
        }

        try {
            //get the Feed Data
            importFeed = readFeedJson(fileName, content);
            //initially mark as valid.
            importFeed.setValid(true);
            //merge in the file components to the user options
            Set<ImportComponentOption> componentOptions = ImportUtil.inspectZipComponents(content, ImportType.FEED);
            options.addOptionsIfNotExists(componentOptions);
            importFeed.setImportOptions(options);

            //validate the import

            //read the JSON into the Feed object
            FeedMetadata metadata = importFeed.getFeedToImport();

            //validate the incoming category exists
            validateFeedCategory(importFeed, options, metadata);

            //verify if we should overwrite the feed if it already exists
            String feedCategory = StringUtils.isNotBlank(options.getCategorySystemName()) ? options.getCategorySystemName() : metadata.getSystemCategoryName();
            //query for this feed.
            FeedMetadata existingFeed = metadataAccess.read(() -> metadataService.getFeedByName(feedCategory, metadata.getSystemFeedName()));

            if (!validateOverwriteExistingFeed(existingFeed, metadata, importFeed)) {
                //exit
                return importFeed;
            }

            //sensitive properties
            if (!validateSensitiveProperties(metadata, importFeed, options)) {
                return importFeed;
            }
            //UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(options.getUploadKey(),"Validating the template data");
            ExportImportTemplateService.ImportTemplate importTemplate = exportImportTemplateService.validateTemplateForImport(importFeed.getFileName(), content, options);
            // need to set the importOptions back to the feed options
            //find importOptions for the Template and add them back to the set of options
            //importFeed.getImportOptions().updateOptions(importTemplate.getImportOptions().getImportComponentOptions());
            importFeed.setTemplate(importTemplate);
            // statusMessage.update("Validated the template data",importTemplate.isValid());
            if (!importTemplate.isValid()) {
                importFeed.setValid(false);
                List<String> errorMessages = importTemplate.getTemplateResults().getAllErrors().stream().map(nifiError -> nifiError.getMessage()).collect(Collectors.toList());
                if (!errorMessages.isEmpty()) {
                    for (String msg : errorMessages) {
                        importFeed.addErrorMessage(metadata, msg);
                    }
                }
            }
            //  statusMessage = uploadProgressService.addUploadStatus(options.getUploadKey(),"Validation complete: the feed is "+(importFeed.isValid() ? "valid" : "invalid"),true,importFeed.isValid());

        } catch (Exception e) {
            feedImportStatusMessage.update("Validation error. Feed import error: " + e.getMessage(), false);
            throw new UnsupportedOperationException("Error importing template  " + fileName + ".  " + e.getMessage());
        }
        feedImportStatusMessage.update("Validated Feed import.", importFeed.isValid());
        return importFeed;
    }

    private Set<String> getValidZipFileEntries() {
        // do not include nifiConnectingReusableTemplate.xml - it may or may not be there or there can be many of them if flow connects to multiple reusable templates
        String[] entries = {
            FEED_JSON_FILE,
            ExportImportTemplateService.NIFI_TEMPLATE_XML_FILE,
            ExportImportTemplateService.TEMPLATE_JSON_FILE
        };
        return Sets.newHashSet(entries);
    }

    private boolean validateSensitiveProperties(FeedMetadata metadata, ImportFeed importFeed, ImportFeedOptions importOptions) {
        //detect any sensitive properties and prompt for input before proceeding
        UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importFeed.getImportOptions().getUploadKey(), "Validating feed properties.");
        List<NifiProperty> sensitiveProperties = metadata.getSensitiveProperties();
        ImportUtil.addToImportOptionsSensitiveProperties(importOptions, sensitiveProperties, ImportComponent.FEED_DATA);
        boolean valid = ImportUtil.applyImportPropertiesToFeed(metadata, importFeed, ImportComponent.FEED_DATA);
        if (!valid) {
            statusMessage.update("Validatoin Error. Additional properties are needed before uploading the feed ", false);
            importFeed.setValid(false);
        } else {
            statusMessage.update("Validated feed properties.", valid);
        }
        completeSection(importFeed.getImportOptions(), ImportSection.Section.VALIDATE_PROPERTIES);
        return valid;

    }

    private boolean validateOverwriteExistingFeed(FeedMetadata existingFeed, FeedMetadata importingFeed, ImportFeed feed) {
        if (existingFeed != null && !feed.getImportOptions().isImportAndOverwrite(ImportComponent.FEED_DATA)) {
            UploadProgressMessage
                statusMessage =
                uploadProgressService.addUploadStatus(feed.getImportOptions().getUploadKey(), "Validation error. " + importingFeed.getCategoryAndFeedName() + " already exists.", true, false);
            //if we dont have permission to overwrite then return with error that feed already exists
            feed.setValid(false);
            ExportImportTemplateService.ImportTemplate importTemplate = new ExportImportTemplateService.ImportTemplate(feed.getFileName());
            feed.setTemplate(importTemplate);
            String msg = "The feed " + existingFeed.getCategoryAndFeedName()
                         + " already exists.";
            feed.getImportOptions().addErrorMessage(ImportComponent.FEED_DATA, msg);
            feed.addErrorMessage(existingFeed, msg);
            feed.setValid(false);
            return false;
        } else {
            String message = "Validated Feed data.  This import will " + (existingFeed != null ? "overwrite" : "create") + " the feed " + importingFeed.getCategoryAndFeedName();
            uploadProgressService.addUploadStatus(feed.getImportOptions().getUploadKey(), message, true, true);
        }

        completeSection(feed.getImportOptions(), ImportSection.Section.VALIDATE_FEED);
        return true;
    }

    private boolean validateFeedCategory(ImportFeed importFeed, ImportFeedOptions importOptions, FeedMetadata metadata) {
        boolean valid = true;
        if (StringUtils.isNotBlank(importOptions.getCategorySystemName())) {
            UploadProgressMessage
                statusMessage =
                uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Validating the newly specified category. Ensure " + importOptions.getCategorySystemName() + " exists.");
            FeedCategory optionsCategory = metadataService.getCategoryBySystemName(importOptions.getCategorySystemName());
            if (optionsCategory == null) {
                importFeed.setValid(false);
                statusMessage.update("Validation Error. The category " + importOptions.getCategorySystemName() + " does not exist.", false);
                valid = false;
            } else {
                metadata.getCategory().setSystemName(importOptions.getCategorySystemName());
                statusMessage.update("Validated. The category " + importOptions.getCategorySystemName() + " exists.", true);
            }
        }
        completeSection(importOptions, ImportSection.Section.VALIDATE_FEED_CATEGORY);
        return valid;
    }

    //Import

    /**
     * Import a feed zip file
     *
     * @param fileName      the name of the file
     * @param content       the file content
     * @param importOptions user options about what/how it should be imported
     * @return the feed data to import
     */
    public ImportFeed importFeed(String fileName, byte[] content, ImportFeedOptions importOptions) throws Exception {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_FEEDS);
        UploadProgress progress = uploadProgressService.getUploadStatus(importOptions.getUploadKey());
        progress.setSections(ImportSection.sectionsForImportAsString(ImportType.FEED));

        ImportFeed feed = validateFeedForImport(fileName, content, importOptions);

        if (feed.isValid()) {
            //read the JSON into the Feed object
            FeedMetadata metadata = feed.getFeedToImport();
            //query for this feed.
            String feedCategory = StringUtils.isNotBlank(importOptions.getCategorySystemName()) ? importOptions.getCategorySystemName() : metadata.getSystemCategoryName();
            FeedMetadata existingFeed = metadataAccess.read(() -> metadataService.getFeedByName(feedCategory, metadata.getSystemFeedName()));

            metadata.getCategory().setSystemName(feedCategory);

            ImportTemplateOptions importTemplateOptions = new ImportTemplateOptions();
            importTemplateOptions.setImportComponentOptions(importOptions.getImportComponentOptions());
            importTemplateOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setContinueIfExists(true);
            ExportImportTemplateService.ImportTemplate importTemplate = feed.getTemplate();
            importTemplate.setImportOptions(importTemplateOptions);
            importTemplateOptions.setUploadKey(importOptions.getUploadKey());
            importTemplate.setValid(true);
            ExportImportTemplateService.ImportTemplate template = exportImportTemplateService.importZip(importTemplate);
            if (template.isSuccess()) {
                //import the feed
                feed.setTemplate(template);
                //now that we have the Feed object we need to create the instance of the feed
                UploadProgressMessage uploadProgressMessage = uploadProgressService.addUploadStatus(importOptions.getUploadKey(), "Saving  and creating feed instance in NiFi");
                NifiFeed nifiFeed = metadataAccess.commit(() -> {
                    metadata.setIsNew(existingFeed == null ? true : false);
                    metadata.setFeedId(existingFeed != null ? existingFeed.getFeedId() : null);
                    metadata.setId(existingFeed != null ? existingFeed.getId() : null);
                    //reassign the templateId to the newly registered template id
                    metadata.setTemplateId(template.getTemplateId());
                    if (metadata.getRegisteredTemplate() != null) {
                        metadata.getRegisteredTemplate().setNifiTemplateId(template.getNifiTemplateId());
                        metadata.getRegisteredTemplate().setId(template.getTemplateId());
                    }
                    //get/create category
                    FeedCategory category = metadataService.getCategoryBySystemName(metadata.getCategory().getSystemName());
                    if (category == null) {
                        metadataService.saveCategory(metadata.getCategory());
                    } else {
                        metadata.setCategory(category);
                    }
                    if (importOptions.isDisableUponImport()) {
                        metadata.setActive(false);
                        metadata.setState(FeedMetadata.STATE.DISABLED.name());
                    }
                    return metadataService.createFeed(metadata);
                });
                if (nifiFeed != null) {
                    feed.setFeedName(nifiFeed.getFeedMetadata().getCategoryAndFeedName());
                    uploadProgressMessage.update("Successfully saved the feed " + feed.getFeedName(), true);
                }
                feed.setNifiFeed(nifiFeed);
                feed.setSuccess(nifiFeed != null && nifiFeed.isSuccess());
            } else {
                feed.setSuccess(false);
                feed.setTemplate(template);
                feed.addErrorMessage(existingFeed, "The feed " + FeedNameUtil.fullName(feedCategory, metadata.getSystemFeedName())
                                                   + " needs additional properties to be supplied before importing.");

            }

            completeSection(importOptions, ImportSection.Section.IMPORT_FEED_DATA);
        }
        return feed;
    }

    //Utility

    private void completeSection(ImportOptions options, ImportSection.Section section) {
        UploadProgress progress = uploadProgressService.getUploadStatus(options.getUploadKey());
        progress.completeSection(section.name());
    }

    private ImportFeed readFeedJson(String fileName, byte[] content) throws IOException {

        byte[] buffer = new byte[1024];
        InputStream inputStream = new ByteArrayInputStream(content);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        // while there are entries I process them
        ImportFeed importFeed = new ImportFeed(fileName);

        while ((zipEntry = zis.getNextEntry()) != null) {

            if (zipEntry.getName().startsWith(FEED_JSON_FILE)) {
                String zipEntryContents = ZipFileUtil.zipEntryToString(buffer, zis, zipEntry);
                importFeed.setFeedJson(zipEntryContents);
            }
        }
        return importFeed;
    }

    //Internal classes

    public class ExportFeed {

        private String fileName;
        private byte[] file;

        public ExportFeed(String fileName, byte[] file) {
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

    public static class ImportFeed {

        private boolean valid;

        private boolean success;
        private String fileName;
        private String feedName;
        private ExportImportTemplateService.ImportTemplate template;
        private NifiFeed nifiFeed;
        private String feedJson;
        private ImportFeedOptions importOptions;

        @JsonIgnore
        private FeedMetadata feedToImport;

        public ImportFeed() {
        }

        public ImportFeed(String fileName) {
            this.fileName = fileName;
            this.template = new ExportImportTemplateService.ImportTemplate(fileName);
        }

        public String getFeedJson() {
            return feedJson;
        }

        public void setFeedJson(String feedJson) {
            this.feedJson = feedJson;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public ExportImportTemplateService.ImportTemplate getTemplate() {
            return template;
        }

        public void setTemplate(ExportImportTemplateService.ImportTemplate template) {
            this.template = template;
        }

        public String getFeedName() {
            return feedName;
        }

        public void setFeedName(String feedName) {
            this.feedName = feedName;
        }

        public NifiFeed getNifiFeed() {
            return nifiFeed;
        }

        public void setNifiFeed(NifiFeed nifiFeed) {
            this.nifiFeed = nifiFeed;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public void addErrorMessage(FeedMetadata feedMetadata, String errorMessage) {
            if (nifiFeed == null) {
                nifiFeed = new NifiFeed(feedMetadata, null);
            }
            nifiFeed.addErrorMessage(errorMessage);
        }

        public ImportFeedOptions getImportOptions() {
            return importOptions;
        }

        public void setImportOptions(ImportFeedOptions importOptions) {
            this.importOptions = importOptions;
        }

        @JsonIgnore
        public FeedMetadata getFeedToImport() {
            if (feedToImport == null && StringUtils.isNotBlank(feedJson)) {
                feedToImport = ObjectMapperSerializer.deserialize(getFeedJson(), FeedMetadata.class);
            }
            return feedToImport;
        }

        @JsonIgnore
        public void setFeedToImport(FeedMetadata feedToImport) {
            this.feedToImport = feedToImport;
        }
    }


}
