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

import javax.annotation.Nonnull;

/**
 * Builds a new field.
 */
public interface FieldDescriptorBuilder {

    FieldDescriptorBuilder name(String name);

    FieldDescriptorBuilder type(FieldDescriptor.Type type);

    FieldDescriptorBuilder displayName(String name);

    FieldDescriptorBuilder description(String descr);

    FieldDescriptorBuilder collection(boolean flag);

    FieldDescriptorBuilder required(boolean flag);

    /**
     * Adds the specified metadata property to this field.
     *
     * @param name  the property name
     * @param value the property value
     * @return this field
     */
    @Nonnull
    FieldDescriptorBuilder property(@Nonnull String name, @Nonnull String value);

    ExtensibleTypeBuilder add();
}
