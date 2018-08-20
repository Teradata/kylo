/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.metadata.api.SystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;

/**
 * To be implemented by JCR objects that have a system name and whose node types extend "mix:title".
 */
public interface SystemEntityMixin extends WrappedNodeMixin, SystemEntity {
    
    String SYSTEM_NAME = JcrPropertyConstants.SYSTEM_NAME;
    String TITLE = JcrPropertyConstants.TITLE;
    String DESCRIPTION = JcrPropertyConstants.DESCRIPTION;
    

    default String getSystemName() {
        return getProperty(SYSTEM_NAME, String.class);
    }
    
    default void setSystemName(String systemName) {
        setProperty(SYSTEM_NAME, systemName);
    }
    
    default String getDescription() {
        return getProperty(DESCRIPTION, String.class);
    }

    default void setDescription(String description) {
        setProperty(DESCRIPTION, description);
    }

    default String getTitle() {
        return getProperty(TITLE, String.class);
    }

    default void setTitle(String title) {
        setProperty(TITLE, title);
    }

}
