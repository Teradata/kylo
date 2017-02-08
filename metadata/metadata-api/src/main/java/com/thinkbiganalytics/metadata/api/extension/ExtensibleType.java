package com.thinkbiganalytics.metadata.api.extension;

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

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Describes an object which may be extended with additional fields or subtypes at runtime.
 */
public interface ExtensibleType {

    ID getId();

    ExtensibleType getSupertype();

    String getName();

    String getDiplayName();

    String getDesciption();

    DateTime getCreatedTime();

    DateTime getModifiedTime();

    Set<FieldDescriptor> getFieldDescriptors();

    FieldDescriptor getFieldDescriptor(String name);

    /**
     * Gets the user-defined fields for this type.
     *
     * @return the user-defined fields
     * @since 0.4.0
     */
    @Nonnull
    Set<UserFieldDescriptor> getUserFieldDescriptors();

    interface ID extends Serializable {

    }
}
