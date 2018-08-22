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
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.repository.api.TemplateMetadata;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;

import javax.inject.Inject;

/**
 * Monitors the default repository and any repositories configured in repositories.json.
 * Generates json metadata for any .zip templates, if not already present.
 * Monitors only for any file creations or deletions.
 *
 */
@Component
public class RepositoryMonitor {

    @Inject
    ObjectMapper mapper;

    @Inject
    Cache<String, Long> templateUpdateInfoCache;

    private static final Logger log = LoggerFactory.getLogger(RepositoryMonitor.class);

    private final WatchService watcher;
    private final WatchEvent.Kind[] events = {StandardWatchEventKinds.ENTRY_CREATE,
                                              StandardWatchEventKinds.ENTRY_DELETE,
                                              StandardWatchEventKinds.ENTRY_MODIFY};

    public RepositoryMonitor() {
        WatchService watcher = null;
        try {
            watcher = FileSystems.getDefault().newWatchService();
        }catch(Exception e) {
            log.error("Error trying to setup watch service for template repositories", e);
        }
        this.watcher = watcher;
    }

    public void watchRepositories(Set<Path> repositoriesToWatch) {

        log.info("Started repository monitoring");
        try {
            repositoriesToWatch.forEach(path -> addMonitorToRepository(path));
            while (true) {
                WatchKey key = watcher.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> ev = cast(event);
                    if(ev.context().getFileName().toString().endsWith(".zip")){
                        WatchEvent.Kind kind = event.kind();
                        Path repositoryPath = (Path)key.watchable();

                        if(kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            createMetadata(repositoryPath.resolve(ev.context()));
                        } else if(kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            Files.deleteIfExists(getMetadataFileName(repositoryPath.resolve(ev.context())));
                            log.info("{}: {} and its metadata.", ev.kind(), ev.context());
                        }
                    }
                }

                if (!key.reset()) {
                    log.info("Exiting repository monitoring.");
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while monitoring repository changes", e);
        }
    }

    private Path getMetadataFileName(Path templateFile) {
        return templateFile.getParent().resolve(FilenameUtils.getBaseName(templateFile.getFileName().toString()) + ".json");
    }

    private void createMetadata(Path templateFilePath) {
        try {
            byte[] content = ImportUtil.streamToByteArray(new FileInputStream(templateFilePath.toFile()));
            InputStream inputStream = new ByteArrayInputStream(content);
            ImportTemplate importTemplate = ImportUtil.openZip(templateFilePath.getFileName().toString(), inputStream);
            RegisteredTemplate tmplt = importTemplate.getTemplateToImport();

            boolean updateRequired = false;
            //no changes required if template is not updated
            if(templateUpdateInfoCache.getIfPresent(tmplt.getTemplateName()) !=null){
               if(templateUpdateInfoCache.getIfPresent(tmplt.getTemplateName()) >= tmplt.getUpdateDate().getTime())
                   return;

               updateRequired = true;
            }

            TemplateMetadata metadata = new TemplateMetadata(tmplt.getTemplateName(), tmplt.getDescription(),
                                                             templateFilePath.getFileName().toString(), DigestUtils.md5DigestAsHex(content),
                                                             tmplt.isStream(), updateRequired, tmplt.getUpdateDate().getTime());
            log.info("Writing metadata for {} template.", tmplt.getTemplateName());
            File json = getMetadataFileName(templateFilePath).toFile();
            mapper.writerWithDefaultPrettyPrinter().writeValue(json, metadata);
        } catch(Exception e) {
            log.error("Error occurred while trying to generate metadata a template file.", e);
        }
    }

    private void generateMissingMetadata(Path repositoryPath) throws Exception {
        Files.find(repositoryPath, 1, (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".zip"))
        .forEach(templateZip -> {
            if(Files.notExists(getMetadataFileName(templateZip))){
                createMetadata(templateZip);
            }
        });
    }

    private void addMonitorToRepository(Path repositoryPath) {
        try {
            generateMissingMetadata(repositoryPath);
            repositoryPath.register(watcher, events);
        } catch (Exception e) {
            log.error("Error occurred while trying to setup {} monitor", repositoryPath, e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }
}
