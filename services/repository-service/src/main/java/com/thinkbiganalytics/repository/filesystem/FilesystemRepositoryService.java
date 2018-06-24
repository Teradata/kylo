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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporter;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporterFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.template.export.ExportTemplate;
import com.thinkbiganalytics.metadata.api.template.export.TemplateExporter;
import com.thinkbiganalytics.repository.api.RepositoryItemMetadata;
import com.thinkbiganalytics.repository.api.RepositoryService;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Service
@PropertySource("classpath:filesystem-repository.properties")
public class FilesystemRepositoryService implements RepositoryService {

    private static final Logger log = LoggerFactory.getLogger(FilesystemRepositoryService.class);

    @Value("#{'${template.dirs}'.split(',')}")
    private List<String> templateDirs;

    @Inject
    TemplateImporterFactory templateImporterFactory;

    @Inject
    UploadProgressService uploadProgressService;

    @Inject
    private RegisteredTemplateService registeredTemplateService;

    @Inject
    TemplateExporter templateExporter;

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<RepositoryItemMetadata> listTemplates() {
        //get JSON files from samples directory
        List<RepositoryItemMetadata> metadataList = new ArrayList<>();
        findFiles(metadataList);

        Set<String> registeredTemplates = registeredTemplateService.getRegisteredTemplates().stream().map(t -> t.getTemplateName()).collect(Collectors.toSet());

        metadataList.stream().forEach(m -> m.setInstalled(registeredTemplates.contains(m.getTemplateName())));

        return metadataList;
    }

    @Override
    public ImportTemplate importTemplates(String fileName, String uploadKey, String importComponents) throws Exception {
        validateTemplateDirs();

        Optional<Path> opt = templateDirs
            .stream()
            .map(dir -> Paths.get(dir + "/" + fileName))
            .filter(p -> Files.exists(p))
            .findFirst();

        if (!opt.isPresent()) {
            throw new RuntimeException("Unable to find file to import: " + fileName);
        }

        log.info("Begin template import {}", fileName);
        ImportTemplateOptions options = new ImportTemplateOptions();
        options.setUploadKey(uploadKey);

        byte[] content = ImportUtil.streamToByteArray(new FileInputStream(opt.get().toFile()));
        uploadProgressService.newUpload(uploadKey);
        ImportTemplate importTemplate = null;
        TemplateImporter templateImporter = null;
        if (importComponents == null) {
            templateImporter = templateImporterFactory.apply(fileName, content, options);
            importTemplate = templateImporter.validate();
            importTemplate.setSuccess(false);
        } else {
            options.setImportComponentOptions(ObjectMapperSerializer.deserialize(importComponents, new TypeReference<Set<ImportComponentOption>>() {
            }));
            templateImporter = templateImporterFactory.apply(fileName, content, options);
            importTemplate = templateImporter.validateAndImport();
        }
        log.info("End template import {} - {}", fileName, importTemplate.isSuccess());

        return importTemplate;

    }

    @Override
    public RepositoryItemMetadata publishTemplate(String templateId, boolean overwrite) throws Exception {
        validateTemplateDirs();

        //export template
        ExportTemplate zipFile = templateExporter.exportTemplate(templateId);

        //Check if template already exists
        Optional<RepositoryItemMetadata> foundMetadataOpt = Optional.empty();
        for (RepositoryItemMetadata m : listTemplates()) {
            if (m.getTemplateName().equals(zipFile.getTemplateName())) {
                foundMetadataOpt = Optional.of(m);
                break;
            }
        }
        if (foundMetadataOpt.isPresent()) {
            if (!overwrite) {
                throw new UnsupportedOperationException("Cannot publish to repository, template with same name already exists");
            }
            log.info("Overwriting template with same name.");
        }

        //create metadata
        RepositoryItemMetadata
            metadata =
            foundMetadataOpt.isPresent() ? foundMetadataOpt.get() : new RepositoryItemMetadata(zipFile.getTemplateName(), zipFile.getDescription(), zipFile.getFileName(), zipFile.isStream());
        String baseName = FilenameUtils.getBaseName(metadata.getFileName());
        log.info("Writing metadata for {} template.", metadata.getTemplateName());
        mapper.writeValue(Paths.get(templateDirs.get(0) + "/" + baseName + ".json").toFile(), metadata);

        //write file in first of templateLocations
        log.info("Now publishing template {} to repository.", metadata.getTemplateName());
        Files.write(Paths.get(templateDirs.get(0) + "/" + metadata.getFileName()), zipFile.getFile());
        log.info("Finished publishing template {} to repository.", metadata.getTemplateName());

        return metadata;
    }

    @Override
    public byte[] downloadTemplate(String fileName) throws Exception {
        validateTemplateDirs();

        Optional<Path> opt = templateDirs
            .stream()
            .map(dir -> Paths.get(dir + "/" + fileName))
            .filter(p -> Files.exists(p))
            .findFirst();

        if (!opt.isPresent()) {
            throw new RuntimeException("File not found for download: " + fileName);
        }
        log.info("Begin template download {}", fileName);

        return ImportUtil.streamToByteArray(new FileInputStream(opt.get().toFile()));
    }

    private RepositoryItemMetadata jsonToMetadata(Path path) {
        try {
            String s = new String(Files.readAllBytes(path));
            return mapper.readValue(s, RepositoryItemMetadata.class);
        } catch (IOException e) {
            log.error("Error reading metadata from {}", e);
        }
        return null;
    }

    private void findFiles(List<RepositoryItemMetadata> metadataList) {
        validateTemplateDirs();

        templateDirs.stream().flatMap(dir -> {
            try {
                return Files.find(Paths.get(dir),
                                  Integer.MAX_VALUE,
                                  (path, attrs) -> attrs.isRegularFile()
                                                   && path.toString().endsWith(".json"));
            } catch (Exception e) {
                log.error("Error reading repository metadata for templates", e);
            }
            return Collections.<Path>emptyList().stream();
        }).map((p) -> jsonToMetadata(p)).forEach(metadataList::add);
    }

    private void validateTemplateDirs() {
        if (templateDirs == null || templateDirs.size() == 0) {
            throw new RuntimeException("Location for templates or feeds is not set.");
        }
    }
}
