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
import com.thinkbiganalytics.feedmgr.rest.model.ImportPropertyBuilder;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgress;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.feedmgr.service.category.FeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
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
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.UserDatasource;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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

    @Inject
    private ConnectorProvider catalogConnectorProvider;

    @Inject
    private DataSourceProvider catalogDataSourceProvider;

    @Inject
    private DataSetProvider catalogDataSetProvider;

    @Inject
    CatalogModelTransform catalogModelTransform;
    
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

    @Inject
    private FeedManagerCategoryService feedManagerCategoryService;

    @Inject
    private FeedManagerFeedService feedManagerFeedService;


    protected ImportFeed importFeed;
    protected ImportFeedOptions importFeedOptions;

    protected String fileName;
    protected byte[] file;

    protected UploadProgressMessage overallStatusMessage;


    public FeedImporter(String fileName, byte[] file, ImportFeedOptions importFeedOptions) {
        this.fileName = fileName;
        this.file = file;
        this.importFeedOptions = importFeedOptions;

        //Set the ContinueIfExists flag on the Template components
        final ImportComponentOption templateImportOption = importFeedOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA);
        if (templateImportOption != null && !templateImportOption.isOverwrite()) {
            templateImportOption.setContinueIfExists(true);
        }
        final ImportComponentOption reusableTemplateOption = importFeedOptions.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE);
        if (reusableTemplateOption != null && !reusableTemplateOption.isOverwrite()) {
            reusableTemplateOption.setContinueIfExists(true);
        }
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
            if (!validateProperties()) {
                return importFeed;
            }

            // Valid data sources
            if (!validateUserDatasources()) {
                return importFeed;
            }

            if(!validateUserDataSets()) {
                return importFeed;
            }

            //  final ImportComponentOption templateImportOption = importFeedOptions.findImportComponentOption(ImportComponent.TEMPLATE_DATA);
            //  if(templateImportOption.isShouldImport() && templateImportOption.isOverwrite()) {
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
                return importFeed;
            }
            //     }
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


    private boolean validateProperties() {

        boolean valid = validateSensitiveProperties();

        valid &= validateRequiredFeedAndCategoryProperties();

        uploadProgressService.completeSection(importFeed.getImportOptions(), ImportSection.Section.VALIDATE_PROPERTIES);
        return valid;
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

        return valid;

    }

    private boolean validateRequiredFeedAndCategoryProperties() {
        FeedMetadata feedToImport = importFeed.getFeedToImport();
        boolean valid = true;
        // on new categories validate there are no required properties
        FeedCategory category = metadataService.getCategoryBySystemName(feedToImport.getCategory().getSystemName());
        if (category == null) {
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importFeed.getImportOptions().getUploadKey(), "New category detected. Validating properties.");
            ImportComponentOption categoryUserFieldOption = importFeed.getImportOptions().findImportComponentOption(ImportComponent.FEED_CATEGORY_USER_FIELDS);

            Map<String, UserProperty>
                suppliedUploadCategoryProperties =
                categoryUserFieldOption.getProperties().stream().map(property -> toUserProperty(property)).collect(Collectors.toMap(p -> p.getSystemName(), p -> p));
            if (feedToImport.getCategory().getUserProperties() != null) {
                Map<String, UserProperty>
                    suppliedCategoryProperties =
                    feedToImport.getCategory().getUserProperties().stream().collect(Collectors.toMap(p -> p.getSystemName(), p -> p));
                suppliedUploadCategoryProperties.putAll(suppliedCategoryProperties);
            }
            //set the list back
            feedToImport.getCategory().setUserProperties(new HashSet<>(suppliedUploadCategoryProperties.values()));
            Map<String, UserProperty>
                requiredCategoryUserProperties =
                feedManagerCategoryService.getUserProperties().stream().filter(p -> Objects.equals(p.isRequired(), Boolean.TRUE)).collect(Collectors.toMap(p -> p.getSystemName(), p -> p));

            //add back in the required if they dont exist of they are blank
            for (String k : requiredCategoryUserProperties.keySet()) {
                boolean supplied = suppliedUploadCategoryProperties.containsKey(k);
                if (!supplied || (supplied && StringUtils.isBlank(suppliedUploadCategoryProperties.get(k).getValue()))) {
                    if (!supplied) {
                        categoryUserFieldOption.getProperties().add(toImportProperty(requiredCategoryUserProperties.get(k)));
                    }
                    valid = false;
                }
            }
            if (valid) {
                statusMessage.update("Validated category properties.", valid);
            } else {
                statusMessage.update("Validation Error. Additional properties are needed on the category before importing.", false);
            }
        }

        ImportComponentOption feedUserFieldOption = importFeed.getImportOptions().findImportComponentOption(ImportComponent.FEED_USER_FIELDS);

        Set<UserField> feedUserFields = feedManagerFeedService.getUserFields();
        //Add in any additional userfields that may be required by this category
        if (category != null) {
            Set<UserField> requiredCategoryFeedUserFields = category.getUserFields().stream().filter(f -> Objects.equals(f.isRequired(), Boolean.TRUE)).collect(Collectors.toSet());
            if (!requiredCategoryFeedUserFields.isEmpty()) {
                feedUserFields.addAll(requiredCategoryFeedUserFields);
            }
        }

        Map<String, UserProperty>
            suppliedUploadFeedProperties =
            feedUserFieldOption.getProperties().stream().map(property -> toUserProperty(property)).collect(Collectors.toMap(p -> p.getSystemName(), p -> p));
        if (feedToImport.getUserProperties() != null) {
            Map<String, UserProperty> suppliedFeedProperties = feedToImport.getUserProperties().stream().collect(Collectors.toMap(p -> p.getSystemName(), p -> p));
            suppliedUploadFeedProperties.putAll(suppliedFeedProperties);
        }
        //set the list back
        feedToImport.setUserProperties(new HashSet<>(suppliedUploadFeedProperties.values()));

        Map<String, UserProperty>
            requiredFeedUserProperties =
            UserPropertyTransform.toUserProperties(Collections.emptyMap(), UserPropertyTransform.toUserFieldDescriptors(feedUserFields)).stream().filter(p -> Objects.equals(p.isRequired(), Boolean.TRUE))
                .collect(Collectors.toMap(p -> p.getSystemName(), p -> p));
        if (!requiredFeedUserProperties.isEmpty()) {
            boolean feedValid = true;
            UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importFeed.getImportOptions().getUploadKey(), "Validating additional required feed properties.");
            //add back in the required if they dont exist of they are blank
            for (String k : requiredFeedUserProperties.keySet()) {
                boolean supplied = suppliedUploadFeedProperties.containsKey(k);
                if (!supplied || (supplied && StringUtils.isBlank(suppliedUploadFeedProperties.get(k).getValue()))) {
                    if (!supplied) {
                        feedUserFieldOption.getProperties().add(toImportProperty(requiredFeedUserProperties.get(k)));
                    }
                    valid = false;
                    feedValid = false;
                }
            }
            if (feedValid) {
                statusMessage.update("Validated additional required feed properties.", true);
            } else {
                statusMessage.update("Validation Error. Additional properties are needed on the feed before importing.", false);
            }
        }
        if (!valid) {
            importFeed.setValid(false);
        }

        return valid;
    }



    /**
     * Validates that user data sources can be imported with provided properties.
     * Legacy UserDatasources will be remapped toe Catalog DataSources or DataSets
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
        if (componentOption.getProperties().isEmpty()) {
            componentOption.setProperties(FeedImportDatasourceUtil.buildDatasourceAssignmentProperties(metadata, availableDatasources));
            //add in any catalogDataSourceIds
            if(metadata.getDataTransformation().getCatalogDataSourceIds() != null && !metadata.getDataTransformation().getCatalogDataSourceIds().isEmpty()){
                final Set<String> catalogDataSources = metadataAccess.read(
                    () -> catalogDataSourceProvider.findAll().stream()
                        .map(com.thinkbiganalytics.metadata.api.catalog.DataSource::getId)
                        .map(Object::toString)
                        .collect(Collectors.toSet())
                );
                componentOption.getProperties().addAll(FeedImportDatasourceUtil.buildCatalogDatasourceAssignmentProperties(metadata,catalogDataSources));
            }
        }

        // Update feed with re-mapped data sources
        Map<String,String> chartModelReplacements = new HashMap<>();
        String sourceDataSetIds = metadata.getSourceDataSetIds();
        Set<String> datasetIds = StringUtils.isNotBlank(sourceDataSetIds) ? new HashSet<String>(Arrays.asList(sourceDataSetIds)) : new HashSet<String>();
        final boolean valid = componentOption.getProperties().stream()
            .allMatch(property -> {
                if (property.getPropertyValue() != null) {
                    if(property.getAdditionalPropertyValue(FeedImportDatasourceUtil.LEGACY_TABLE_DATA_SOURCE_KEY) != null && "true".equalsIgnoreCase(property.getAdditionalPropertyValue(FeedImportDatasourceUtil.LEGACY_TABLE_DATA_SOURCE_KEY))){
                        //remap
                        String table = property.getAdditionalPropertyValue("table");
                        String datasourceId = property.getAdditionalPropertyValue("datasourceId");
                        String datasetId = property.getPropertyValue();
                        com.thinkbiganalytics.kylo.catalog.rest.model.DataSet dataSet = metadataAccess.read(() -> {
                           return catalogDataSetProvider.find(catalogDataSetProvider.resolveId(datasetId))
                               .map(catalogDataSet -> catalogModelTransform.dataSetToRestModel().apply(catalogDataSet)).orElse(null);
                        });
                       if(dataSet != null){
                           FeedImportDatasourceUtil.replaceLegacyDataSourceScript(metadata, table, datasourceId, dataSet);
                           datasetIds.add(dataSet.getId());
                           //TODO is this needed?
                           chartModelReplacements.put(datasourceId, dataSet.getDataSource().getId());
                           return true;
                       }
                       else {
                           return false;
                       }
                    }
                    if(property.getAdditionalPropertyValue(FeedImportDatasourceUtil.LEGACY_QUERY_DATA_SOURCE_KEY) != null && "true".equalsIgnoreCase(property.getAdditionalPropertyValue(FeedImportDatasourceUtil.LEGACY_QUERY_DATA_SOURCE_KEY))){
                        //remap
                        //thre is only 1 datasource throughout, replace the method call and the datasource id with the new one
                        String datasourceId = property.getAdditionalPropertyValue("datasourceId");
                        String catalogDataSourceId = property.getPropertyValue();
                        com.thinkbiganalytics.kylo.catalog.rest.model.DataSource dataSource = metadataAccess.read(() -> {
                            return catalogDataSourceProvider.find(catalogDataSourceProvider.resolveId(catalogDataSourceId))
                                .map(catalogDataSource -> catalogModelTransform.dataSourceToRestModel().apply(catalogDataSource)).orElse(null);
                        });
                        if(dataSource != null){
                            FeedImportDatasourceUtil.replaceLegacyQueryDataSourceScript(metadata,datasourceId,dataSource);
                            //TODO is this needed?
                            chartModelReplacements.put(datasourceId, dataSource.getId());
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                    if(property.getAdditionalPropertyValue(FeedImportDatasourceUtil.CATALOG_DATASOURCE_KEY) != null && "true".equalsIgnoreCase(property.getAdditionalPropertyValue(FeedImportDatasourceUtil.CATALOG_DATASOURCE_KEY))){
                        String datasourceId = property.getAdditionalPropertyValue("datasourceId");
                        String catalogDataSourceId = property.getPropertyValue();
                        com.thinkbiganalytics.kylo.catalog.rest.model.DataSource dataSource = metadataAccess.read(() -> {
                            return catalogDataSourceProvider.find(catalogDataSourceProvider.resolveId(catalogDataSourceId))
                                .map(catalogDataSource -> catalogModelTransform.dataSourceToRestModel().apply(catalogDataSource)).orElse(null);
                        });

                        if(dataSource != null){
                            List<String> newIds = metadata.getDataTransformation().getCatalogDataSourceIds().stream().map(id -> id.equalsIgnoreCase(datasourceId) ? catalogDataSourceId : id ).collect(Collectors.toList());
                            metadata.getDataTransformation().setCatalogDataSourceIds(newIds);
                            String script = metadata.getDataTransformation().getDataTransformScript();
                            script = script.replaceAll(datasourceId,catalogDataSourceId);
                            metadata.getDataTransformation().setDataTransformScript(script);
                            chartModelReplacements.put(datasourceId, catalogDataSourceId);
                            return true;
                        }
                        else {
                            return false;
                        }

                    }
                    else {
                        //Shouldnt get here now!!
                       // ImportUtil.replaceDatasource(metadata, property.getProcessorId(), property.getPropertyValue());
                        return false;
                    }
                } else {
                    return false;
                }
            });

        if (valid) {
            //make the final replacements and add in the sources
            if(datasetIds != null) {
                metadata.setSourceDataSetIds(datasetIds.stream().collect(Collectors.joining(",")));
            }
            FeedImportDatasourceUtil.ensureConnectionKeysMatch(metadata);
            FeedImportDatasourceUtil.replaceChartModelReferences(metadata, chartModelReplacements);
            FeedImportDatasourceUtil.populateFeedDatasourceIdsProperty(metadata);
            statusMessage.update("Validated data sources.", true);
        } else {
            statusMessage.update("Validation Error. Additional properties are needed before uploading the feed.", false);
            importFeed.setValid(false);
        }

        uploadProgressService.completeSection(importFeed.getImportOptions(), ImportSection.Section.VALIDATE_USER_DATASOURCES);
        return valid;
    }

    /**
     * Find the systems dataset using the following criteria
     * 1) find by ID
     * 2) find DataSource and then by hashing the params of the DataSet
     * 3) find DataSource and then just by the DataSet Title
     * @param dataSet
     * @return
     */
    private com.thinkbiganalytics.kylo.catalog.rest.model.DataSet findMatchingDataSet(com.thinkbiganalytics.kylo.catalog.rest.model.DataSet dataSet){
      return   metadataAccess.read(() -> {
            DataSet catalogDataSet = catalogDataSetProvider.find(catalogDataSetProvider.resolveId(dataSet.getId()))
                .orElseGet(() -> {
                    //find datasource by id
                    DataSource dataSource = catalogDataSourceProvider.find(catalogDataSourceProvider.resolveId(dataSet.getDataSource().getId())).orElseGet(() -> {
                        //find the connector
                        return catalogConnectorProvider.findByPlugin(dataSet.getDataSource().getConnector().getPluginId())
                            .map(connector -> catalogDataSourceProvider.findByConnector(connector.getId()).stream()
                                .filter(ds -> ds.getTitle().equalsIgnoreCase(dataSet.getDataSource().getTitle())).findFirst()).get().orElse(null);
                    });
                    //find the datasource
                    if (dataSource != null) {
                        return catalogDataSetProvider.build(dataSource.getId())
                            .title(dataSet.getTitle())
                            .format(dataSet.getFormat())
                            .addOptions(dataSet.getOptions())
                            .addPaths(dataSet.getPaths())
                            .addFiles(dataSet.getFiles())
                            .addJars(dataSet.getJars()).find().orElse(catalogDataSetProvider.findByDataSourceAndTitle(dataSource.getId(),dataSet.getTitle()));
                    }
                    return null;

                });
            if(catalogDataSet != null){
                return catalogModelTransform.dataSetToRestModel().apply(catalogDataSet);
            }
            else {
                return null;
            }
        });

    }




    
    /**
     * Validates that user data sets can be imported with provided properties.
     *
     * @return {@code true} if the feed can be imported, or {@code false} otherwise
     */
    private boolean validateUserDataSets() {
        List<com.thinkbiganalytics.kylo.catalog.rest.model.DataSet> sourceDataSets = importFeed.getDataSetsToImport();

        if(sourceDataSets != null && !sourceDataSets.isEmpty()) {
            final UploadProgressMessage statusMessage = uploadProgressService.addUploadStatus(importFeed.getImportOptions().getUploadKey(), "Validating data sets.");
            final ImportComponentOption componentOption = importFeedOptions.findImportComponentOption(ImportComponent.USER_DATA_SETS);

            ///Map the orig datasets by their id
            Map<String, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet> origDataSetMap = sourceDataSets.stream().collect(Collectors.toMap(ds -> ds.getId(), ds->ds));

            //create a copy with the map so it can be modified from the user properties
            Map<String, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet> modifiedDataSetMap = sourceDataSets.stream().collect(Collectors.toMap(ds -> ds.getId(), ds->new com.thinkbiganalytics.kylo.catalog.rest.model.DataSet(ds)));

            //look at the properties supplied by the user and apply those first
            List<ImportProperty> properties = componentOption.getProperties();
            properties.stream().forEach(importProperty -> {
                if(StringUtils.isNotBlank(importProperty.getPropertyValue())){
                    com.thinkbiganalytics.kylo.catalog.rest.model.DataSet matchingDataSet = modifiedDataSetMap.get(importProperty.getComponentId());
                    if(matchingDataSet != null) {
                        matchingDataSet.setId(importProperty.getPropertyValue());
                        log.info("Remap dataset old id: {}, new id: {}, details: {} ", importProperty.getComponentId(), importProperty.getPropertyValue(), importProperty);
                    }
                }
            });

            FeedMetadata metadata = importFeed.getFeedToImport();
            //find the data sets that need importing

            Map<String,Map<String,String>> datasetAdditionalProperties = new HashMap<>();

            //find schemas associated with data set for data transform feeds
            if(metadata.getDataTransformation() != null && StringUtils.isNotBlank(metadata.getDataTransformation().getDataTransformScript())) {

                List<Map<String,Object>> nodes = (List<Map<String,Object>>) metadata.getDataTransformation().getChartViewModel().get("nodes");
                if(nodes != null) {
                    nodes.stream().forEach((nodeMap) -> {
                        Map<String, Object> nodeDataSetMap = ( Map<String, Object> ) nodeMap.get("dataset");
                        if(nodeDataSetMap != null) {
                            String dataSetId = (String) nodeDataSetMap.get("id");
                            List<Map<String,String>> schema = (List<Map<String,String>>) nodeDataSetMap.get("schema");
                            if(schema != null) {
                                String schemaString = schema.stream().map(field -> {
                                    Map<String, String> fieldMap = (Map<String, String>) field;
                                    String name = fieldMap.get("name");
                                    String dataType = fieldMap.get("dataType");
                                    return name + " " + dataType;
                                }).collect(Collectors.joining(","));
                                //find the property associated with this dataset and add the schema as an additional property
                                datasetAdditionalProperties.computeIfAbsent(dataSetId, dsId -> new HashMap<String, String>()).put("schema", schemaString);
                            }
                        }
                    });
                }
            }


            //create a map of the zip file datasets and the matching system datasets
            Map<com.thinkbiganalytics.kylo.catalog.rest.model.DataSet, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet> importDataSetIdMap = new HashMap<>();
            //attempt to find the dataset and associate it with the incoming one
            sourceDataSets.stream().forEach(dataSet -> {
                com.thinkbiganalytics.kylo.catalog.rest.model.DataSet modifiedDataSet = modifiedDataSetMap.get(dataSet.getId());
                importDataSetIdMap.put(dataSet, findMatchingDataSet(modifiedDataSet));
            });

            // the list of properties to be returned to the user to reassign datasets
            List<ImportProperty> dataSetProperties = new ArrayList<>();
            boolean valid = true;
            //for all the values that are null they need to be created, otherwise we have what we need
            //if the value in the map is null, we need to ask the user to supply a dataset.  Create the ImportProperty and mark as invalid
            importDataSetIdMap.entrySet().stream().forEach(entry -> {
                com.thinkbiganalytics.kylo.catalog.rest.model.DataSet incomingDataSet = entry.getKey();
                com.thinkbiganalytics.kylo.catalog.rest.model.DataSet matchingDataSet = entry.getValue();
                String datasetPathTitle = incomingDataSet.getPaths().stream().collect(Collectors.joining(","));
                String title = incomingDataSet.getDataSource().getTitle();

                ImportProperty property = ImportPropertyBuilder.anImportProperty().withComponentName(title)
                    .withDisplayName(datasetPathTitle)
                    .withPropertyKey("dataset_" + UUID.randomUUID().toString().replaceAll("-","_"))
                    .withDescription(datasetPathTitle)
                    .withComponentId(incomingDataSet.getId())
                    .withImportComponent(ImportComponent.USER_DATA_SETS)
                    .asValid(matchingDataSet != null)
                    .withAdditionalProperties(datasetAdditionalProperties.get(incomingDataSet.getId()))
                    .putAdditionalProperty("dataset", "true")
                    .build();
                dataSetProperties.add(property);
                componentOption.setValidForImport(property.isValid());
            });
            componentOption.setProperties(dataSetProperties);








            // mark the component as valid only if the dataset properties are all valid
            componentOption.setValidForImport(dataSetProperties.stream().allMatch(ImportProperty::isValid));

            if (componentOption.isValidForImport()) {
                //replace the source datasets with the found ones
                metadata.setSourceDataSets(new ArrayList<>(importDataSetIdMap.values()));
                Set<String> datasourceIds = new HashSet<>();
                Map<String,String> chartModelReplacements = new HashMap<>();
                //replace the Data Transformation dataset references with the new one
                if(metadata.getDataTransformation() != null && StringUtils.isNotBlank(metadata.getDataTransformation().getDataTransformScript())){
                    String script = metadata.getDataTransformation().getDataTransformScript();
                    //iterate through the map of datasets and find/replace the dataset ids and datasource ids with the new ones

                    for(Map.Entry<com.thinkbiganalytics.kylo.catalog.rest.model.DataSet, com.thinkbiganalytics.kylo.catalog.rest.model.DataSet> entry:importDataSetIdMap.entrySet()){
                        com.thinkbiganalytics.kylo.catalog.rest.model.DataSet incomingDataSet = entry.getKey();
                        com.thinkbiganalytics.kylo.catalog.rest.model.DataSet matchingDataSet = entry.getValue();
                        if(!incomingDataSet.getId().equalsIgnoreCase(matchingDataSet.getId())){
                         script = script.replaceAll(incomingDataSet.getId(),matchingDataSet.getId());
                         chartModelReplacements.put(incomingDataSet.getId(),matchingDataSet.getId());
                         chartModelReplacements.put(incomingDataSet.getDataSource().getId(),matchingDataSet.getDataSource().getId());
                        }
                        datasourceIds.add(matchingDataSet.getDataSource().getId());

                        metadata.getDataTransformation().setDatasourceIds(new ArrayList<>(datasourceIds));
                    }
                    metadata.getDataTransformation().setDataTransformScript(script);
                    FeedImportDatasourceUtil.replaceChartModelReferences(metadata, chartModelReplacements);


                }
                statusMessage.update("Validated data sets.", true);
            } else {
                statusMessage.update("Validation Error. Additional properties are needed before uploading the feed.", false);
                importFeed.setValid(false);
            }

            uploadProgressService.completeSection(importFeed.getImportOptions(), ImportSection.Section.VALIDATE_USER_DATASOURCES);
            return componentOption.isValidForImport();
        }
        return true;
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
                    if (domainCategory == null || (!accessController.hasPermission(domainCategory, CategoryAccessControl.CREATE_FEED))) {
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


    private ImportProperty toImportProperty(UserProperty userProperty) {

       return  ImportPropertyBuilder.anImportProperty()
            .withDisplayName(userProperty.getDisplayName())
            .withPropertyKey(userProperty.getSystemName())
            .withPropertyValue(userProperty.getValue())
            .withDescription(userProperty.getDescription())
            .withImportComponent(ImportComponent.FEED_USER_FIELDS)
            .build();
    }

    private UserProperty toUserProperty(ImportProperty importProperty) {
        UserProperty userProperty = new UserProperty();
        userProperty.setSystemName(importProperty.getPropertyKey());
        userProperty.setDisplayName(importProperty.getDisplayName());
        userProperty.setDescription(importProperty.getDescription());
        userProperty.setValue(importProperty.getPropertyValue());
        return userProperty;
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
            if (zipEntry.getName().startsWith(ImportFeed.FEED_DATASETS_FILE)) {
                String zipEntryContents = ZipFileUtil.zipEntryToString(buffer, zis, zipEntry);
                importFeed.setDatasets(zipEntryContents);
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
