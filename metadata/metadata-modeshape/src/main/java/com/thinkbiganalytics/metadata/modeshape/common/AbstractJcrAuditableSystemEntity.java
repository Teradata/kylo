package com.thinkbiganalytics.metadata.modeshape.common;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import org.joda.time.DateTime;

import javax.jcr.Node;

/**
 */
public class AbstractJcrAuditableSystemEntity extends AbstractJcrSystemEntity {

    public AbstractJcrAuditableSystemEntity(Node node) {
        super(node);
    }


    public DateTime getCreatedTime() {
        return getProperty(JcrPropertyConstants.CREATED_TIME, DateTime.class);
    }

    public void setCreatedTime(DateTime createdTime) {
        // setProperty(JcrPropertyConstants.CREATED_TIME, createdTime);
    }

    public DateTime getModifiedTime() {
        return getProperty(JcrPropertyConstants.MODIFIED_TIME, DateTime.class);
    }

    public void setModifiedTime(DateTime modifiedTime) {
        // setProperty(JcrPropertyConstants.CREATED_TIME, modifiedTime);
    }

    public String getCreatedBy() {
        return getProperty(JcrPropertyConstants.CREATED_BY, String.class);
    }

    public String getModifiedBy() {
        return getProperty(JcrPropertyConstants.MODIFIED_BY, String.class);
    }

}
