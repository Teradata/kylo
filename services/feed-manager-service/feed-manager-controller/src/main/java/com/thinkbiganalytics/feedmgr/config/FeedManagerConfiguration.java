package com.thinkbiganalytics.feedmgr.config;

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


import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.SpringCloudContextEnvironmentChangedListener;
import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCacheClusterManager;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCacheImpl;
import com.thinkbiganalytics.feedmgr.rest.Model;
import com.thinkbiganalytics.feedmgr.rest.model.ImportFeedOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.service.DefaultJobService;
import com.thinkbiganalytics.feedmgr.service.FeedManagerMetadataService;
import com.thinkbiganalytics.feedmgr.service.MetadataModelTransform;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.category.CategoryModelTransform;
import com.thinkbiganalytics.feedmgr.service.category.DefaultFeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.category.FeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceService;
import com.thinkbiganalytics.feedmgr.service.domaintype.DomainTypeTransform;
import com.thinkbiganalytics.feedmgr.service.feed.DefaultFeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedHiveTableService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerPreconditionService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedModelTransform;
import com.thinkbiganalytics.feedmgr.service.feed.datasource.DerivedDatasourceFactory;
import com.thinkbiganalytics.feedmgr.service.feed.exporting.FeedExporter;
import com.thinkbiganalytics.feedmgr.service.feed.importing.FeedImporter;
import com.thinkbiganalytics.feedmgr.service.feed.importing.FeedImporterFactory;
import com.thinkbiganalytics.feedmgr.service.security.DefaultSecurityService;
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.feedmgr.service.template.exporting.TemplateExporter;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporter;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporterFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportReusableTemplateFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.validation.AbstractValidateImportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.DefaultFeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportFeedTemplateXml;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportReusableTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportTemplateArchive;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportTemplateRoutine;
import com.thinkbiganalytics.feedmgr.service.template.importing.importprocess.ImportTemplateRoutineFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.validation.ValidateImportTemplateFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.validation.ValidateImportTemplateXml;
import com.thinkbiganalytics.feedmgr.service.template.importing.validation.ValidateImportTemplatesArchive;
import com.thinkbiganalytics.feedmgr.service.template.NiFiTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateUtil;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.feedmgr.sla.DefaultServiceLevelAgreementService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementModelTransform;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.jobrepo.service.JobService;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.core.feed.FeedPreconditionService;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.security.core.encrypt.EncryptionService;
import com.thinkbiganalytics.spring.SpringEnvironmentProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Spring Bean configuration for feed manager
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
public class FeedManagerConfiguration {

    @Inject
    private Environment env;

    public FeedManagerConfiguration() {
    }

    @Bean
    public FeedManagerFeedService feedManagerFeedService() {
        return new DefaultFeedManagerFeedService();
    }

    @Bean
    public FeedManagerCategoryService feedManagerCategoryService() {
        return new DefaultFeedManagerCategoryService();
    }


    @Bean
    public FeedManagerTemplateService feedManagerTemplateService() {
        return new DefaultFeedManagerTemplateService();
    }

    @Bean(name = "metadataModelTransform")
    public MetadataModelTransform metadataTransform() {
        return new MetadataModelTransform();
    }

    @Bean
    public FeedModelTransform feedModelTransformer() {
        return new FeedModelTransform();
    }


    @Bean
    public TemplateModelTransform templateModelTransform() {
        return new TemplateModelTransform();
    }


    @Bean
    public CategoryModelTransform categoryModelTransform() {
        return new CategoryModelTransform();
    }


    @Bean
    public FeedManagerPreconditionService feedManagerPreconditionService() {
        return new FeedManagerPreconditionService();
    }

    @Bean
    public SpringEnvironmentProperties springEnvironmentProperties() {
        return new SpringEnvironmentProperties();
    }

    @Bean
    public SpringCloudContextEnvironmentChangedListener springEnvironmentChangedListener() {
        return new SpringCloudContextEnvironmentChangedListener();
    }

    @Bean
    public MetadataService metadataService() {
        return new FeedManagerMetadataService();
    }

    @Bean
    public SecurityService securityService() {
        return new DefaultSecurityService();
    }

    @Bean
    public PropertyExpressionResolver propertyExpressionResolver() {
        return new PropertyExpressionResolver();
    }

    @Bean
    public NifiFlowCache nifiFlowCache() {
        return new NifiFlowCacheImpl();
    }


    @Bean
    public ServiceLevelAgreementService serviceLevelAgreementService() {
        return new DefaultServiceLevelAgreementService();
    }


    @Bean
    public FeedPreconditionService feedPreconditionService() {
        return new FeedPreconditionService();
    }


    @Bean
    public DatasourceService datasourceService() {
        return new DatasourceService();
    }

    @Bean
    public DerivedDatasourceFactory derivedDatasourceFactory() {
        return new DerivedDatasourceFactory();
    }

    @Bean
    public JobService jobService() {
        return new DefaultJobService();
    }

    @Bean
    public EncryptionService encryptionService() {
        return new EncryptionService();
    }

    @Bean
    public RegisteredTemplateService registeredTemplateService() {
        return new RegisteredTemplateService();
    }

    @Bean
    public RegisteredTemplateUtil registeredTemplateUtil() {
        return new RegisteredTemplateUtil();
    }

    @Bean
    public NiFiTemplateCache niFiTemplateCache() {
        return new NiFiTemplateCache();
    }

    @Bean
    public UploadProgressService uploadProgressService() {
        return new UploadProgressService();
    }

