package com.thinkbiganalytics.nifi.rest.model;

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

    String getQueuedCount(ProcessGroupStatusDTO processGroupStatusDTO);
}
