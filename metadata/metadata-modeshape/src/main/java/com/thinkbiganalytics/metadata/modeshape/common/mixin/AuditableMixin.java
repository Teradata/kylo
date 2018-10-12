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

import com.thinkbiganalytics.metadata.api.Auditable;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;

import org.joda.time.DateTime;

/**
 * A mixin interface to be implemented by classes that wrap nodes extending the "mix:lastModified" and "mix:created" mixin types.
 */
public interface AuditableMixin extends WrappedNodeMixin, Auditable {
    
    String CREATED_TIME = JcrPropertyConstants.CREATED_TIME;
    String CREATED_BY = JcrPropertyConstants.CREATED_BY;
    String MODIFIED_TIME = JcrPropertyConstants.MODIFIED_TIME;
    String MODIFIED_BY = JcrPropertyConstants.MODIFIED_BY;

    
    default DateTime getCreatedTime() {
        return getProperty(CREATED_TIME, DateTime.class, null);
    }

    default DateTime getModifiedTime() {
        return getProperty(MODIFIED_TIME, DateTime.class, null);
    }

    default String getCreatedBy() {
        return getProperty(CREATED_BY, String.class, null);
    }

    default String getModifiedBy() {
        return getProperty(MODIFIED_BY, String.class, null);
    }

}
