package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

public class RegisteredTemplateRequest {

    private String templateId;
    private String templateName;
    private boolean includePropertyDescriptors;
    private boolean includeSensitiveProperties;

    private String nifiTemplateId;
    private boolean includeAllProperties;

    /**
     * Is request coming from a Feed Details
     */
    private boolean isFeedEdit;

    /**
     * Is the request coming from editing a template
     */
    private boolean isTemplateEdit;

    public RegisteredTemplateRequest() {

    }

    public RegisteredTemplateRequest(RegisteredTemplateRequest request) {
        this.templateId = request.getTemplateId();
        this.nifiTemplateId = request.getNifiTemplateId();
        this.templateName = request.getTemplateName();
        this.isFeedEdit = request.isFeedEdit();
        this.isTemplateEdit = request.isTemplateEdit();
        this.includeSensitiveProperties = request.isIncludeSensitiveProperties();
        this.includePropertyDescriptors = request.isIncludePropertyDescriptors();
        this.includeAllProperties = request.isIncludeAllProperties();
    }

    public static RegisteredTemplateRequest requestByTemplateName(String templateName) {
        return new RegisteredTemplateRequest.Builder().templateName(templateName).build();
    }
    public static RegisteredTemplateRequest requestAccessAsServiceAccountByTemplateName(String templateName) {
        return new RegisteredTemplateRequest.Builder().templateName(templateName).isFeedEdit(true).build();
    }

    public static RegisteredTemplateRequest requestByTemplateId(String templateId) {
        return new RegisteredTemplateRequest.Builder().templateId(templateId).nifiTemplateId(templateId).build();
    }

    public static RegisteredTemplateRequest requestByNiFiTemplateProperties(String nifiTemplateId, String templateName) {
        return new RegisteredTemplateRequest.Builder().nifiTemplateId(nifiTemplateId).templateName(templateName).build();
    }

    public static RegisteredTemplateRequest requestForFeedCreation(String templateId, String templateName) {
        return new RegisteredTemplateRequest.Builder().templateId(templateId).templateName(templateName).includeSensitiveProperties(true).includeAllProperties(true).build();
    }

    public static RegisteredTemplateRequest requestForTemplateCreation(String templateId, String templateName) {
        return new RegisteredTemplateRequest.Builder().templateId(templateId).templateName(templateName).includePropertyDescriptors(true).includeSensitiveProperties(true).includeAllProperties(true)
            .build();
    }

    public static RegisteredTemplateRequest requestForFeedRead(String templateId, String templateName) {
        return new RegisteredTemplateRequest.Builder().templateId(templateId).templateName(templateName).build();
    }


    public RegisteredTemplateRequest(String templateId, String templateName, boolean includePropertyDescriptors, boolean includeSensitiveProperties, boolean includeAllProperties,
                                     String nifiTemplateId, boolean isFeedEdit, boolean isTemplateEdit) {
        this.templateId = templateId;
        this.templateName = templateName;
        this.includePropertyDescriptors = includePropertyDescriptors;
        this.includeSensitiveProperties = includeSensitiveProperties;
        this.includeAllProperties = includeAllProperties;
        this.nifiTemplateId = nifiTemplateId;
        this.isFeedEdit = isFeedEdit;
        this.isTemplateEdit = isTemplateEdit;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isIncludePropertyDescriptors() {
        return includePropertyDescriptors;
    }

    public void setIncludePropertyDescriptors(boolean includePropertyDescriptors) {
        this.includePropertyDescriptors = includePropertyDescriptors;
    }

    public boolean isIncludeSensitiveProperties() {
        return includeSensitiveProperties;
    }

    public void setIncludeSensitiveProperties(boolean includeSensitiveProperties) {
        this.includeSensitiveProperties = includeSensitiveProperties;
    }

    public String getNifiTemplateId() {
        return nifiTemplateId;
    }

    public void setNifiTemplateId(String nifiTemplateId) {
        this.nifiTemplateId = nifiTemplateId;
    }

    public boolean isIncludeAllProperties() {
        return includeAllProperties;
    }

    public void setIncludeAllProperties(boolean includeAllProperties) {
        this.includeAllProperties = includeAllProperties;
    }

    public boolean isFeedEdit() {
        return isFeedEdit;
    }

    public void setFeedEdit(boolean feedEdit) {
        isFeedEdit = feedEdit;
    }

    public void setTemplateEdit(boolean templateEdit) {
        isTemplateEdit = templateEdit;
    }

    public boolean isTemplateEdit() {
        return isTemplateEdit;
    }

    public static class Builder {

        private String templateId;
        private String templateName;
        private boolean includePropertyDescriptors;
        private boolean includeSensitiveProperties;
        private String nifiTemplateId;
        private boolean includeAllProperties;

        private boolean isFeedEdit;

        /**
         * Is the request coming from editing a template
         */
        private boolean isTemplateEdit;

        public Builder() {

        }

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder templateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public Builder includePropertyDescriptors(boolean includePropertyDescriptors) {
            this.includePropertyDescriptors = includePropertyDescriptors;
            return this;
        }

        public Builder includeSensitiveProperties(boolean includeSensitiveProperties) {
            this.includeSensitiveProperties = includeSensitiveProperties;
            return this;
        }

        public Builder includeAllProperties(boolean includeAllProperties) {
            this.includeAllProperties = includeAllProperties;
            return this;
        }

        public Builder nifiTemplateId(String nifiTemplateId) {
            this.nifiTemplateId = nifiTemplateId;
            return this;
        }

        public Builder isFeedEdit(boolean isFeedEdit) {
            this.isFeedEdit = isFeedEdit;
            return this;
        }

        public Builder isTemplateEdit(boolean isTemplateEdit) {
            this.isTemplateEdit = isTemplateEdit;
            return this;
        }

        public RegisteredTemplateRequest build() {
            return new RegisteredTemplateRequest(templateId, templateName, includePropertyDescriptors, includeSensitiveProperties, includeAllProperties, nifiTemplateId, isFeedEdit, isTemplateEdit);
        }

    }


}
