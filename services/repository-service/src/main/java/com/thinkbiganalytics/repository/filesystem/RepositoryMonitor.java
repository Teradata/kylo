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
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.template.export.ExportTemplate;
import com.thinkbiganalytics.metadata.api.template.export.TemplateExporter;
import com.thinkbiganalytics.repository.api.TemplateMetadata;
import com.thinkbiganalytics.security.AccessController;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Monitors the default repository and any repositories configured in repositories.json.
 * Generates json metadata for any .zip templates, if not already present.
 * Monitors only for any file creations or deletions.
 */
@Component
public class RepositoryMonitor {

    @Inject
    ObjectMapper mapper;


    @Inject
    private RegisteredTemplateService registeredTemplateService;

    @Inject
    TemplateExporter templateExporter;

    @Inject
    Cache<String, Boolean> templateUpdateInfoCache;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private AccessController accessController;

    private static final Logger log = LoggerFactory.getLogger(RepositoryMonitor.class);

    private final WatchService watcher;
    private final WatchEvent.Kind[] events = {StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY};

    public RepositoryMonitor() {
        WatchService watcher = null;
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (Exception e) {
            log.error("Error in Template Repository watch service setup", e);
        }
        this.watcher = watcher;
    }

    public void watchRepositories(Set<Path> repositoriesToWatch) {


        Map<String, RegisteredTemplate> registeredTemplateMap = getAllRegisteredTemplatesAsMap();
        log.info("Started repository monitoring");
        try {
            repositoriesToWatch.forEach(path -> addMonitorToRepository(path, registeredTemplateMap));
            while (true) {
                WatchKey key = watcher.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> ev = cast(event);
                    if (ev.context().getFileName().toString().endsWith(".zip")) {
                        WatchEvent.Kind kind = event.kind();
                        Path repositoryPath = (Path) key.watchable();

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            createMetadata(repositoryPath.resolve(ev.context()), getAllRegisteredTemplatesAsMap());
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            Path json = getMetadataFileName(repositoryPath.resolve(ev.context()));
                            TemplateMetadata metadata = mapper.readValue(json.toFile(), TemplateMetadata.class);
                            templateUpdateInfoCache.invalidate(metadata.getTemplateName());
                            Files.deleteIfExists(json);
                            log.info("{}: {} and its metadata.", ev.kind(), ev.context());
                        }
                    }
                }

                if (!key.reset()) {
                    log.info("Exiting repository monitoring.");
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.warn("Terminating repository monitoring {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred monitoring repository changes", e);
        }
    }

    private Map<String, RegisteredTemplate> getAllRegisteredTemplatesAsMap() {
        return metadataAccess.read(() -> {
                this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_TEMPLATES);
                return registeredTemplateService.getRegisteredTemplates().stream().collect(Collectors.toMap(t -> t.getTemplateName(), t -> t));
            }, MetadataAccess.SERVICE);
    }

    private Path getMetadataFileName(Path templateFile) {
        return templateFile.getParent().resolve(FilenameUtils.getBaseName(templateFile.getFileName().toString()) + ".json");
    }

    private void createMetadata(Path templateFilePath, Map<String, RegisteredTemplate> registeredTemplateMap) {
        RegisteredTemplate tmplt = null;
        try {
            File templateZipFile = templateFilePath.toFile();
            byte[] content = ImportUtil.streamToByteArray(new FileInputStream(templateZipFile));
            InputStream inputStream = new ByteArrayInputStream(content);
            ImportTemplate importTemplate = ImportUtil.openZip(templateFilePath.getFileName().toString(), inputStream);
            tmplt = importTemplate.getTemplateToImport();

            File json = getMetadataFileName(templateFilePath).toFile();
            String checksum = DigestUtils.md5DigestAsHex(content);
            boolean updateAvailable = false;

            TemplateMetadata metadata = new TemplateMetadata(tmplt.getTemplateName(), tmplt.getDescription(),
                    templateFilePath.getFileName().toString(), checksum,
                    tmplt.isStream(), updateAvailable, tmplt.getUpdateDate().getTime());

            //startup or new template is published
            if (!json.exists()) {

                if (registeredTemplateMap.containsKey(tmplt.getTemplateName())) {
                    final String templateName = tmplt.getTemplateName();
                    ExportTemplate zipFile = metadataAccess.read(() -> {
                        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_TEMPLATES);
                        return templateExporter.exportTemplate(registeredTemplateMap.get(templateName).getId());
                    }, MetadataAccess.SERVICE);
                    String digest = DigestUtils.md5DigestAsHex(zipFile.getFile());
                    updateAvailable = !StringUtils.equals(checksum, digest);
                    metadata.setUpdateAvailable(updateAvailable);
                    templateUpdateInfoCache.put(templateName, updateAvailable);
                }
            }else {
                //if registered template was updated
                metadata = mapper.readValue(json, TemplateMetadata.class);
                templateUpdateInfoCache.put(tmplt.getTemplateName(), metadata.isUpdateAvailable());

                if (metadata.getLastModified() >= tmplt.getUpdateDate().getTime()) {
                    return;
                }

                if (StringUtils.equals(metadata.getChecksum(), checksum)) {
                    return;
                }

                metadata.setUpdateAvailable(true);
                templateUpdateInfoCache.put(tmplt.getTemplateName(), true);
            }
            log.info("Writing template metadata for {}.", tmplt.getTemplateName());
            mapper.writerWithDefaultPrettyPrinter().writeValue(json, metadata);
        } catch (Exception e) {
            log.error("Error occurred trying to generate template metadata.", e);
            if (tmplt != null) {
                templateUpdateInfoCache.invalidate(tmplt.getTemplateName());
            }
        }
    }

    private void generateMissingMetadata(Path repositoryPath, Map<String, RegisteredTemplate> registeredTemplateMap) throws Exception {
        Files.find(repositoryPath, 1, (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".zip"))
                .forEach(templateZip -> {
                    createMetadata(templateZip, registeredTemplateMap);
                });
    }

    private void addMonitorToRepository(Path repositoryPath, Map<String, RegisteredTemplate> registeredTemplateMap) {
        try {
            generateMissingMetadata(repositoryPath, registeredTemplateMap);
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
