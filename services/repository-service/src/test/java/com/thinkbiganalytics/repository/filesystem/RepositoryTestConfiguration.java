package com.thinkbiganalytics.repository.filesystem;

import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.NiFiTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateUtil;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporterFactory;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.template.export.TemplateExporter;
import com.thinkbiganalytics.nifi.rest.NiFiObjectCache;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.core.encrypt.EncryptionService;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.service.user.UserService;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

//@Configuration
@ContextConfiguration
public class RepositoryTestConfiguration {

    @Bean
    public FilesystemRepositoryService filesystemRepositoryService() { return new FilesystemRepositoryService(); }

    @Bean
    public TemplateImporterFactory templateImporterFactory() {return Mockito.mock(TemplateImporterFactory.class); }

    @Bean
    public UploadProgressService uploadProgressService() {return Mockito.mock(UploadProgressService.class); }

    @Bean
    public RegisteredTemplateService registeredTemplateService() {return Mockito.mock(RegisteredTemplateService.class); }

    @Bean
    public TemplateExporter templateExporter() {return Mockito.mock(TemplateExporter.class); }

    @Bean
    public FeedManagerTemplateProvider feedManagerTemplateProvider() {return Mockito.mock(FeedManagerTemplateProvider.class); }

    @Bean
    public MetadataAccess metadataAccess() {return Mockito.mock(MetadataAccess.class); }

    @Bean
    public TemplateModelTransform templateModelTransform() {return Mockito.mock(TemplateModelTransform.class); }

    @Bean
    public SecurityModelTransform securityTransform() {return Mockito.mock(SecurityModelTransform.class); }

    @Bean
    public UserService userService() {return Mockito.mock(UserService.class); }

    @Bean
    public EncryptionService encryptionService() {return Mockito.mock(EncryptionService.class); }

    @Bean
    public AccessController accessController() {return Mockito.mock(AccessController.class); }

    @Bean
    public LegacyNifiRestClient nifiRestClient() {return Mockito.mock(LegacyNifiRestClient.class); }

    @Bean
    public RegisteredTemplateUtil registeredTemplateUtil() {return Mockito.mock(RegisteredTemplateUtil.class); }

    @Bean
    public NiFiTemplateCache niFiTemplateCache() {return Mockito.mock(NiFiTemplateCache.class); }

    @Bean
    public RegisteredTemplateCache registeredTemplateCache() {return Mockito.mock(RegisteredTemplateCache.class); }

    @Bean
    public NiFiRestClient client() {return Mockito.mock(NiFiRestClient.class); }

    @Bean
    public NiFiPropertyDescriptorTransform propertyDescriptorTransform() {return Mockito.mock(NiFiPropertyDescriptorTransform.class); }

    @Bean
    public NiFiObjectCache niFiObjectCache() {return Mockito.mock(NiFiObjectCache.class); }

}
