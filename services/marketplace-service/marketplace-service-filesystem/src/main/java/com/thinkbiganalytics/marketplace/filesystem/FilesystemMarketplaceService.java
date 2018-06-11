package com.thinkbiganalytics.marketplace.filesystem;

/*-
 * #%L
 * marketplace-service-filesystem
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
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporter;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporterFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.marketplace.MarketplaceItemMetadata;
import com.thinkbiganalytics.marketplace.MarketplaceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

@Service
@PropertySource("classpath:filesystem-marketplace.properties")
public class FilesystemMarketplaceService implements MarketplaceService {

    private static final Logger log = LoggerFactory.getLogger(FilesystemMarketplaceService.class);

    @Value("${templates.dir}")
    private String templateLocation;

    @Value("${feeds.dir}")
    private String feedsLocation;

    @Inject
    TemplateImporterFactory templateImporterFactory;

    @Inject
    UploadProgressService uploadProgressService;

    @Inject
    private RegisteredTemplateService registeredTemplateService;

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<MarketplaceItemMetadata> listTemplates() throws Exception {
        //get JSON files from samples directory
        List<MarketplaceItemMetadata> metadataList = new ArrayList<>();
        findFiles(metadataList, templateLocation);
        findFiles(metadataList, feedsLocation);

        Set<String> registeredTemplates = registeredTemplateService.getRegisteredTemplates().stream().map(t -> t.getTemplateName()).collect(Collectors.toSet());

        metadataList.stream().forEach(m -> m.setInstalled(registeredTemplates.contains(m.getTemplateName())));

        return metadataList;
    }

    @Override
    public ImportTemplate importTemplates(String fileName, String uploadKey, String importComponents) throws Exception {

        log.info("Begin template import {}", fileName);
        File file = new File(templateLocation + "/" + fileName);
        if(!file.exists()){
            file = new File(feedsLocation + "/" + fileName);
        }
        if(!file.exists())
            throw new RuntimeException("Unable to find file to import: "+fileName);

        ImportTemplateOptions options = new ImportTemplateOptions();
        options.setUploadKey(uploadKey);

        byte[] content = ImportUtil.streamToByteArray(new FileInputStream(file));
        uploadProgressService.newUpload(uploadKey);
        ImportTemplate importTemplate = null;
        TemplateImporter templateImporter = null;
        if (importComponents == null) {
            templateImporter = templateImporterFactory.apply(fileName, content, options);
            importTemplate = templateImporter.validate();
            importTemplate.setSuccess(false);
        } else {
            options.setImportComponentOptions(ObjectMapperSerializer.deserialize(importComponents, new TypeReference<Set<ImportComponentOption>>() {}));
            templateImporter = templateImporterFactory.apply(fileName, content, options);
            importTemplate = templateImporter.validateAndImport();
        }
        log.info("End template import {} - {}", fileName, importTemplate.isSuccess());

        return importTemplate;

    }

    private MarketplaceItemMetadata jsonToMetadata(Path path) {
        String s = null;
        try {
            s = new String(Files.readAllBytes(path));
            return mapper.readValue(s, MarketplaceItemMetadata.class);
        } catch (IOException e) {
            log.error("Error reading metadata from {}", e);
        }
        return null;
    }

    private void findFiles(List<MarketplaceItemMetadata> metadataList, String templateLocation) throws Exception {
        if (templateLocation != null) {
            try (Stream<Path> stream = Files.find(Paths.get(templateLocation),
                                                  Integer.MAX_VALUE,
                                                  (path, attrs) -> attrs.isRegularFile()
                                                                   && path.toString().endsWith(".json"))) {
                stream.map((p) -> jsonToMetadata(p)).forEach(metadataList::add);
            }
        }
    }
}
