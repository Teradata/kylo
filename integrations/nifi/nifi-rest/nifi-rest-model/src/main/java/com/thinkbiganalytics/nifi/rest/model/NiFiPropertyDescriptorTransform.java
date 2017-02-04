package com.thinkbiganalytics.nifi.rest.model;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;

import javax.annotation.Nonnull;

/**
 * Transforms objects between a {@link NiFiPropertyDescriptor} and a {@link PropertyDescriptorDTO}.
 */
public interface NiFiPropertyDescriptorTransform {

    /**
     * Indicates if the specified property has a sensitive value.
     *
     * @param propertyDescriptorDTO the property descriptor
     * @return {@code true} if the value is sensitive, {@code false} otherwise
     */
    Boolean isSensitive(@Nonnull PropertyDescriptorDTO propertyDescriptorDTO);

    /**
     * Transforms the specified {@link PropertyDescriptorDTO} to a {@link NiFiPropertyDescriptor}.
     *
     * @param propertyDescriptorDTO the property descriptor DTO
     * @return the NiFi property descriptor
     */
    @Nonnull
    NiFiPropertyDescriptor toNiFiPropertyDescriptor(@Nonnull PropertyDescriptorDTO propertyDescriptorDTO);

    /**
     * Returns the of items in queue for the passed in statistics
     *
     * @param processGroupStatusDTO a groups statistics
     * @return the of items in queue for the passed in statistics
     */
    String getQueuedCount(ProcessGroupStatusDTO processGroupStatusDTO);
}
