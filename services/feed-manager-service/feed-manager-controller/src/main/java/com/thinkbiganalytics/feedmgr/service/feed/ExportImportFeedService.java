package com.thinkbiganalytics.feedmgr.service.feed;

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

import com.google.common.collect.Sets;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.feedmgr.service.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.support.ZipFileUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/6/16.
 */
public class ExportImportFeedService {

    private static final Logger log = LoggerFactory.getLogger(ExportImportFeedService.class);

    private static final String FEED_JSON_FILE = "feed.json";

    @Autowired
    MetadataService metadataService;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    ExportImportTemplateService exportImportTemplateService;

    @Inject
    private AccessController accessController;

    public class ExportFeed {

        private String fileName;
        private byte[] file;

        public ExportFeed(String fileName, byte[] file) {
            this.fileName = fileName;
            this.file = file;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getFile() {
            return file;
        }
    }

    private Set<String> getValidZipFileEntries(){
        // do not include nifiConnectingReusableTemplate.xml - it may or may not be there or there can be many of them if flow connects to multiple reusable templates
        String[] entries = {
            "feed.json",
            "nifiTemplate.xml",
            "template.json"
        };
        return Sets.newHashSet(entries);
    }


    public class ImportFeed {

        private boolean success;
        private String fileName;
        private String feedName;
        private ExportImportTemplateService.ImportTemplate template;
        private NifiFeed nifiFeed;

        public ImportFeed(String fileName) {
            this.fileName = fileName;
        }

        private String feedJson;

        public String getFeedJson() {
            return feedJson;
        }

        public void setFeedJson(String feedJson) {
            this.feedJson = feedJson;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public ExportImportTemplateService.ImportTemplate getTemplate() {
            return template;
        }

        public void setTemplate(ExportImportTemplateService.ImportTemplate template) {
            this.template = template;
        }

        public String getFeedName() {
            return feedName;
        }

        public void setFeedName(String feedName) {
            this.feedName = feedName;
        }

        public NifiFeed getNifiFeed() {
            return nifiFeed;
        }

        public void setNifiFeed(NifiFeed nifiFeed) {
            this.nifiFeed = nifiFeed;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public void addErrorMessage(FeedMetadata feedMetadata, String errorMessage) {
            if (nifiFeed == null) {
                nifiFeed = new NifiFeed(feedMetadata, null);
            }
            nifiFeed.addErrorMessage(errorMessage);
        }
    }


    private ImportFeed readFeedJson(String fileName, byte[] content) throws IOException {

        byte[] buffer = new byte[1024];
        InputStream inputStream = new ByteArrayInputStream(content);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        // while there are entries I process them
        ImportFeed importFeed = new ImportFeed(fileName);
        while ((entry = zis.getNextEntry()) != null) {

            if (entry.getName().startsWith(FEED_JSON_FILE)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int len = 0;
                while ((len = zis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.close();
                String outString = new String(out.toByteArray(), "UTF-8");
                importFeed.setFeedJson(outString);

            }


        }
        return importFeed;


    }

    public ExportFeed exportFeed(String feedId) throws IOException {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EXPORT_FEEDS);

        FeedMetadata feed = metadataService.getFeedById(feedId);
        RegisteredTemplate template = feed.getRegisteredTemplate();
        ExportImportTemplateService.ExportTemplate exportTemplate = exportImportTemplateService.exportTemplate(feed.getTemplateId());
        //merge zip files
        String feedJson = ObjectMapperSerializer.serialize(feed);
        byte[] zipFile = ZipFileUtil.addToZip(exportTemplate.getFile(),feedJson, FEED_JSON_FILE);
        return new ExportFeed(feed.getSystemFeedName() + ".feed.zip", zipFile);

    }
    private byte[] streamToByteArray(InputStream inputStream)  throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = inputStream.read(buf)) >= 0) {
            baos.write(buf, 0, n);
        }
        byte[] content = baos.toByteArray();
        return content;
    }

