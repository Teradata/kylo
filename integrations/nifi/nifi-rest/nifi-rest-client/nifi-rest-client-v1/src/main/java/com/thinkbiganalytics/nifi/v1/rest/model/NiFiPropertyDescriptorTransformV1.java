package com.thinkbiganalytics.nifi.v1.rest.model;

import com.thinkbiganalytics.nifi.rest.model.NiFiAllowableValue;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptor;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;

import org.apache.nifi.web.api.dto.AllowableValueDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
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
            nifi.setAllowableValues(allowableValues.stream()
                    .map(AllowableValueEntity::getAllowableValue)
                    .map(this::toNiFiAllowableValue)
                    .collect(Collectors.toList()));
        }

        return nifi;
    }
}
