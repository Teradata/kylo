package com.thinkbiganalytics.feedmgr.service.feed.importing;
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
import com.google.common.collect.Sets;
import com.thinkbiganalytics.feedmgr.MetadataFieldAnnotationFieldNameResolver;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportSection;
import com.thinkbiganalytics.feedmgr.rest.ImportType;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportFeedOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportProperty;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgress;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.feedmgr.service.feed.ImportFeedException;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.ImportFeed;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.ImportException;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporter;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporterFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportTemplateRoutine;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportTemplateRoutineFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.support.ZipFileUtil;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.UserDatasource;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

/**
 * Created by sr186054 on 12/11/17.
 */
public class FeedImporter {

    private static final Logger log = LoggerFactory.getLogger(FeedImporter.class);

    @Inject
    private LegacyNifiRestClient nifiRestClient;

    @Inject
    private MetadataService metadataService;

    @Inject
    private CategoryProvider categoryProvider;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private AccessController accessController;

    @Inject
    private UploadProgressService uploadProgressService;

    /**
     * Provides access to {@code Datasource} objects.
     */
    @Inject
    private DatasourceProvider datasourceProvider;

    /**
     * The {@code Datasource} transformer
     */
    @Inject
    private DatasourceModelTransform datasourceTransform;

    @Inject
    private RegisteredTemplateService registeredTemplateService;

    @Inject
    private TemplateImporterFactory templateImporterFactory;

    @Inject
    private ImportTemplateRoutineFactory importTemplateRoutineFactory;


    protected ImportFeed importFeed;
    protected ImportFeedOptions importFeedOptions;

    protected String fileName;
    protected byte[] file;

    protected UploadProgressMessage overallStatusMessage;


    public FeedImporter(String fileName, byte[] file, ImportFeedOptions importFeedOptions) {
        this.fileName = fileName;
        this.file = file;
        this.importFeedOptions = importFeedOptions;
    }


    public ImportFeed validate() {
        UploadProgressMessage feedImportStatusMessage = uploadProgressService.addUploadStatus(importFeedOptions.getUploadKey(), "Validating Feed import.");
        try {
            init();

            //read the JSON into the Feed object
            FeedMetadata metadata = importFeed.getFeedToImport();

            //validate the incoming category exists
            validateFeedCategory();

            //verify if we should overwrite the feed if it already exists
            String feedCategory = StringUtils.isNotBlank(importFeedOptions.getCategorySystemName()) ? importFeedOptions.getCategorySystemName() : metadata.getSystemCategoryName();
            //query for this feed.
            //first read in the feed as a service account
            FeedMetadata existingFeed = metadataAccess.read(() -> {
                return metadataService.getFeedByName(feedCategory, metadata.getSystemFeedName());
            }, MetadataAccess.SERVICE);
            if (!validateOverwriteExistingFeed(existingFeed)) {
                //exit
                return importFeed;
            }

            if (accessController.isEntityAccessControlled()) {
                if (!validateEntityAccess(existingFeed, feedCategory)) {
                    return importFeed;
                }
            }

            //sensitive properties
            if (!validateSensitiveProperties()) {
                return importFeed;
            }

            // Valid data sources
            if (!validateUserDatasources()) {
                return importFeed;
            }

            //UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(options.getUploadKey(),"Validating the template data");
            TemplateImporter templateImporter = templateImporterFactory.apply(importFeed.getFileName(), file, importFeedOptions);
            ImportTemplate importTemplate = templateImporter.validate();
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

        return this.importFeed;
    }

    public ImportFeed validateAndImport() {
        UploadProgress progress = uploadProgressService.getUploadStatus(importFeedOptions.getUploadKey());
        progress.setSections(ImportSection.sectionsForImportAsString(ImportType.FEED));
        validate();
        if (this.importFeed.isValid()) {
            try {
                importFeed();
            } catch (Exception e) {
                //rollback
                log.error("Error importing feed {}", fileName, e);
            }
        }
        return importFeed;

    }


    private void init() {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.IMPORT_FEEDS);
        UploadProgressMessage feedImportStatusMessage = uploadProgressService.addUploadStatus(importFeedOptions.getUploadKey(), "Initialize feed import.");
        try {
            boolean isValid = isValidFileImport(fileName) && ZipFileUtil.validateZipEntriesWithRequiredEntries(file, getValidZipFileEntries(), Sets.newHashSet(ImportFeed.FEED_JSON_FILE));
            if (!isValid) {
                feedImportStatusMessage.update("Validation error. Feed import error. The zip file you uploaded is not valid feed export.", false);
                throw new ImportFeedException("The zip file you uploaded is not valid feed export.");
            }

            //get the Feed Data
            importFeed = readFeedJson(fileName, file);
            //initially mark as valid.
            importFeed.setValid(true);

            //merge in the file components to the user options
            Set<ImportComponentOption> componentOptions = ImportUtil.inspectZipComponents(file, ImportType.FEED);
            importFeedOptions.addOptionsIfNotExists(componentOptions);

            //importFeedOptions.findImportComponentOption(ImportComponent.TEMPLATE_CONNECTION_INFORMATION).addConnectionInfo(importFeed.getReusableTemplateConnections());

            importFeed.setImportOptions(importFeedOptions);
            feedImportStatusMessage.complete(true);
        } catch (Exception e) {
            throw new ImportException("Unable to import feed. ", e);
        }
    }


