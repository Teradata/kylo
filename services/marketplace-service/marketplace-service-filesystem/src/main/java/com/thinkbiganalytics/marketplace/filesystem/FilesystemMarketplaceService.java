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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporter;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporterFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.marketplace.MarketplaceItemMetadata;
import com.thinkbiganalytics.marketplace.MarketplaceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

@Service
@PropertySource("classpath:filesystem-marketplace.properties")
public class FilesystemMarketplaceService implements MarketplaceService {

    private static final Logger log = LoggerFactory.getLogger(FilesystemMarketplaceService.class);

    @Value("${templates.location}")
    private String templateLocation;

    @Inject
    TemplateImporterFactory templateImporterFactory;

    @Inject
    UploadProgressService uploadProgressService;

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<MarketplaceItemMetadata> listTemplates() throws Exception {
        //get JSON files from samples directory
        //TODO and check if templates exist
        try (Stream<Path> stream = Files.find(Paths.get(templateLocation),
                                              Integer.MAX_VALUE,
                                              (path, attrs) -> attrs.isRegularFile()
                                                               && path.toString().endsWith(".json"))) {
            return stream.map((p) -> jsonToMetadata(p)).collect(Collectors.toList());
        }
    }

    @Override
    public List<ImportTemplate> importTemplates(List<String> templateFileNames) throws Exception {
        log.info("Templates to import {}", templateFileNames.size());
        List<ImportTemplate> statusList = new ArrayList<>();
        for (String fileName : templateFileNames) {
            log.info("Begin template import {}", fileName);
            File template = new File(templateLocation + "/" + fileName);
            ImportTemplateOptions options = new ImportTemplateOptions();
            String uploadKey = uploadProgressService.newUpload();
            options.setUploadKey(uploadKey);
            options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setShouldImport(true);
            options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setUserAcknowledged(true);
            options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setOverwrite(true);
            options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setOverwrite(true);
            options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setUserAcknowledged(true);
            options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setShouldImport(true);
            options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setContinueIfExists(false);
            options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setOverwrite(true);
            options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setUserAcknowledged(true);
            options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setShouldImport(true);

            byte[] content = ImportUtil.streamToByteArray(new FileInputStream(template));
            TemplateImporter templateImporter = templateImporterFactory.apply(fileName, content, options);
            ImportTemplate importTemplate = templateImporter.validateAndImport();
            log.info("End template import {} - {}", fileName, importTemplate.isSuccess());
            String jsonFileName = com.google.common.io.Files.getNameWithoutExtension(fileName) + ".json";
            MarketplaceItemMetadata metadata = new MarketplaceItemMetadata(importTemplate.getTemplateName(),
                                                                           importTemplate.getFileName(), "", importTemplate.isSuccess());
            mapper.writeValue(new File(templateLocation + "/" + jsonFileName), metadata);
            log.info("Updated {} metadata with {}", jsonFileName, metadata);

            statusList.add(importTemplate);
        }

        return statusList;
    }

    private MarketplaceItemMetadata jsonToMetadata(Path path) {
        String s = null;
        try {
            s = new String(Files.readAllBytes(path));
            return mapper.readValue(s, MarketplaceItemMetadata.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static void main(String[] args) throws Exception {
//        ApplicationContext ctx =
//            new AnnotationConfigApplicationContext(FilesystemConfig.class);
//        ctx.getBean(FilesystemMarketplaceService.class).listTemplates().forEach(d -> System.out.println(d.getFileName()));
//    }
}
