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

import javax.jcr.Node;

/**
 */
public class AbstractJcrSystemEntity extends JcrPropertiesEntity {

    public static final String TITLE = JcrPropertyConstants.TITLE;
    public static final String SYSTEM_NAME = JcrPropertyConstants.SYSTEM_NAME;
    public static final String DESCRIPTION = JcrPropertyConstants.DESCRIPTION;

    public AbstractJcrSystemEntity(Node node) {
        super(node);
    }

    public String getDescription() {
        return getProperty(DESCRIPTION, String.class);
    }

    public void setDescription(String description) {
        setProperty(DESCRIPTION, description);
    }

    public String getSystemName() {
        return getProperty(SYSTEM_NAME, String.class);
    }

    public void setSystemName(String systemName) {
        setProperty(SYSTEM_NAME, systemName);
    }

    public String getTitle() {
        return getProperty(TITLE, String.class);
    }

    public void setTitle(String title) {
        setProperty(TITLE, title);
    }


}
