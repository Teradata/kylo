package com.thinkbiganalytics.discovery.model;

/*-
 * #%L
 * thinkbig-schema-discovery-model2
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.FileSchema;

/**
 * The model used to pass a file schema
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultFileSchema extends AbstractSchema implements FileSchema {

    private String format;
    private boolean binary;
    private boolean embeddedSchema;

    @Override
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    @Override
    public boolean hasEmbeddedSchema() {
        return embeddedSchema;
    }

    public boolean isEmbeddedSchema() {
        return embeddedSchema;
    }

    public void setEmbeddedSchema(boolean embeddedSchema) {
        this.embeddedSchema = embeddedSchema;
    }
}
