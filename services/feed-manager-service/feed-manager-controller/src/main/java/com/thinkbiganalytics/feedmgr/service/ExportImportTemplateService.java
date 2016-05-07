package com.thinkbiganalytics.feedmgr.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.support.ObjectMapperSerializer;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.rest.JerseyClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by sr186054 on 5/6/16.
 */
public class ExportImportTemplateService {

    private static final String NIFI_TEMPLATE_XML_FILE = "nifiTemplate.xml";

    private static final String TEMPLATE_JSON_FILE = "template.json";


    @Autowired
    MetadataService metadataService;

    @Autowired
    NifiRestClient nifiRestClient;

    public class ExportTemplate {
        private String fileName;
        private byte[] file;

        public ExportTemplate(String fileName, byte[] file) {
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

    public class ImportTemplate {
        String templateName;
        boolean success;
        private NifiProcessGroup templateResults;

        public ImportTemplate() {

        }

        private String nifiTemplateXml;
        private String templateJson;

        @JsonIgnore
        public boolean isValid() {
            return StringUtils.isNotBlank(templateJson) && StringUtils.isNotBlank(nifiTemplateXml);
        }

        public ImportTemplate(String templateName, boolean success) {
            this.templateName = templateName;
            this.success = success;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getNifiTemplateXml() {
            return nifiTemplateXml;
        }

        public void setNifiTemplateXml(String nifiTemplateXml) {
            this.nifiTemplateXml = nifiTemplateXml;
        }

        public String getTemplateJson() {
            return templateJson;
        }

        public void setTemplateJson(String templateJson) {
            this.templateJson = templateJson;
        }

        public NifiProcessGroup getTemplateResults() {
            return templateResults;
        }

        public void setTemplateResults(NifiProcessGroup templateResults) {
            this.templateResults = templateResults;
        }
    }

    public ExportTemplate exportTemplate(String templateId) {
        RegisteredTemplate template = metadataService.getRegisteredTemplate(templateId);
        if (template != null) {
            String templateXml = null;
            try {
                if (template != null) {
                    try {
                        templateXml = nifiRestClient.getTemplateXml(template.getNifiTemplateId());
                    } catch (JerseyClientException e) {
                        TemplateDTO templateDTO = nifiRestClient.getTemplateByName(template.getTemplateName());
                        if (templateDTO != null) {
                            templateXml = nifiRestClient.getTemplateXml(templateDTO.getId());
                        }
                    }
                }
            } catch (JerseyClientException e) {
                throw new UnsupportedOperationException("Unable to find Nifi Template for " + templateId);
            }

            //create a zip file with the template and xml
            byte[] zipFile = zip(template, templateXml);

            return new ExportTemplate(SystemNamingService.generateSystemName(template.getTemplateName()) + ".zip", zipFile);

        } else {
            throw new UnsupportedOperationException("Unable to find Template for " + templateId);
        }
    }

    private byte[] zip(RegisteredTemplate template, String nifiTemplateXml) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry = new ZipEntry(NIFI_TEMPLATE_XML_FILE);
            zos.putNextEntry(entry);
            zos.write(nifiTemplateXml.getBytes());
            zos.closeEntry();
            entry = new ZipEntry(TEMPLATE_JSON_FILE);
            zos.putNextEntry(entry);
            String json = ObjectMapperSerializer.serialize(template);
            zos.write(json.getBytes());
            zos.closeEntry();


  /* use more Entries to add more files
     and use closeEntry() to close each file entry */

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return baos.toByteArray();
    }

    @Transactional(transactionManager = "metadataTransactionManager")
    public ImportTemplate importTemplate(InputStream inputStream, boolean overwrite) throws IOException {
        ImportTemplate importTemplate = openZip(inputStream);
        RegisteredTemplate template = ObjectMapperSerializer.deserialize(importTemplate.getTemplateJson(), RegisteredTemplate.class);

        //1 ensure this template doesnt already exist
        RegisteredTemplate existingTemplate = metadataService.getRegisteredTemplateByName(template.getTemplateName());
        if (existingTemplate != null) {
            if (!overwrite) {
                throw new UnsupportedOperationException("Unable to import the template " + template.getTemplateName() + " because it already exists");
            }
            template.setId(existingTemplate.getId());
        } else {
            template.setId(null);
        }

        try {
            if (overwrite) {
                //check and delete nifi template if it does exist
                TemplateDTO templateDTO = nifiRestClient.getTemplateByName(template.getTemplateName());
                if (templateDTO != null) {
                    nifiRestClient.deleteTemplate(templateDTO.getId());
                }
            }


            TemplateDTO dto = nifiRestClient.importTemplate(template.getTemplateName(), importTemplate.getNifiTemplateXml());
            template.setNifiTemplateId(dto.getId());
            metadataService.registerTemplate(template);
            //get the new template
            template = metadataService.getRegisteredTemplateByName(template.getTemplateName());


            NifiProcessGroup newTemplateInstance = nifiRestClient.createNewTemplateInstance(template.getNifiTemplateId());
            if (newTemplateInstance.isSuccess()) {
                importTemplate.setSuccess(true);
            } else {
                //delete this template
                importTemplate.setSuccess(false);
                //TODO DELETE THE TEMPLATE FROM THE DB
                //if it doesnt have any feeds delete it
                metadataService.deleteRegisteredTemplate(template.getId());
            }
            importTemplate.setTemplateResults(newTemplateInstance);
            //delete processgroup
            nifiRestClient.deleteProcessGroup(newTemplateInstance.getProcessGroupEntity().getProcessGroup());

        } catch (JerseyClientException e) {
            throw new UnsupportedOperationException("Error importing template into nifi " + e.getMessage());
        }

        //register it

        return importTemplate;
    }

    private ImportTemplate openZip(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        // while there are entries I process them
        ImportTemplate importTemplate = new ImportTemplate();
        while ((entry = zis.getNextEntry()) != null) {
            System.out.println("entry: " + entry.getName() + ", " + entry.getSize());
            // consume all the data from this entry
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int len = 0;
            while ((len = zis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            String outString = new String(out.toByteArray(), "UTF-8");
            if (entry.getName().startsWith(NIFI_TEMPLATE_XML_FILE)) {
                importTemplate.setNifiTemplateXml(outString);
            } else if (entry.getName().startsWith(TEMPLATE_JSON_FILE)) {
                importTemplate.setTemplateJson(outString);
            }

            //while (zis.available() > 0)
            //  zis.read();

        }
        zis.closeEntry();
        zis.close();
        if (!importTemplate.isValid()) {
            throw new UnsupportedOperationException(" The file you uploaded is not a valid archive.  Please ensure the Zip file has been exported from the system and has 2 valid files named: " + NIFI_TEMPLATE_XML_FILE + ", and " + TEMPLATE_JSON_FILE);
        }
        importTemplate.setSuccess(true);
        return importTemplate;

    }


}
