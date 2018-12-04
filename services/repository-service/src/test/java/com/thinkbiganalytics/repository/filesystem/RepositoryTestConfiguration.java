package com.thinkbiganalytics.repository.filesystem;

/*-
 * #%L
 * kylo-repository-service
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.NiFiTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateUtil;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporterFactory;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEvent;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
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

    @Bean
    public Cache<String, Boolean> templateUpldateInfoCache() {return Mockito.mock(Cache.class); }

    @Bean
    public MetadataEventService eventService() { return Mockito.mock(MetadataEventService.class); }

    @Bean
    public RepositoryMonitor repositoryMonitor() { return Mockito.mock(RepositoryMonitor.class); }

    @Bean
    public ObjectMapper objectMapper() { return Mockito.mock(ObjectMapper.class); }

}
