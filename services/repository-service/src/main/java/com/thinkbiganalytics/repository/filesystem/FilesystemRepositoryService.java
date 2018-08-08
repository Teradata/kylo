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
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
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
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChange;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChangeEvent;
import com.thinkbiganalytics.metadata.api.template.export.ExportTemplate;
import com.thinkbiganalytics.metadata.api.template.export.TemplateExporter;
import com.thinkbiganalytics.repository.api.RepositoryService;
import com.thinkbiganalytics.repository.api.TemplateMetadata;
import com.thinkbiganalytics.repository.api.TemplateMetadataWrapper;
import com.thinkbiganalytics.repository.api.TemplateRepository;
import com.thinkbiganalytics.repository.api.TemplateSearchFilter;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.thinkbiganalytics.repository.api.TemplateRepository.RepositoryType.FILESYSTEM;
import static com.thinkbiganalytics.repository.api.TemplateSearchFilter.TemplateComparator.NAME;
import static com.thinkbiganalytics.repository.api.TemplateSearchFilter.TemplateComparator.valueOf;

@Service
@PropertySource(value = "classpath:application.properties")
public class FilesystemRepositoryService implements RepositoryService {

    private static final Logger log = LoggerFactory.getLogger(FilesystemRepositoryService.class);

    @Inject
    TemplateImporterFactory templateImporterFactory;

    @Inject
    UploadProgressService uploadProgressService;

    @Inject
    private RegisteredTemplateService registeredTemplateService;

    @Inject
    TemplateExporter templateExporter;

    @Autowired
    ResourceLoader resourceLoader;

    @Inject
    Cache<String, Long> templateUpdateInfoCache;

    @Inject
    MetadataEventService eventService;

    ObjectMapper mapper = new ObjectMapper();

    @Value("${kylo.default.template.repository}")
    String defaultKyloRepository;

    private final MetadataEventListener<TemplateChangeEvent> templateChangedListener = new TemplateChangeRepositoryListener();

    @PostConstruct
    public void addEventListener() {
        log.info("Event listener in fileRepositoryService");
        eventService.addListener(templateChangedListener);
    }

    @Override
    public List<TemplateMetadataWrapper> listTemplates() {
        List<TemplateMetadataWrapper> repositoryItems = new ArrayList<>();
        List<TemplateRepository> repositories = listRepositories();

        Set<String> registeredTemplates = registeredTemplateService
            .getRegisteredTemplates()
            .stream().map(t -> t.getTemplateName())
            .collect(Collectors.toSet());

        repositories
            .stream()
            .filter(r -> StringUtils.isNotBlank(r.getLocation()))
            .flatMap(r -> {
                return fileToTemplateConversion(registeredTemplates, r);
            }).forEach(repositoryItems::add);

        return repositoryItems;
    }

    private Stream<? extends TemplateMetadataWrapper> fileToTemplateConversion(Set<String> registeredTemplates, TemplateRepository r) {
        try {
            return Files.find(Paths.get(r.getLocation()),
                              Integer.MAX_VALUE,
                              (path, attrs) -> attrs.isRegularFile()
                                               && path.toString().endsWith(".json"))
                .map(p -> jsonToMetadata(p, r, Optional.of(registeredTemplates)));
        } catch (Exception e) {
            log.error("Error reading repository metadata for templates", e);
        }
        return Collections.<TemplateMetadataWrapper>emptyList().stream();
    }

    @Override
    public ImportTemplate importTemplate(String repositoryName,
                                         String repositoryType,
                                         String fileName,
                                         String uploadKey,
                                         String importComponents) throws Exception {

        Optional<TemplateRepository> repository = listRepositories()
            .stream()
            .filter(r -> StringUtils.equals(r.getName(), repositoryName) &&
                         StringUtils.equals(r.getType().getKey(), repositoryType))
            .findFirst();

        Path filePath = Paths.get(repository.get().getLocation() + "/" + fileName);

        if (!filePath.toFile().exists()) {
            throw new RuntimeException("Unable to find template file to import: " + fileName);
        }

        log.info("Begin template import {}", fileName);
        ImportTemplateOptions options = new ImportTemplateOptions();
        options.setUploadKey(uploadKey);

        byte[] content = ImportUtil.streamToByteArray(new FileInputStream(filePath.toFile()));
        String checksum = DigestUtils.md5DigestAsHex(content);
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

        //update template metadata
        String baseName = FilenameUtils.getBaseName(fileName);
        Path metadataPath = Paths.get(repository.get().getLocation() + "/" + baseName + ".json");
        TemplateMetadata templateMetadata = mapper.readValue(metadataPath.toFile(), TemplateMetadata.class);
        templateMetadata.setChecksum(checksum);
        templateMetadata.setLastModified(filePath.toFile().lastModified());
        templateMetadata.setUpdateAvailable(false);
        mapper.writer(new DefaultPrettyPrinter()).writeValue(metadataPath.toFile(), templateMetadata);
        log.info("Generated checksum for {} - {}", templateMetadata.getTemplateName(), checksum);

        templateUpdateInfoCache.put(templateMetadata.getTemplateName(), filePath.toFile().lastModified());
        return importTemplate;
    }