    private boolean validateSensitiveProperties() {
        FeedMetadata metadata = importFeed.getFeedToImport();

        //detect any sensitive properties and prompt for input before proceeding
        UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importFeed.getImportOptions().getUploadKey(), "Validating feed properties.");
        List<NifiProperty> sensitiveProperties = metadata.getSensitiveProperties();
        ImportUtil.addToImportOptionsSensitiveProperties(importFeedOptions, sensitiveProperties, ImportComponent.FEED_DATA);
        boolean valid = ImportUtil.applyImportPropertiesToFeed(metadata, importFeed, ImportComponent.FEED_DATA);
        if (!valid) {
            statusMessage.update("Validation Error. Additional properties are needed before uploading the feed.", false);
            importFeed.setValid(false);
        } else {
            statusMessage.update("Validated feed properties.", valid);
        }
        uploadProgressService.completeSection(importFeed.getImportOptions(), ImportSection.Section.VALIDATE_PROPERTIES);
        return valid;

    }

    /**
     * Validates that user data sources can be imported with provided properties.
     *
     * @return {@code true} if the feed can be imported, or {@code false} otherwise
     */
    private boolean validateUserDatasources() {
        FeedMetadata metadata = importFeed.getFeedToImport();
        final UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importFeed.getImportOptions().getUploadKey(), "Validating data sources.");

        // Get data sources needing to be created
        final Set<String> availableDatasources = metadataAccess.read(
            () -> datasourceProvider.getDatasources(datasourceProvider.datasetCriteria().type(UserDatasource.class)).stream()
                .map(com.thinkbiganalytics.metadata.api.datasource.Datasource::getId)
                .map(Object::toString)
                .collect(Collectors.toSet())
        );
        final ImportComponentOption componentOption = importFeedOptions.findImportComponentOption(ImportComponent.USER_DATASOURCES);
        final List<Datasource> providedDatasources = Optional.ofNullable(metadata.getUserDatasources()).orElse(Collections.emptyList());

        if (componentOption.getProperties().isEmpty()) {
            componentOption.setProperties(
                providedDatasources.stream()
                    .filter(datasource -> !availableDatasources.contains(datasource.getId()))
                    .map(datasource -> new ImportProperty(datasource.getName(), datasource.getId(), null, null, null))
                    .collect(Collectors.toList())
            );
        }

        // Update feed with re-mapped data sources
        final boolean valid = componentOption.getProperties().stream()
            .allMatch(property -> {
                if (property.getPropertyValue() != null) {
                    ImportUtil.replaceDatasource(metadata, property.getProcessorId(), property.getPropertyValue());
                    return true;
                } else {
                    return false;
                }
            });

        if (valid) {
            statusMessage.update("Validated data sources.", true);
        } else {
            statusMessage.update("Validation Error. Additional properties are needed before uploading the feed.", false);
            importFeed.setValid(false);
        }

        uploadProgressService.completeSection(importFeed.getImportOptions(), ImportSection.Section.VALIDATE_USER_DATASOURCES);
        return valid;
    }

    private boolean validateEntityAccess(FeedMetadata existingFeed, String feedCategory) {
        FeedMetadata importingFeed = importFeed.getFeedToImport();
        if (existingFeed != null) {
            FeedMetadata userAccessFeed = metadataAccess.read(() -> {
                return metadataService.getFeedByName(feedCategory, importingFeed.getSystemFeedName());
            });
            if (userAccessFeed == null || !userAccessFeed.hasAction(FeedAccessControl.EDIT_DETAILS.getSystemName())) {
                //error
                importFeed.setValid(false);
                if (importFeed.getTemplate() == null) {
                    ImportTemplate importTemplate = new ImportTemplate(importFeed.getFileName());
                    importFeed.setTemplate(importTemplate);
                }
                String msg = "Access Denied.  You do not have access to edit this feed.";
                importFeed.getImportOptions().addErrorMessage(ImportComponent.FEED_DATA, msg);
                importFeed.addErrorMessage(existingFeed, msg);
                importFeed.setValid(false);
                return false;
            } else {
                return true;
            }

        } else {
            //ensure the user can create under the category
            Category category = metadataAccess.read(() -> {
                return categoryProvider.findBySystemName(feedCategory);
            }, MetadataAccess.SERVICE);

            if (category == null) {
                //ensure the user has functional access to create categories
                boolean hasPermission = accessController.hasPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_CATEGORIES);
                if (!hasPermission) {
                    String msg = "Access Denied. The category for this feed," + feedCategory + ", doesn't exist and you do not have access to create a new category.";
                    importFeed.getImportOptions().addErrorMessage(ImportComponent.FEED_DATA, msg);
                    importFeed.addErrorMessage(existingFeed, msg);
                    importFeed.setValid(false);
                    return false;
                }
                return true;
            } else {
                //if the feed is new ensure the user has write access to create feeds
                return metadataAccess.read(() -> {
                    //Query for Category and ensure the user has access to create feeds on that category
                    Category domainCategory = categoryProvider.findBySystemName(feedCategory);
                    if (domainCategory == null || (!domainCategory.getAllowedActions().hasPermission(CategoryAccessControl.CREATE_FEED))) {
                        String msg = "Access Denied. You do not have access to create feeds under the category " + feedCategory
                                     + ". Attempt made to create feed " + FeedNameUtil.fullName(feedCategory, importingFeed.getSystemFeedName()) + ".";
                        importFeed.getImportOptions().addErrorMessage(ImportComponent.FEED_DATA, msg);
                        importFeed.addErrorMessage(existingFeed, msg);
                        importFeed.setValid(false);
                        return false;
                    }

                    /*
                       TemplateAccessControl.CREATE_FEED permission is not being used right now.
                       Uncomment this code once/if we should be checking it

                    // Query for Template and ensure the user has access to create feeds
                    final RegisteredTemplate domainTemplate = registeredTemplateService.findRegisteredTemplate(
                        new RegisteredTemplateRequest.Builder().templateName(importingFeed.getTemplateName()).isFeedEdit(true).build());
                    if (domainTemplate != null && !registeredTemplateService.hasTemplatePermission(domainTemplate.getId(), TemplateAccessControl.CREATE_FEED)) {
                        final String msg = "Access Denied. You do not have access to create feeds using the template " + importingFeed.getTemplateName()
                                           + ". Attempt made to create feed " + FeedNameUtil.fullName(feedCategory, importingFeed.getSystemFeedName()) + ".";
                        feed.getImportOptions().addErrorMessage(ImportComponent.FEED_DATA, msg);
                        feed.addErrorMessage(existingFeed, msg);
                        feed.setValid(false);
                        return false;
                    }
                    */
                    return true;
                });
            }

        }
    }

    private boolean validateOverwriteExistingFeed(FeedMetadata existingFeed) {
        FeedMetadata importingFeed = importFeed.getFeedToImport();
        if (existingFeed != null && !importFeedOptions.isImportAndOverwrite(ImportComponent.FEED_DATA)) {
            UploadProgressMessage
                statusMessage =
                uploadProgressService.addUploadStatus(importFeedOptions.getUploadKey(), "Validation error. " + importingFeed.getCategoryAndFeedName() + " already exists.", true, false);
            //if we dont have permission to overwrite then return with error that feed already exists
            importFeed.setValid(false);
            ImportTemplate importTemplate = new ImportTemplate(importFeed.getFileName());
            importFeed.setTemplate(importTemplate);
            String msg = "The feed " + existingFeed.getCategoryAndFeedName()
                         + " already exists.";
            importFeed.getImportOptions().addErrorMessage(ImportComponent.FEED_DATA, msg);
            importFeed.addErrorMessage(existingFeed, msg);
            importFeed.setValid(false);
            return false;
        } else {
            String message = "Validated Feed data.  This import will " + (existingFeed != null ? "overwrite" : "create") + " the feed " + importingFeed.getCategoryAndFeedName();
            uploadProgressService.addUploadStatus(importFeedOptions.getUploadKey(), message, true, true);
        }

        uploadProgressService.completeSection(importFeedOptions, ImportSection.Section.VALIDATE_FEED);
        return true;
    }

    private boolean validateFeedCategory() {

        FeedMetadata metadata = importFeed.getFeedToImport();

        boolean valid = true;
        if (StringUtils.isNotBlank(importFeedOptions.getCategorySystemName())) {
            UploadProgressMessage
                statusMessage =
                uploadProgressService.addUploadStatus(importFeedOptions.getUploadKey(), "Validating the newly specified category. Ensure " + importFeedOptions.getCategorySystemName() + " exists.");
            FeedCategory optionsCategory = metadataService.getCategoryBySystemName(importFeedOptions.getCategorySystemName());
            if (optionsCategory == null) {
                importFeed.setValid(false);
                statusMessage.update("Validation Error. The category " + importFeedOptions.getCategorySystemName() + " does not exist, or you dont have access to it.", false);
                valid = false;
            } else {
                if (valid) {
                    metadata.getCategory().setSystemName(importFeedOptions.getCategorySystemName());
                    statusMessage.update("Validated. The category " + importFeedOptions.getCategorySystemName() + " exists.", true);
                }
            }
        }
        uploadProgressService.completeSection(importFeedOptions, ImportSection.Section.VALIDATE_FEED_CATEGORY);
        return valid;
    }

    //Import

    /**
     * Import a feed zip file
     */
    private ImportFeed importFeed() throws Exception {

        //read the JSON into the Feed object
        FeedMetadata metadata = importFeed.getFeedToImport();
        //query for this feed.
        String feedCategory = StringUtils.isNotBlank(importFeedOptions.getCategorySystemName()) ? importFeedOptions.getCategorySystemName() : metadata.getSystemCategoryName();
        FeedMetadata existingFeed = metadataAccess.read(() -> metadataService.getFeedByName(feedCategory, metadata.getSystemFeedName()));

        metadata.getCategory().setSystemName(feedCategory);

        ImportTemplateOptions importTemplateOptions = new ImportTemplateOptions();
        importTemplateOptions.setImportComponentOptions(importFeedOptions.getImportComponentOptions());
        importTemplateOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setContinueIfExists(true);
        ImportTemplate importTemplate = importFeed.getTemplate();
        importTemplate.setImportOptions(importTemplateOptions);
        importTemplateOptions.setUploadKey(importFeedOptions.getUploadKey());
        importTemplate.setValid(true);
        importTemplateOptions.setDeferCleanup(true);

        //Import the Template
        ImportTemplateRoutine importTemplateRoutine = importTemplateRoutineFactory.apply(importTemplate, importTemplateOptions, ImportTemplate.TYPE.ARCHIVE);
        importTemplateRoutine.importTemplate();
        if (importTemplate.isSuccess()) {
            //import the feed
            importFeed.setTemplate(importTemplate);
            //now that we have the Feed object we need to create the instance of the feed
            UploadProgressMessage uploadProgressMessage = uploadProgressService.addUploadStatus(importFeedOptions.getUploadKey(), "Saving  and creating feed instance in NiFi");

            metadata.setIsNew(existingFeed == null ? true : false);
            metadata.setFeedId(existingFeed != null ? existingFeed.getFeedId() : null);
            metadata.setId(existingFeed != null ? existingFeed.getId() : null);
            //reassign the templateId to the newly registered template id
            metadata.setTemplateId(importTemplate.getTemplateId());
            if (metadata.getRegisteredTemplate() != null) {
                metadata.getRegisteredTemplate().setNifiTemplateId(importTemplate.getNifiTemplateId());
                metadata.getRegisteredTemplate().setId(importTemplate.getTemplateId());
            }
            //get/create category
            FeedCategory category = metadataService.getCategoryBySystemName(metadata.getCategory().getSystemName());
            if (category == null) {
                metadata.getCategory().setId(null);
                metadataService.saveCategory(metadata.getCategory());
            } else {
                metadata.setCategory(category);
            }
            if (importFeedOptions.isDisableUponImport()) {
                metadata.setActive(false);
                metadata.setState(FeedMetadata.STATE.DISABLED.name());
            }

            //remap any preconditions to this new feed/category name.
            if (metadata.getSchedule().hasPreconditions()) {
                metadata.getSchedule().getPreconditions().stream()
                    .flatMap(preconditionRule -> preconditionRule.getProperties().stream())
                    .filter(fieldRuleProperty -> PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name().equals(fieldRuleProperty.getType()))
                    .forEach(fieldRuleProperty -> fieldRuleProperty.setValue(metadata.getCategoryAndFeedName()));
            }

            ////for all those properties where the template value is != userEditable and the template value has a metadata. property, remove that property from the feed properties so it can be imported and assigned correctly
            RegisteredTemplate template1 = registeredTemplateService.findRegisteredTemplateById(importTemplate.getTemplateId());
            if (template1 != null) {

                //Find all the properties in the template that have ${metadata. and are not userEditable.
                //These are the properties we need to replace on the feed metadata
                List<NifiProperty> metadataProperties = template1.getProperties().stream().filter(nifiProperty -> {

                    return nifiProperty != null && StringUtils.isNotBlank(nifiProperty.getValue()) && !nifiProperty.isUserEditable() && nifiProperty.getValue().contains("${" +
                                                                                                                                                                         MetadataFieldAnnotationFieldNameResolver.metadataPropertyPrefix);
                }).collect(Collectors.toList());

                //Replace the Feed Metadata properties with those that match the template ones from above.
                List<NifiProperty> updatedProperties = metadata.getProperties().stream().map(nifiProperty -> {
                    NifiProperty p = NifiPropertyUtil.findPropertyByProcessorName(metadataProperties, nifiProperty);
                    return p != null ? p : nifiProperty;
                }).collect(Collectors.toList());
                metadata.setProperties(updatedProperties);

            }

            NifiFeed nifiFeed = metadataService.createFeed(metadata);

            if (nifiFeed != null) {
                importFeed.setFeedName(nifiFeed.getFeedMetadata().getCategoryAndFeedName());
                if (nifiFeed.isSuccess()) {
                    uploadProgressMessage.update("Successfully saved the feed " + importFeed.getFeedName(), true);
                } else {
                    if (nifiFeed.getFeedProcessGroup() != null && nifiFeed.getFeedProcessGroup().isRolledBack()) {

                        if (importTemplateRoutine != null) {
                            importTemplateRoutine.rollback();
                        }
                    }
                    uploadProgressMessage.update("Errors were found importing the feed " + importFeed.getFeedName(), false);

                }
                importTemplateRoutine.cleanup();

            }
            importFeed.setNifiFeed(nifiFeed);
            importFeed.setSuccess(nifiFeed != null && nifiFeed.isSuccess());
        } else {
            importFeed.setSuccess(false);
            importFeed.setTemplate(importTemplate);
            importFeed.addErrorMessage(existingFeed, "The feed " + FeedNameUtil.fullName(feedCategory, metadata.getSystemFeedName())
                                                     + " needs additional properties to be supplied before importing.");

        }

        uploadProgressService.completeSection(importFeedOptions, ImportSection.Section.IMPORT_FEED_DATA);

        return importFeed;
    }


    private ImportFeed readFeedJson(String fileName, byte[] content) throws IOException {

        byte[] buffer = new byte[1024];
        InputStream inputStream = new ByteArrayInputStream(content);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        // while there are entries I process them
        ImportFeed importFeed = new ImportFeed(fileName);

        while ((zipEntry = zis.getNextEntry()) != null) {

            if (zipEntry.getName().startsWith(ImportFeed.FEED_JSON_FILE)) {
                String zipEntryContents = ZipFileUtil.zipEntryToString(buffer, zis, zipEntry);
                importFeed.setFeedJson(zipEntryContents);
            }
        }
        return importFeed;
    }


    private Set<String> getValidZipFileEntries() {
        // do not include nifiConnectingReusableTemplate.xml - it may or may not be there or there can be many of them if flow connects to multiple reusable templates
        String[] entries = {
            ImportFeed.FEED_JSON_FILE,
            ImportTemplate.NIFI_TEMPLATE_XML_FILE,
            ImportTemplate.TEMPLATE_JSON_FILE
        };
        return Sets.newHashSet(entries);
    }


    /**
     * Is the file a valid file for importing
     *
     * @param fileName the name of the file
     * @return true if valid, false if not valid
     */
    private boolean isValidFileImport(String fileName) {
        return fileName.endsWith(".zip");
    }


}
