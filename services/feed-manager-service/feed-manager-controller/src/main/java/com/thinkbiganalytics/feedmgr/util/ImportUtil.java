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

import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportType;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportProperty;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.feed.ExportImportFeedService;
import com.thinkbiganalytics.feedmgr.service.template.ExportImportTemplateService;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            if (entry.getName().startsWith(ExportImportTemplateService.NIFI_TEMPLATE_XML_FILE)) {
                options.add(new ImportComponentOption(ImportComponent.NIFI_TEMPLATE, importType.equals(ImportType.TEMPLATE) ? true : false));
            } else if (entry.getName().startsWith(ExportImportTemplateService.TEMPLATE_JSON_FILE)) {
                options.add(new ImportComponentOption(ImportComponent.TEMPLATE_DATA, importType.equals(ImportType.TEMPLATE) ? true : false));
            } else if (entry.getName().startsWith(ExportImportTemplateService.NIFI_CONNECTING_REUSABLE_TEMPLATE_XML_FILE)) {
                options.add(new ImportComponentOption(ImportComponent.REUSABLE_TEMPLATE, false));

            } else if (importType.equals(ImportType.FEED) && entry.getName().startsWith(ExportImportFeedService.FEED_JSON_FILE)) {
                options.add(new ImportComponentOption(ImportComponent.FEED_DATA, true));
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


    public static boolean applyImportPropertiesToTemplate(RegisteredTemplate template, ExportImportTemplateService.ImportTemplate importTemplate, ImportComponent component) {
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
                nifiProperty.setValue(userSuppliedValue.getPropertyValue());
            });
            return true;
        }
    }

    public static boolean applyImportPropertiesToFeed(FeedMetadata metadata, ExportImportFeedService.ImportFeed importFeed, ImportComponent component) {
        ImportComponentOption option = importFeed.getImportOptions().findImportComponentOption(component);

        if (!option.getProperties().isEmpty() && option.getProperties().stream().anyMatch(importProperty -> StringUtils.isBlank(importProperty.getPropertyValue()))) {
            importFeed.setSuccess(false);
            if (importFeed.getTemplate() == null) {
                ExportImportTemplateService.ImportTemplate importTemplate = new ExportImportTemplateService.ImportTemplate(importFeed.getFileName());
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

}