    @Override
    public TemplateMetadataWrapper publishTemplate(String repositoryName, String repositoryType, String templateId, boolean overwrite) throws Exception {

        //export template
        ExportTemplate zipFile = templateExporter.exportTemplate(templateId);

        //get repository
        TemplateRepository repository = getRepositoryByNameAndType(repositoryName, repositoryType);

        //Check if template already exists
        Optional<TemplateMetadataWrapper> foundMetadataOpt = Optional.empty();
        for (TemplateMetadataWrapper item : getAllTemplatesInRepository(repository)) {
            if (item.getTemplateName().equals(zipFile.getTemplateName())) {
                foundMetadataOpt = Optional.of(item);
                break;
            }
        }
        if (foundMetadataOpt.isPresent()) {
            if (!overwrite) {
                throw new UnsupportedOperationException("Template with same name already exists.");
            }
            log.info("Overwriting template with same name.");
        }

        String digest = DigestUtils.md5DigestAsHex(zipFile.getFile());

        TemplateMetadata metadata = new TemplateMetadata(zipFile.getTemplateName(), zipFile.getDescription(), zipFile.getFileName(), zipFile.isStream());

        //create repositoryItem
        TemplateMetadataWrapper
            templateMetadata =
            foundMetadataOpt.isPresent()
            ? foundMetadataOpt.get()
            : new TemplateMetadataWrapper(metadata);
        String baseName = FilenameUtils.getBaseName(templateMetadata.getFileName());
        //write file in first of templateLocations
        log.info("Now publishing template {} to repository {}.", templateMetadata.getTemplateName(), repository.getName());

        Path templatePath = Paths.get(repository.getLocation() + "/" + templateMetadata.getFileName());
        Files.write(templatePath, zipFile.getFile());
        log.info("Finished publishing template {} to repository {}.", templateMetadata.getTemplateName(), repository.getName());
        metadata.setChecksum(digest);
        metadata.setLastModified(templatePath.toFile().lastModified());
        metadata.setUpdateAvailable(false);

        log.info("Writing metadata for {} template.", templateMetadata.getTemplateName());
        File newFile = Paths.get(repository.getLocation() + "/" + baseName + ".json").toFile();
        mapper.writeValue(Paths.get(repository.getLocation() + "/" + baseName + ".json").toFile(), templateMetadata);
        log.info("Generated checksum for {} - {}", templateMetadata.getTemplateName(), digest);

        templateUpdateInfoCache.put(templateMetadata.getTemplateName(), newFile.lastModified());
        return new TemplateMetadataWrapper(metadata);
    }

    @Override
    public byte[] downloadTemplate(String repositoryName, String repositoryType, String fileName) throws Exception {
        //get repository
        TemplateRepository repository = getRepositoryByNameAndType(repositoryName, repositoryType);
        Path path = Paths.get(repository.getLocation() + "/" + fileName);

        if (!Files.exists(Paths.get(repository.getLocation() + "/" + fileName))) {
            throw new RuntimeException("File not found for download: " + fileName);
        }
        log.info("Begin template download {}", fileName);

        return ImportUtil.streamToByteArray(new FileInputStream(path.toFile()));
    }

    @Override
    public List<TemplateRepository> listRepositories() {
        TypeReference<List<TemplateRepository>> typeReference = new TypeReference<List<TemplateRepository>>() {
        };
        try {
            InputStream is = resourceLoader.getResource("classpath:repositories.json").getInputStream();
            List<TemplateRepository> repositories = new ArrayList<>();
            repositories.add(new TemplateRepository("Kylo Repository", defaultKyloRepository, "", FILESYSTEM, true));
            repositories.addAll(mapper.readValue(is, typeReference));
            Set<String> set = new HashSet<>(repositories.size());

            return repositories
                .stream()
                .filter(r -> StringUtils.isNotBlank(r.getLocation()) && //location must be provided
                             Files.exists(Paths.get(r.getLocation().trim())) && //location must be valid
                             set.add(r.getName().trim().toLowerCase() + "_" + r.getType().getKey().trim().toLowerCase())) //unique name per repository type
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Unable to read repositories", e);
        }

        return new ArrayList<>();
    }

    @Override
    public SearchResult getTemplatesPage(TemplateSearchFilter filter) {
        SearchResult<TemplateMetadataWrapper> searchResult = new SearchResultImpl<>();
        List<TemplateMetadataWrapper> templates = listTemplates();

        String sort = filter.getSort();

        List<TemplateMetadataWrapper> data = templates.stream()
            .sorted(getComparator(sort))
            .skip(filter.getStart())
            .limit(filter.getLimit() > 0 ? filter.getLimit() : Integer.MAX_VALUE)
            .collect(Collectors.toList());

        Long total = new Long(templates.size());
        searchResult.setData(data);
        searchResult.setRecordsTotal(total);
        searchResult.setRecordsFiltered(total);
        return searchResult;
    }

