package com.thinkbiganalytics.repository.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TemplateMetadataWrapper {

    private String templateName;
    private String description;
    private String fileName;
    private String checksum;
    private boolean installed;
    private boolean stream;

    private boolean updateAvailable = false;
    private TemplateRepository repository;

    public TemplateMetadataWrapper(TemplateMetadata m){

        this.templateName = m.getTemplateName();
        this.description = m.getDescription();
        this.fileName = m.getFileName();
        this.checksum = m.getChecksum();
        this.stream = m.isStream();
        this.updateAvailable = m.isUpdateAvailable();
    }

    public TemplateRepository getRepository() {
        return repository;
    }

    public void setRepository(TemplateRepository repository) {
        this.repository = repository;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDescription() {
        return description;
    }

    public String getFileName() {
        return fileName;
    }

    public String getChecksum() {
        return checksum;
    }

    public boolean isInstalled() {
        return installed;
    }

    public boolean isStream() {
        return stream;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
}
