package com.thinkbiganalytics.metadata.api.template.export;
/*-
 * #%L
 * thinkbig-metadata-api
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

/**
 * Model object for the export of the template
 */
public class ExportTemplate {

    private String fileName;
    private String templateName;
    private String description;
    private boolean stream;
    private byte[] file;

    public ExportTemplate(String fileName, String templateName, String description, boolean stream, byte[] zipFile) {
        this.templateName = templateName;
        this.description = description;
        this.stream = stream;
        this.fileName = fileName;
        this.file = zipFile;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFile() {
        return file;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isStream() {
        return stream;
    }
}