    private Comparator<TemplateMetadataWrapper> getComparator(String sort) {
        return StringUtils.isNotBlank(sort) ?
               (sort.startsWith("-") ? valueOf(sort.substring(1)).getComparator().reversed() : valueOf(sort).getComparator())
                                            : NAME.getComparator();
    }

    private TemplateRepository getRepositoryByNameAndType(String repositoryName, String repositoryType) {
        return listRepositories()
            .stream()
            .filter(r -> StringUtils.equals(r.getName(), repositoryName) && StringUtils.equals(r.getType().getKey(), repositoryType))
            .findFirst().get();
    }

    private List<TemplateMetadataWrapper> getAllTemplatesInRepository(TemplateRepository repository) throws IOException {
        List<TemplateMetadataWrapper> repositoryItems = new ArrayList<>();
        getMetadataFilesInRepository(repository).stream()
            .map((p) -> jsonToMetadata(p, repository, Optional.empty()))
            .forEach(repositoryItems::add);

        return repositoryItems;
    }

    private List<Path> getMetadataFilesInRepository(TemplateRepository repository) {
        try {
            return Files.find(Paths.get(repository.getLocation()),
                              Integer.MAX_VALUE,
                              (path, attrs) -> attrs.isRegularFile()
                                               && path.toString().endsWith(".json")).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error reading metadata from repository", e);
        }

        return Collections.<Path>emptyList();
    }

    private TemplateMetadataWrapper jsonToMetadata(Path path, TemplateRepository repository, Optional<Set<String>> registeredTemplates) {
        try {
            TemplateMetadata tmpltMetaData = mapper.readValue(path.toFile(), TemplateMetadata.class);
            TemplateMetadataWrapper wrapper = new TemplateMetadataWrapper(tmpltMetaData);

            Path templatePath = Paths.get(repository.getLocation() + "/" + tmpltMetaData.getFileName());
            long latest = templatePath.toFile().lastModified();

            if (registeredTemplates.isPresent() && registeredTemplates.get().contains(tmpltMetaData.getTemplateName())) {
                long lastModified = templateUpdateInfoCache.get(tmpltMetaData.getTemplateName(), () -> tmpltMetaData.getLastModified());
                //init checksum if not already set
                //set updated flag if file is modified.
                if (StringUtils.isBlank(tmpltMetaData.getChecksum()) || lastModified < latest) {
                    String checksum = DigestUtils.md5DigestAsHex(Files.readAllBytes(templatePath));

                    //capturing checksum first time or it has actually changed?
                    boolean templateModified = StringUtils.isNotBlank(tmpltMetaData.getChecksum()) && !StringUtils.equals(tmpltMetaData.getChecksum(), checksum);
                    //update cache if needed
                    if (templateModified) {
                        templateUpdateInfoCache.put(tmpltMetaData.getTemplateName(), latest);
                    }

                    if (StringUtils.isBlank(tmpltMetaData.getChecksum())) {
                        tmpltMetaData.setChecksum(checksum);
                    }
                    tmpltMetaData.setUpdateAvailable(templateModified);

                    mapper.writer(new DefaultPrettyPrinter()).writeValue(path.toFile(), tmpltMetaData);
                    wrapper = new TemplateMetadataWrapper(tmpltMetaData);
                }
                wrapper.setInstalled(true);
            }

            wrapper.setRepository(repository);
            return wrapper;
        } catch (Exception e) {
            log.error("Error reading metadata", e);
        }
        return null;
    }

    private class TemplateChangeRepositoryListener implements MetadataEventListener<TemplateChangeEvent> {

        @Override
        public void notify(TemplateChangeEvent event) {
            TemplateChange change = event.getData();
            if (templateUpdateInfoCache.getIfPresent(change.getDescription()) != null) {
                listRepositories().stream()
                    .flatMap(repo -> {
                        return getMetadataFilesInRepository(repo).stream();
                    })
                    .filter(path -> {
                        return updateLastModifiedDate(event, change, path);
                    })
                    .findFirst();
            }
        }

        private boolean updateLastModifiedDate(TemplateChangeEvent event, TemplateChange change, Path path) {
            TemplateMetadata metadata = null;
            try {
                metadata = mapper.readValue(path.toFile(), TemplateMetadata.class);

                RegisteredTemplate foundTemplate = registeredTemplateService.findRegisteredTemplateByName(change.getDescription());

                if (StringUtils.equals(metadata.getTemplateName(), foundTemplate.getTemplateName())) {
                    long millis = event.getTimestamp().getMillis();
                    metadata.setLastModified(foundTemplate.getUpdateDate().getTime());
                    metadata.setUpdateAvailable(false);
                    mapper.writer(new DefaultPrettyPrinter()).writeValue(path.toFile(), metadata);
                    templateUpdateInfoCache.put(change.getDescription(), millis);
                    return true;
                }
            } catch (Exception e) {
                log.info("Error when reading/writing template metadata", e);
            }
            return false;
        }
    }

}
