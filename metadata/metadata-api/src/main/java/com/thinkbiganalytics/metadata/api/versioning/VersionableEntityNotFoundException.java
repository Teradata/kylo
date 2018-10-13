package com.thinkbiganalytics.metadata.api.versioning;

/*-
 * #%L
 * kylo-metadata-api
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

import com.thinkbiganalytics.metadata.api.MetadataException;

import java.io.Serializable;

public class VersionableEntityNotFoundException extends MetadataException {

    private static final long serialVersionUID = 1L;

    private final Serializable id;

    public VersionableEntityNotFoundException(Serializable id) {
        this("No versionable entity exists with the ID: " + id, id);
    }

    public VersionableEntityNotFoundException(String message, Serializable id) {
        super(message);
        this.id = id;
    }

    /**
     * @return the id
     */
    public Serializable getId() {
        return id;
    }
}
