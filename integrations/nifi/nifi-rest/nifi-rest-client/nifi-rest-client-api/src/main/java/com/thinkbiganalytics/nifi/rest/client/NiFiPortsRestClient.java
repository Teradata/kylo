package com.thinkbiganalytics.nifi.rest.client;

import org.apache.nifi.web.api.dto.PortDTO;

import javax.annotation.Nonnull;

/**
 * Exposes the NiFi Ports REST endpoint as a Java class.
 */
public interface NiFiPortsRestClient {

    /**
     * Updates an input port.
     *
     * @param processGroupId the process group id
     * @param inputPort the input port
     * @return the updated input port
     * @throws NifiComponentNotFoundException if the process group does not exist
     */
    @Nonnull
    PortDTO updateInputPort(@Nonnull String processGroupId, @Nonnull PortDTO inputPort);

    /**
     * Updates an output port.
     *
     * @param processGroupId the process group id
     * @param outputPort the output port
     * @return the updated output port
     * @throws NifiComponentNotFoundException if the process group does not exist
     */
    @Nonnull
    PortDTO updateOutputPort(@Nonnull String processGroupId, @Nonnull PortDTO outputPort);
}
