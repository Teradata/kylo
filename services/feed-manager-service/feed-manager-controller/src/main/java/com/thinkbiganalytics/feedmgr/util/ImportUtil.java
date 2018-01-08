package com.thinkbiganalytics.feedmgr.util;

/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportType;
import com.thinkbiganalytics.feedmgr.rest.model.FeedDataTransformation;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportProperty;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.ImportFeed;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.support.ZipFileUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;

public class ImportUtil {


    public static Set<ImportComponentOption> inspectZipComponents(byte[] content, ImportType importType) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(content);
        return inspectZipComponents(inputStream, importType);
    }


    public static Set<ImportComponentOption> inspectZipComponents(InputStream inputStream, ImportType importType) throws IOException {
        Set<ImportComponentOption> options = new HashSet<>();
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().startsWith(ImportTemplate.NIFI_TEMPLATE_XML_FILE)) {
                options.add(new ImportComponentOption(ImportComponent.NIFI_TEMPLATE, importType.equals(ImportType.TEMPLATE) ? true : false));
            } else if (entry.getName().startsWith(ImportTemplate.TEMPLATE_JSON_FILE)) {
                options.add(new ImportComponentOption(ImportComponent.TEMPLATE_DATA, importType.equals(ImportType.TEMPLATE) ? true : false));
            } else if (entry.getName().startsWith(ImportTemplate.NIFI_CONNECTING_REUSABLE_TEMPLATE_XML_FILE)) {
                options.add(new ImportComponentOption(ImportComponent.REUSABLE_TEMPLATE, false));
            }
            else if(entry.getName().startsWith(ImportTemplate.REUSABLE_TEMPLATE_OUTPUT_CONNECTION_FILE)){
                options.add(new ImportComponentOption(ImportComponent.TEMPLATE_CONNECTION_INFORMATION,true));
            } else if (importType.equals(ImportType.FEED) && entry.getName().startsWith(ImportFeed.FEED_JSON_FILE)) {
                options.add(new ImportComponentOption(ImportComponent.FEED_DATA, true));
                options.add(new ImportComponentOption(ImportComponent.USER_DATASOURCES, true));
            }
        }
        zis.closeEntry();
        zis.close();

        return options;
    }


    public static void addToImportOptionsSensitiveProperties(ImportOptions importOptions, List<NifiProperty> sensitiveProperties, ImportComponent component) {
        ImportComponentOption option = importOptions.findImportComponentOption(component);
        if (option.getProperties().isEmpty()) {
            option.setProperties(sensitiveProperties.stream().map(p -> new ImportProperty(p.getProcessorName(), p.getProcessorId(), p.getKey(), "", p.getProcessorType())).collect(
                Collectors.toList()));
        } else {
            //only add in those that are unique
            Map<String, ImportProperty> propertyMap = option.getProperties().stream().collect(Collectors.toMap(p -> p.getProcessorNameTypeKey(), p -> p));
            sensitiveProperties.stream().filter(nifiProperty -> !propertyMap.containsKey(nifiProperty.getProcessorNameTypeKey())).forEach(p -> {
                option.getProperties().add(new ImportProperty(p.getProcessorName(), p.getProcessorId(), p.getKey(), "", p.getProcessorType()));
            });
        }
    }


    public static boolean applyImportPropertiesToTemplate(RegisteredTemplate template, ImportTemplate importTemplate, ImportComponent component) {
        ImportComponentOption option = importTemplate.getImportOptions().findImportComponentOption(component);

        if (!option.getProperties().isEmpty() && option.getProperties().stream().anyMatch(importProperty -> StringUtils.isBlank(importProperty.getPropertyValue()))) {
            importTemplate.setSuccess(false);
            importTemplate.setTemplateResults(new NifiProcessGroup());
            String msg = "Unable to import Template. Additional properties to be supplied before importing.";
            importTemplate.getTemplateResults().addError(NifiError.SEVERITY.WARN, msg, "");
            option.getErrorMessages().add(msg);
            return false;
        } else {
            template.getSensitiveProperties().forEach(nifiProperty -> {
                ImportProperty
                    userSuppliedValue =
                    option.getProperties().stream().filter(importFeedProperty -> nifiProperty.getProcessorId().equalsIgnoreCase(importFeedProperty.getProcessorId()) && nifiProperty.getKey()
                        .equalsIgnoreCase(importFeedProperty.getPropertyKey())).findFirst().orElse(null);
                //deal with nulls?
                if(userSuppliedValue == null) {
                    //attempt to find it via the name
                    userSuppliedValue =
                        option.getProperties().stream().filter(importFeedProperty -> nifiProperty.getProcessorName().equalsIgnoreCase(importFeedProperty.getProcessorName()) && nifiProperty.getKey()
                            .equalsIgnoreCase(importFeedProperty.getPropertyKey())).findFirst().orElse(null);
                }
                if(userSuppliedValue != null) {
                    nifiProperty.setValue(userSuppliedValue.getPropertyValue());
                }
            });
            return true;
        }
    }

    public static boolean applyImportPropertiesToFeed(FeedMetadata metadata, ImportFeed importFeed, ImportComponent component) {
        ImportComponentOption option = importFeed.getImportOptions().findImportComponentOption(component);

        if (!option.getProperties().isEmpty() && option.getProperties().stream().anyMatch(importProperty -> StringUtils.isBlank(importProperty.getPropertyValue()))) {
            importFeed.setSuccess(false);
            if (importFeed.getTemplate() == null) {
                ImportTemplate importTemplate = new ImportTemplate(importFeed.getFileName());
                importFeed.setTemplate(importTemplate);
            }
            String feedCategory = importFeed.getImportOptions().getCategorySystemName() != null ? importFeed.getImportOptions().getCategorySystemName() : metadata.getSystemCategoryName();
            String msg = "The feed " + FeedNameUtil.fullName(feedCategory, metadata.getSystemFeedName())
                         + " needs additional properties to be supplied before importing.";
            importFeed.addErrorMessage(metadata, msg);
            option.getErrorMessages().add(msg);
            return false;
        } else {
            metadata.getSensitiveProperties().forEach(nifiProperty -> {
                ImportProperty userSuppliedValue = importFeed.getImportOptions().getProperties(ImportComponent.FEED_DATA).stream().filter(importFeedProperty -> {
                    return nifiProperty.getProcessorId().equalsIgnoreCase(importFeedProperty.getProcessorId()) && nifiProperty.getKey().equalsIgnoreCase(importFeedProperty.getPropertyKey());
                }).findFirst().orElse(null);
                //deal with nulls?
                if (userSuppliedValue != null) {
                    nifiProperty.setValue(userSuppliedValue.getPropertyValue());
                }
            });
            return true;
        }
    }

    /**
     * Replaces the specified data source id with a new data source id.
     *
     * @param metadata the feed metadata
     * @param oldDatasourceId the id of the data source to be replaced
     * @param newDatasourceId the id of the new data source
     */
    @SuppressWarnings("unchecked")
    public static void replaceDatasource(@Nonnull final FeedMetadata metadata, @Nonnull final String oldDatasourceId, @Nonnull final String newDatasourceId) {
        // Update data transformation
        final FeedDataTransformation transformation = metadata.getDataTransformation();
        if (transformation != null) {
            // Update chart view model
            Optional.of(transformation.getChartViewModel())
                .map(model -> (List<Map<String, Object>>) model.get("nodes"))
                .ifPresent(
                    nodes -> nodes.forEach(
                        node -> {
                            final String nodeDatasourceId = (String) node.get("datasourceId");
                            if (nodeDatasourceId != null && oldDatasourceId.equals(nodeDatasourceId)) {
                                node.put("datasourceId", newDatasourceId);
                            }
                        }
                    )
                );

            // Update data source id list
            transformation.getDatasourceIds().replaceAll(id -> oldDatasourceId.equals(id) ? newDatasourceId : id);

            // Update transform script
            final String updatedDataTransformScript = transformation.getDataTransformScript().replace(oldDatasourceId, newDatasourceId);
            transformation.setDataTransformScript(updatedDataTransformScript);
        }

        // Update processor properties
        metadata.getProperties().forEach(property -> {
            final String value = property.getValue();
            if (value != null && !value.isEmpty()) {
                property.setValue(value.replace(oldDatasourceId, newDatasourceId));
            }
        });
    }

    public static byte[] streamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = inputStream.read(buf)) >= 0) {
            baos.write(buf, 0, n);
        }
        byte[] content = baos.toByteArray();
        return content;
    }

    public static ImportTemplate getNewNiFiTemplateImport(String fileName, InputStream inputStream) throws IOException {
        ImportTemplate template = new ImportTemplate(fileName);
        template.setValid(true);
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        String xmlTemplate = writer.toString();
        template.setNifiTemplateXml(xmlTemplate);
        return template;
    }

    public static ImportTemplate getNewNiFiTemplateImport(String fileName, byte[] xmlFile) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(xmlFile);
        return getNewNiFiTemplateImport(fileName,inputStream);
    }

    /**
     * Open the zip file and populate the {@link ImportTemplate} object with the components in the file/archive
     *
     * @param fileName    the file name
     * @param inputStream the file
     * @return the template data to import
     */
    public static ImportTemplate openZip(String fileName, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        ImportTemplate importTemplate = new ImportTemplate(fileName);
        while ((zipEntry = zis.getNextEntry()) != null) {
            String zipEntryContents = ZipFileUtil.zipEntryToString(buffer, zis, zipEntry);
            if (zipEntry.getName().startsWith(ImportTemplate.NIFI_TEMPLATE_XML_FILE)) {
                importTemplate.setNifiTemplateXml(zipEntryContents);
            } else if (zipEntry.getName().startsWith(ImportTemplate.TEMPLATE_JSON_FILE)) {
                importTemplate.setTemplateJson(zipEntryContents);
            } else if (zipEntry.getName().startsWith(ImportTemplate.NIFI_CONNECTING_REUSABLE_TEMPLATE_XML_FILE)) {
                importTemplate.addNifiConnectingReusableTemplateXml(zipEntryContents);
            }
            else if (zipEntry.getName().startsWith(ImportTemplate.REUSABLE_TEMPLATE_OUTPUT_CONNECTION_FILE)) {
                String json = zipEntryContents;
                List<ReusableTemplateConnectionInfo> connectionInfos = ObjectMapperSerializer.deserialize(json,new TypeReference<List<ReusableTemplateConnectionInfo>>(){});
                importTemplate.addReusableTemplateConnectionInformation(connectionInfos);
            }
        }
        zis.closeEntry();
        zis.close();
        if (!importTemplate.hasValidComponents()) {
            throw new UnsupportedOperationException(
                " The file you uploaded is not a valid archive.  Please ensure the Zip file has been exported from the system and has 2 valid files named: " + ImportTemplate.NIFI_TEMPLATE_XML_FILE + ", and "
                + ImportTemplate.TEMPLATE_JSON_FILE);
        }
        importTemplate.setZipFile(true);
        return importTemplate;

    }



}
