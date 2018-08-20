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
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.metadata.api.template.export.ExportTemplate;
import com.thinkbiganalytics.metadata.api.template.export.TemplateExporter;
import com.thinkbiganalytics.repository.api.TemplateMetadata;
import com.thinkbiganalytics.repository.api.TemplateMetadataWrapper;
import com.thinkbiganalytics.repository.api.TemplateRepository;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@SpringBootTest(classes = {RepositoryTestConfiguration.class})
@PowerMockRunnerDelegate(SpringRunner.class)
@ComponentScan(basePackages = {"com.thinkbiganalytics.repository"})
@PrepareForTest({Files.class, Paths.class, FilesystemRepositoryService.class, FilenameUtils.class})
public class FilesystemRepositoryServiceTest {

    @Autowired
    @InjectMocks
    private FilesystemRepositoryService filesystemRepositoryService;

    @Mock
    TemplateRepository repository;

    @Mock
    ExportTemplate exportTemplate;

    @Mock
    TemplateExporter templateExporter;

    @Mock
    ObjectMapper mapper;

    @Inject
    RegisteredTemplateService registeredTemplateService;

    @Before
    public void mockSetup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(Files.class);
        mockStatic(Paths.class);
        mockStatic(FilenameUtils.class);
    }

    @Test
    public void testPublishTemplate_NewTemplateInRepo_NoOverwriteRequired() throws Exception {
        ExportTemplate template = new ExportTemplate("testFile", "testTemplate", "testDesc", false, new byte[0]);
        when(templateExporter.exportTemplate(anyString())).thenReturn(template);
        FilesystemRepositoryService spy = PowerMockito.spy(filesystemRepositoryService);

        doReturn(repository).when(spy, "getRepositoryByNameAndType", anyString(), anyString());
        doReturn(new ArrayList<TemplateMetadata>()).when(spy, "listTemplatesByRepository", repository);
        PowerMockito.when(FilenameUtils.getBaseName(anyString())).thenReturn("testName");
        Path pathMock = mock(Path.class);
        PowerMockito.when(Paths.get(anyString())).thenReturn(pathMock);
        Mockito.when(pathMock.toFile()).thenReturn(mock(File.class));
        Mockito.doNothing().when(mapper).writeValue(any(File.class), any());

        TemplateMetadataWrapper newTemplate = spy.publishTemplate("test", "test", "test", false);

        assertNotNull(newTemplate);
        assertTrue(newTemplate.getTemplateName().equals("testTemplate"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPublishTemplate_ExistingTemplate_OverwriteFalse() throws Exception {
        ExportTemplate template = new ExportTemplate("testFile", "testTemplate", "testDesc", false, new byte[0]);
        TemplateMetadata existingTemplate = new TemplateMetadata("testTemplate", "testDesc", "xyz.zip", false);
        List<TemplateMetadataWrapper> templates = new ArrayList<>();
        templates.add(new TemplateMetadataWrapper(existingTemplate));
        when(templateExporter.exportTemplate(anyString())).thenReturn(template);
        FilesystemRepositoryService spy = PowerMockito.spy(filesystemRepositoryService);

        doReturn(repository).when(spy, "getRepositoryByNameAndType", anyString(), anyString());
        doReturn(templates).when(spy, "listTemplatesByRepository", repository);

        spy.publishTemplate("test", "test", "test",false);

        PowerMockito.verifyStatic(never());
        FilenameUtils.getBaseName(anyString());
    }

}
