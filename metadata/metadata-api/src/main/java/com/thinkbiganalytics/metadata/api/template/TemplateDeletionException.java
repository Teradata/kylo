package com.thinkbiganalytics.metadata.api.template;

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

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 */
public class TemplateDeletionException extends MetadataException {

    private static final long serialVersionUID = 1L;

    String templateName;
    String templateId;


    public TemplateDeletionException(String templateName, String templateId, String message) {
        super("Unable to delete the template with the name " + templateName + ", id: " + templateId + " " + (message != null ? message : ""));
        this.templateName = templateName;
        this.templateId = templateId;
    }
}
