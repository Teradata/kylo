package com.thinkbiganalytics.nifi.v1.rest.model;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v1
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

import com.thinkbiganalytics.nifi.rest.model.NiFiAllowableValue;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptor;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;

import org.apache.nifi.web.api.dto.AllowableValueDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;
import org.apache.nifi.web.api.entity.AllowableValueEntity;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Transforms {@link NiFiPropertyDescriptor} objects for NiFi v1.0.
 */
public class NiFiPropertyDescriptorTransformV1 implements NiFiPropertyDescriptorTransform {

    @Override
    public Boolean isSensitive(@Nonnull final PropertyDescriptorDTO propertyDescriptorDTO) {
        return propertyDescriptorDTO.isSensitive();
    }

    /**
     * Transforms the specified {@link AllowableValueDTO} to a {@link NiFiAllowableValue}.
     *
     * @param dto the allowable value DTO
     * @return the NiFi allowable value
     */
    @Nonnull
    public NiFiAllowableValue toNiFiAllowableValue(@Nonnull final AllowableValueDTO dto) {

        final NiFiAllowableValue nifi = new NiFiAllowableValue();
        nifi.setDisplayName(dto.getDisplayName());
        nifi.setValue(dto.getValue());
        nifi.setDescription(dto.getDescription());
        return nifi;
    }

    @Nonnull
    @Override
    public NiFiPropertyDescriptor toNiFiPropertyDescriptor(@Nonnull final PropertyDescriptorDTO dto) {
        final NiFiPropertyDescriptor nifi = new NiFiPropertyDescriptor();
        nifi.setName(dto.getName());
        nifi.setDisplayName(dto.getDisplayName());
        nifi.setDescription(dto.getDescription());
        nifi.setDefaultValue(dto.getDefaultValue());
        nifi.setRequired(dto.isRequired());
        nifi.setSensitive(dto.isSensitive());
        nifi.setDynamic(dto.isDynamic());
        nifi.setSupportsEl(dto.getSupportsEl());
        nifi.setIdentifiesControllerService(dto.getIdentifiesControllerService());

        final List<AllowableValueEntity> allowableValues = dto.getAllowableValues();
        if (allowableValues != null) {
            nifi.setAllowableValues(
                allowableValues.stream().filter(allowableValueEntity -> allowableValueEntity.getAllowableValue() != null && allowableValueEntity.getAllowableValue().getValue() != null)
                    .map(AllowableValueEntity::getAllowableValue)
                    .map(this::toNiFiAllowableValue)
                    .collect(Collectors.toList()));
        }

        return nifi;
    }

    @Override
    public String getQueuedCount(ProcessGroupStatusDTO processGroupStatusDTO) {
        return processGroupStatusDTO.getAggregateSnapshot().getQueuedCount();
    }
}
