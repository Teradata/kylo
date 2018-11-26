/**
 * 
 */
package com.thinkbiganalytics.metadata.api.versioning;

/*-
 * #%L
 * kylo-metadata-api
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

import java.io.Serializable;
import java.util.Optional;

import com.thinkbiganalytics.metadata.api.template.ChangeComment;

import org.joda.time.DateTime;

/**
 * Generic definition of info about an entity version.
 */
public interface EntityVersion<I, E> {
    
    /** The version name of a draft version */
    String DRAFT_NAME = "draft";
    
    /**
     * @return the ID of this version
     */
    ID getId();

    /**
     * @return the name of this version
     */
    String getName();
    
    /**
     * @return the date the version was created
     */
    DateTime getCreatedDate();
    
    /**
     * @return an optional comment associated with the version
     */
    Optional<ChangeComment> getChangeComment();
    
    /**
     * @return the ID of the entity
     */
    I getEntityId();
    
    /**
     * @return the optional state of the entity for this version
     */
    Optional<E> getEntity();
    

    interface ID extends Serializable {

    }

    /**
     * return true if this is the very first version, false if not
     * @return
     */
    boolean isFirstVersion();
}