    public ImportFeed importFeed(String fileName, InputStream inputStream, ImportOptions importOptions) throws IOException {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.IMPORT_FEEDS);

        byte[] content = streamToByteArray(inputStream);

        boolean isValid = ZipFileUtil.validateZipEntriesWithRequiredEntries(content,getValidZipFileEntries(),Sets.newHashSet(FEED_JSON_FILE));
        if(!isValid){
            throw new ImportFeedException("The zip file you uploaded is not valid feed export.");
        }



        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);

        final FeedCategory optionsCategory;
        if (StringUtils.isNotBlank(importOptions.getCategorySystemName())) {
            optionsCategory = metadataService.getCategoryBySystemName(importOptions.getCategorySystemName());
            if (optionsCategory == null) {
                throw new UnsupportedOperationException(String.format("No such category '%s'", importOptions.getCategorySystemName()));
            }
        } else {
            optionsCategory = null;
        }

        //verify this feed and if it exists should we overwrite/proceed
        final ImportFeed feed = readFeedJson(fileName, content);
        FeedMetadata metadata = ObjectMapperSerializer.deserialize(feed.getFeedJson(), FeedMetadata.class);

        String feedCategory = optionsCategory != null ? optionsCategory.getSystemName() : metadata.getSystemCategoryName();
        FeedMetadata existingFeed = metadataAccess.read(() -> metadataService.getFeedByName(feedCategory, metadata.getSystemFeedName()));
        if (existingFeed != null && !importOptions.isOverwrite()) {
            //if we dont have permission to overwrite then return with error that feed already exists
            feed.setSuccess(false);
            ExportImportTemplateService.ImportTemplate importTemplate = new ExportImportTemplateService.ImportTemplate(fileName);
            feed.setTemplate(importTemplate);
            feed.addErrorMessage(existingFeed, "The feed " + FeedNameUtil.fullName(feedCategory, metadata.getSystemFeedName())
                                               + " already exists.  If you would like to proceed with this import please check the box to 'Overwrite' this feed");
            return feed;
        }
        //if we get here set the import overwrite to be true to allow for the template to be overwritten
        importOptions.setOverwrite(true);
        ExportImportTemplateService.ImportTemplate template = exportImportTemplateService.importTemplate(fileName, byteArrayInputStream, importOptions);
        if (template.isVerificationToReplaceConnectingResuableTemplateNeeded()) {
            //if we dont have the permission to replace the reusable template, then return and ask for it.
            ImportFeed askForPermissionFeed = new ImportFeed(fileName);
            askForPermissionFeed.setTemplate(template);
            return askForPermissionFeed;
        }
        if (template.isSuccess()) {
            //import the feed
            feed.setTemplate(template);
            //now that we have the Feed object we need to create the instance of the feed
            NifiFeed nifiFeed = metadataAccess.commit(() -> {
                metadata.setIsNew(existingFeed == null ? true : false);
                metadata.setFeedId(existingFeed != null ? existingFeed.getFeedId() : null);
                metadata.setId(existingFeed != null ? existingFeed.getId() : null);
                //reassign the templateId to the newly registered template id
                metadata.setTemplateId(template.getTemplateId());
                if (metadata.getRegisteredTemplate() != null) {
                    metadata.getRegisteredTemplate().setNifiTemplateId(template.getNifiTemplateId());
                    metadata.getRegisteredTemplate().setId(template.getTemplateId());
                }
                //get/create category
                FeedCategory category = optionsCategory != null ? optionsCategory : metadataService.getCategoryBySystemName(metadata.getCategory().getSystemName());
                if (category == null) {
                    metadataService.saveCategory(metadata.getCategory());
                } else {
                    metadata.setCategory(category);
                }
                return metadataService.createFeed(metadata);
            });
            if (nifiFeed != null) {
                feed.setFeedName(nifiFeed.getFeedMetadata().getCategoryAndFeedName());
            }
            feed.setNifiFeed(nifiFeed);
            feed.setSuccess(nifiFeed != null && nifiFeed.isSuccess());
            return feed;

        }

        return null;


    }



}
