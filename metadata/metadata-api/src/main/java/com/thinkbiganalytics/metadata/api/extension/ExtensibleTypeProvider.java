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

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Provides access to {@link ExtensibleType} objects.
 */
public interface ExtensibleTypeProvider {

    ExtensibleType.ID resolve(Serializable ser);

    /**
     * Creates a builder for a new type with the specified name.
     *
     * @param name the type name
     * @return the type builder
     * @throws RuntimeException if a type with the same name already exists, or the repository is unavailable
     */
    @Nonnull
    ExtensibleTypeBuilder buildType(@Nonnull String name);

    /**
     * Creates a builder that overwrites the specified type.
     *
     * @param id the type id
     * @return the type builder
     * @throws RuntimeException if the type does not exist, or the repository is unavailable
     */
    @Nonnull
    ExtensibleTypeBuilder updateType(@Nonnull ExtensibleType.ID id);

    ExtensibleType getType(ExtensibleType.ID id);

    ExtensibleType getType(String name);

    boolean deleteType(ExtensibleType.ID id);

    List<ExtensibleType> getTypes();

    List<ExtensibleType> getTypes(ExtensibleType type);

    Set<FieldDescriptor> getPropertyDescriptors(String nodeType);
}
