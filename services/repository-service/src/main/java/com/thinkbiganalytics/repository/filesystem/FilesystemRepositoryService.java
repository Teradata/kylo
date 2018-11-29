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
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporter;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporterFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataChange;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
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
    Cache<String, Boolean> templateUpdateInfoCache;

    @Inject
    MetadataEventService eventService;

    @Inject
    RepositoryMonitor repositoryMonitor;

    @Inject
    ObjectMapper mapper;

    @Value("${kylo.template.repository.default:/opt/kylo/setup/data/templates/nifi-1.0}")
    String defaultKyloRepository;

    private final MetadataEventListener<TemplateChangeEvent> templateChangedListener = new TemplateChangeRepositoryListener();

    @PostConstruct
    public void addEventListener() throws Exception {
        log.info("Template change listener added in fileRepositoryService");
        eventService.addListener(templateChangedListener);

    }

    @Scheduled(fixedDelay = 5000)
    public void monitorRepositories() throws Exception {
        Set<Path> repositoriesToWatch = listRepositories()
            .stream()
            .map(repo -> Paths.get(repo.getLocation()))
            .collect(Collectors.toSet());
        repositoryMonitor.watchRepositories(repositoriesToWatch);
        log.info("End of scheduler");
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
                              1,
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
        long updateTime = importTemplate.getTemplateToImport().getUpdateDate().getTime();
        templateMetadata.setLastModified(updateTime);
        templateMetadata.setUpdateAvailable(false);
        mapper.writer(new DefaultPrettyPrinter()).writeValue(metadataPath.toFile(), templateMetadata);
        log.info("Generated checksum for {} - {}", templateMetadata.getTemplateName(), checksum);

        templateUpdateInfoCache.put(templateMetadata.getTemplateName(), false);
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
        for (TemplateMetadataWrapper item : listTemplatesByRepository(repository)) {
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

        Path templatePath = Paths.get(repository.getLocation() + "/" + zipFile.getFileName());
        TemplateMetadata metadata = new TemplateMetadata(zipFile.getTemplateName(), zipFile.getDescription(),
                                                         zipFile.getFileName().toString(), digest,
                                                         zipFile.isStream(), false, templatePath.toFile().lastModified());

        //create repositoryItem
        TemplateMetadataWrapper
            templateMetadata =
            foundMetadataOpt.isPresent()
            ? foundMetadataOpt.get()
            : new TemplateMetadataWrapper(metadata);
        String baseName = FilenameUtils.getBaseName(templateMetadata.getFileName());
        //write file in first of templateLocations

        Files.write(templatePath, zipFile.getFile());
        log.info("Finished publishing template {} to repository {}.", templateMetadata.getTemplateName(), repository.getName());

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
            log.error("Error reading template repositories", e);
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

    @Override
    public List<TemplateMetadataWrapper> listTemplatesByRepository(String repositoryType, String repositoryName) throws Exception {
        TemplateRepository repo = getRepositoryByNameAndType(repositoryName, repositoryType);

        return listTemplatesByRepository(repo);
    }

    private List<TemplateMetadataWrapper> listTemplatesByRepository(TemplateRepository repository) throws Exception {
        List<TemplateMetadataWrapper> repositoryItems = new ArrayList<>();

        Set<String> registeredTemplates = registeredTemplateService
            .getRegisteredTemplates()
            .stream().map(t -> t.getTemplateName())
            .collect(Collectors.toSet());

        getMetadataFilesInRepository(repository).stream()
            .map((p) -> jsonToMetadata(p, repository, Optional.of(registeredTemplates)))
            .forEach(repositoryItems::add);

        return repositoryItems;
    }

    private List<Path> getMetadataFilesInRepository(TemplateRepository repository) {
        try {
            return Files.find(Paths.get(repository.getLocation()),
                              1,
                              (path, attrs) -> attrs.isRegularFile()
                                               && path.toString().endsWith(".json")).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error reading metadata from repository", e);
        }

        return Collections.<Path>emptyList();
    }

    private TemplateMetadataWrapper jsonToMetadata(Path path, TemplateRepository repository, Optional<Set<String>> registeredTemplates) {
        try {
            TemplateMetadata templateMetaData = mapper.readValue(path.toFile(), TemplateMetadata.class);
            TemplateMetadataWrapper wrapper = new TemplateMetadataWrapper(templateMetaData);

            Path templatePath = Paths.get(repository.getLocation() + "/" + templateMetaData.getFileName());

            if (registeredTemplates.isPresent() && registeredTemplates.get().contains(templateMetaData.getTemplateName())) {

                byte[] content = ImportUtil.streamToByteArray(new FileInputStream(templatePath.toFile()));
                InputStream inputStream = new ByteArrayInputStream(content);
                ImportTemplate importTemplate = ImportUtil.openZip(templatePath.getFileName().toString(), inputStream);
                RegisteredTemplate template = importTemplate.getTemplateToImport();

                templateUpdateInfoCache.get(templateMetaData.getTemplateName(), () -> templateMetaData.isUpdateAvailable());
                //init checksum if not already set
                //set updated flag if file is modified.
                if (templateMetaData.getLastModified() < template.getUpdateDate().getTime()) {
                    String checksum = DigestUtils.md5DigestAsHex(content);

                    //capturing checksum first time or it has actually changed?
                    boolean templateModified = !StringUtils.equals(templateMetaData.getChecksum(), checksum);

                    templateMetaData.setUpdateAvailable(templateModified);
                    templateUpdateInfoCache.put(templateMetaData.getTemplateName(), templateModified);

                    mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), templateMetaData);
                    wrapper = new TemplateMetadataWrapper(templateMetaData);
                }
                if(wrapper.isUpdateAvailable()){
                    wrapper.getUpdates().addAll(template.getChangeComments());
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
            if(change.getChange() == MetadataChange.ChangeType.DELETE){
                templateUpdateInfoCache.invalidate(change.getDescription());
                return false;
            }

            TemplateMetadata metadata = null;
            try {
                metadata = mapper.readValue(path.toFile(), TemplateMetadata.class);
                Principal[] principals = new Principal[1];
                principals[0] = MetadataAccess.SERVICE;

                RegisteredTemplate foundTemplate = registeredTemplateService
                        .findRegisteredTemplateByName(change.getDescription(), TemplateModelTransform.TEMPLATE_TRANSFORMATION_TYPE.WITH_FEED_NAMES, principals);

                if (StringUtils.equals(metadata.getTemplateName(), foundTemplate.getTemplateName())) {
                    metadata.setLastModified(foundTemplate.getUpdateDate().getTime());
                    metadata.setUpdateAvailable(false);
                    mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), metadata);
                    templateUpdateInfoCache.put(change.getDescription(), false);
                    return true;
                }
            } catch (Exception e) {
                log.info("Error when reading/writing template metadata", e);
            }
            return false;
        }
    }

}
