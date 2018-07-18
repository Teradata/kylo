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

import com.fasterxml.jackson.core.JsonParser;
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
import com.thinkbiganalytics.repository.api.RepositoryItem;
import com.thinkbiganalytics.repository.api.RepositoryService;
import com.thinkbiganalytics.repository.api.TemplateRepository;
import com.thinkbiganalytics.repository.api.TemplateSearchFilter;
import com.thinkbiganalytics.repository.api.TemplateSearchFilter.TemplateComparator;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import static com.thinkbiganalytics.repository.api.TemplateSearchFilter.TemplateComparator.NAME;
import static com.thinkbiganalytics.repository.api.TemplateSearchFilter.TemplateComparator.valueOf;

@Service
@PropertySource("classpath:filesystem-repository.properties")
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

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<RepositoryItem> listTemplates() {
        List<RepositoryItem> repositoryItems = new ArrayList<>();
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

    private Stream<? extends RepositoryItem> fileToTemplateConversion(Set<String> registeredTemplates, TemplateRepository r) {
        try {
            return Files.find(Paths.get(r.getLocation()),
                              Integer.MAX_VALUE,
                              (path, attrs) -> attrs.isRegularFile()
                                               && path.toString().endsWith(".json"))
                .map((p) -> {
                    RepositoryItem template = jsonToMetadata(p, r);
                    template.setInstalled(registeredTemplates.contains(template.getTemplateName()));
                    return template;
                });
        } catch (Exception e) {
            log.error("Error reading repository metadata for templates", e);
        }
        return Collections.<RepositoryItem>emptyList().stream();
    }

    @Override
    public ImportTemplate importTemplate(String repositoryName,
                                         String repositoryType,
                                         String fileName,
                                         String uploadKey,
                                         String importComponents) throws Exception {

        Optional<Path> opt = listRepositories()
            .stream()
            .filter(r -> StringUtils.equals(r.getName(), repositoryName) &&
                         StringUtils.equals(r.getType().getKey(), repositoryType))
            .map(r -> Paths.get(r.getLocation() + "/" + fileName))
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
    public RepositoryItem publishTemplate(String repositoryName, String repositoryType, String templateId, boolean overwrite) throws Exception {

        //export template
        ExportTemplate zipFile = templateExporter.exportTemplate(templateId);

        //get repository
        TemplateRepository repository = getRepositoryByNameAndType(repositoryName, repositoryType);

        //Check if template already exists
        Optional<RepositoryItem> foundMetadataOpt = Optional.empty();
        for (RepositoryItem item : getAllTemplatesInRepository(repository)) {
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

        //create repositoryItem
        RepositoryItem
            repositoryItem =
            foundMetadataOpt.isPresent()
            ? foundMetadataOpt.get()
            : new RepositoryItem(zipFile.getTemplateName(), zipFile.getDescription(), zipFile.getFileName(), zipFile.isStream());
        String baseName = FilenameUtils.getBaseName(repositoryItem.getFileName());
        log.info("Writing metadata for {} template.", repositoryItem.getTemplateName());
        mapper.writeValue(Paths.get(repository.getLocation() + "/" + baseName + ".json").toFile(), repositoryItem);

        //write file in first of templateLocations
        log.info("Now publishing template {} to repository {}.", repositoryItem.getTemplateName(), repository.getName());
        Files.write(Paths.get(repository.getLocation() + "/" + repositoryItem.getFileName()), zipFile.getFile());
        log.info("Finished publishing template {} to repository {}.", repositoryItem.getTemplateName(), repository.getName());

        return repositoryItem;
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
        TypeReference<List<TemplateRepository>> typeReference = new TypeReference<List<TemplateRepository>>() {};
        try {
            InputStream is = resourceLoader.getResource("classpath:repositories.json").getInputStream();
            List<TemplateRepository> repositories = mapper.readValue(is, typeReference);
            Set<String> set = new HashSet<>(repositories.size());

            return repositories
                .stream()
                .filter(r -> StringUtils.isNotBlank(r.getLocation()) && //location must be provided
                             Files.exists(Paths.get(r.getLocation().trim())) && //location must be valid
                             set.add(r.getName().trim().toLowerCase()+"_"+r.getType().getKey().trim().toLowerCase())) //unique name per repository type
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Unable to read repositories", e);
        }

        return new ArrayList<>();
    }

    @Override
    public SearchResult getTemplatesPage(TemplateSearchFilter filter) {
        SearchResult<RepositoryItem> searchResult = new SearchResultImpl<>();
        List<RepositoryItem> templates = listTemplates();

        String sort = filter.getSort();

        List<RepositoryItem> data = templates.stream()
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

    private Comparator<RepositoryItem> getComparator(String sort) {
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

    private List<RepositoryItem> getAllTemplatesInRepository(TemplateRepository repository) throws IOException {
        List<RepositoryItem> repositoryItems = new ArrayList<>();
        Files.find(Paths.get(repository.getLocation()),
                   Integer.MAX_VALUE,
                   (path, attrs) -> attrs.isRegularFile()
                                    && path.toString().endsWith(".json"))
            .map((p) -> jsonToMetadata(p, repository))
            .forEach(repositoryItems::add);

        return repositoryItems;
    }

    private RepositoryItem jsonToMetadata(Path path, TemplateRepository repository) {
        try {
            String s = new String(Files.readAllBytes(path));
            RepositoryItem item = mapper.readValue(s, RepositoryItem.class);
            item.setRepository(repository);
            return item;
        } catch (IOException e) {
            log.error("Error reading metadata from {}", e);
        }
        return null;
    }
}
