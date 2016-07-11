package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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


    public class ImportFeed {

        private String fileName;
        private ExportImportTemplateService.ImportTemplate template;

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
    }


    private byte[] addToZip(byte[] zip, String file, String fileName) throws IOException {
        InputStream zipInputStream = new ByteArrayInputStream(zip);
        ZipInputStream zis = new ZipInputStream(zipInputStream);
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int len = 0;
                while ((len = zis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.close();

                zos.putNextEntry(entry);
                zos.write(out.toByteArray());
                zos.closeEntry();

            }
            zis.closeEntry();
            zis.close();

            entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            zos.write(file.getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
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
        FeedMetadata feed = metadataService.getFeedById(feedId);
        RegisteredTemplate template = feed.getRegisteredTemplate();
        ExportImportTemplateService.ExportTemplate exportTemplate = exportImportTemplateService.exportTemplate(feed.getTemplateId());
        //merge zip files
        String feedJson = ObjectMapperSerializer.serialize(feed);
        byte[] zipFile = addToZip(exportTemplate.getFile(), feedJson, FEED_JSON_FILE);
        ExportFeed exportFeed = new ExportFeed(feed.getSystemFeedName() + ".zip", zipFile);
        return exportFeed;

    }

    public ImportFeed importFeed(String fileName, InputStream inputStream, boolean overwrite) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while ((n = inputStream.read(buf)) >= 0) {
            baos.write(buf, 0, n);
        }
        byte[] content = baos.toByteArray();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);

        ExportImportTemplateService.ImportTemplate template = exportImportTemplateService.importTemplate(fileName, byteArrayInputStream, overwrite, false);
        if (template.isSuccess()) {
            //import the feed
            ImportFeed feed = readFeedJson(fileName, content);
            feed.setTemplate(template);
            //now that we have the Feed object we need to create the instance of the feed
            metadataAccess.commit(new Command<FeedMetadata>() {
                @Override
                public FeedMetadata execute() {
                    FeedMetadata metadata = ObjectMapperSerializer.deserialize(feed.getFeedJson(), FeedMetadata.class);
                    //get/create category
                    FeedCategory category = metadataService.getCategoryBySystemName(metadata.getCategory().getSystemName());
                    if (category == null) {
                        metadataService.saveCategory(metadata.getCategory());
                    }
                    metadataService.createFeed(metadata);
                    return metadata;
                }
            });
            return feed;

        }

        return null;


    }

}
