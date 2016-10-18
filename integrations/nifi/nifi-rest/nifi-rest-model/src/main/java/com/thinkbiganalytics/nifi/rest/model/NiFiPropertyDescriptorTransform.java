package com.thinkbiganalytics.nifi.rest.model;

import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;

import javax.annotation.Nonnull;

/**
 * Transforms objects between a {@link NiFiPropertyDescriptor} and a {@link PropertyDescriptorDTO}.
 */
public interface NiFiPropertyDescriptorTransform {

    /**
     * Transforms the specified {@link PropertyDescriptorDTO} to a {@link NiFiPropertyDescriptor}.
     *
     * @param propertyDescriptorDTO the property descriptor DTO
     * @return the NiFi property descriptor
     */
    @Nonnull
    NiFiPropertyDescriptor toNiFiPropertyDescriptor(@Nonnull PropertyDescriptorDTO propertyDescriptorDTO);
}