    /**
     * Transforms objects between {@link com.thinkbiganalytics.metadata.rest.model.data.Datasource} and {@link com.thinkbiganalytics.metadata.api.datasource.Datasource}.
     *
     * @param datasourceProvider the {@link com.thinkbiganalytics.metadata.api.datasource.Datasource} provider
     * @param textEncryptor      the encryption provider
     * @param niFiRestClient     the NiFi REST client
     * @param securityService    the security service
     * @return the model transformer
     */
    @Bean
    @Nonnull
    public DatasourceModelTransform datasourceModelTransform(@Nonnull final DatasourceProvider datasourceProvider, @Nonnull final TextEncryptor textEncryptor,
                                                             @Nonnull final NiFiRestClient niFiRestClient, @Nonnull final SecurityService securityService) {
        return new DatasourceModelTransform(datasourceProvider, textEncryptor, niFiRestClient, securityService);
    }

    /**
     * Transforms objects between different feed models and domain objects.
     *
     * @param datasourceTransform the {@code Datasource} object transformer
     * @return the model transformer
     */
    @Bean
    @Nonnull
    public Model model(@Nonnull final DatasourceModelTransform datasourceTransform) {
        return new Model(datasourceTransform);
    }

    /**
     * Transforms SLA objects between the REST model and the domain object.
     *
     * @param model the model transformer
     * @return the SLA transformer
     */
    @Bean
    @Nonnull
    public ServiceLevelAgreementModelTransform serviceLevelAgreementModelTransform(@Nonnull final Model model) {
        return new ServiceLevelAgreementModelTransform(model);
    }


    @Bean
    public NifiFlowCacheClusterManager nifiFlowCacheClusterManager() {
        return new NifiFlowCacheClusterManager();
    }

    /**
     * Transforms domain type objects between the REST and domain models.
     */
    @Bean
    public DomainTypeTransform domainTypeTransform() {
        return new DomainTypeTransform();
    }

    @Bean
    public FeedHiveTableService feedHiveTableService(@Nonnull final HiveService hiveService) {
        return new FeedHiveTableService(hiveService);
    }

    @Bean
    public RegisteredTemplateCache registeredTemplateCache() {
        return new RegisteredTemplateCache();
    }

    @Bean
    public TemplateConnectionUtil templateConnectionUtil() {
        return new TemplateConnectionUtil();
    }

    @Bean
    public FeedExporter feedExporter(){
        return new FeedExporter();
    }

    @Bean
    public TemplateExporter templateExporter(){
        return new TemplateExporter();
    }


    @Bean
    public ValidateImportTemplateFactory<ImportTemplate,ImportTemplateOptions, ImportTemplate.TYPE,AbstractValidateImportTemplate> validateImportTemplateFactory() {
        return (importTemplate, importTemplateOptions, importType) -> validateImportTemplate(importTemplate, importTemplateOptions, importType);
    }

    @Bean
    @Scope(value = "prototype")
    public AbstractValidateImportTemplate validateImportTemplate(ImportTemplate importTemplate, ImportTemplateOptions importTemplateOptions,ImportTemplate.TYPE importType) {
        if(importType == ImportTemplate.TYPE.ARCHIVE) {
            return new ValidateImportTemplatesArchive(importTemplate, importTemplateOptions);
        }
        else {
            return new ValidateImportTemplateXml(importTemplate, importTemplateOptions);
        }
    }



    @Bean
    public TemplateImporterFactory<ImportOptions,TemplateImporter> templateImporterFactory() {
        return (fileName, content, importOptions) -> templateImporter(fileName,content, importOptions);
    }

    @Bean
    @Scope(value = "prototype")
    public TemplateImporter templateImporter(String fileName, byte[] content, ImportOptions importOptions) {
        return new TemplateImporter(fileName,content, importOptions);
    }

    @Bean
    public FeedImporterFactory<ImportFeedOptions,FeedImporter> feedImporterFactory() {
        return (fileName, content, importFeedOptions) -> feedImporter(fileName,content, importFeedOptions);
    }

    @Bean
    @Scope(value = "prototype")
    public FeedImporter feedImporter(String fileName, byte[] content, ImportFeedOptions importFeedOptions) {
        return new FeedImporter(fileName,content, importFeedOptions);
    }




    @Bean
    public ImportReusableTemplateFactory<ImportTemplateOptions,ImportReusableTemplate> importReusableTemplateArchiveFactory() {
        return (fileName, content, importTemplateOptions) -> importReusableTemplate(fileName,content, importTemplateOptions);
    }

    @Bean
    @Scope(value = "prototype")
    public ImportReusableTemplate importReusableTemplate(String fileName, byte[] content, ImportTemplateOptions importTemplateOptions) {
        return new ImportReusableTemplate(fileName,content, importTemplateOptions);
    }


    @Bean
    public ImportTemplateRoutineFactory<ImportTemplate,ImportTemplateOptions,ImportTemplate.TYPE,ImportTemplateRoutine> importTemplateRoutineFactory() {
        return (importTemplate, importTemplateOptions, importType) -> importTemplateRoutine(importTemplate, importTemplateOptions, importType);
    }

    @Bean
    @Scope(value = "prototype")
    public ImportTemplateRoutine importTemplateRoutine(ImportTemplate importTemplate, ImportTemplateOptions importTemplateOptions, ImportTemplate.TYPE importType) {
        if(importType == ImportTemplate.TYPE.ARCHIVE) {
            return new ImportTemplateArchive(importTemplate,importTemplateOptions);
        }
        else if(importType == ImportTemplate.TYPE.REUSABLE_TEMPLATE) {
            return new ImportReusableTemplate(importTemplate, importTemplateOptions);
        }
        else {
            return new ImportFeedTemplateXml(importTemplate, importTemplateOptions);
        }

    }






}
