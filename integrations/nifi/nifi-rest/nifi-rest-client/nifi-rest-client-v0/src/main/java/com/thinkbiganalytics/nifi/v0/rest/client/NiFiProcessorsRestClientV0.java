package com.thinkbiganalytics.nifi.v0.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v0
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

import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessorsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.entity.ProcessorEntity;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiProcessorsRestClient} for communicating with NiFi v0.6.
 */
public class NiFiProcessorsRestClientV0 implements NiFiProcessorsRestClient {

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiProcessorsRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiProcessorsRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public Optional<ProcessorDTO> findById(@Nonnull final String processGroupId, @Nonnull final String processorId) {
        try {
            return Optional.of(client.get("/controller/process-groups/" + processGroupId + "/processors/" + processorId, null, ProcessorEntity.class).getProcessor());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public ProcessorDTO update(@Nonnull final ProcessorDTO processor) {
        final ProcessorEntity processorEntity = new ProcessorEntity();
        processorEntity.setProcessor(processor);

        try {
            return client.put("/controller/process-groups/" + processor.getParentGroupId() + "/processors/" + processor.getId(), processorEntity, ProcessorEntity.class).getProcessor();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processor.getId(), NifiConstants.NIFI_COMPONENT_TYPE.PROCESSOR, e);
        }
    }

    public ProcessorDTO updateWithRetry(@Nonnull ProcessorDTO processor, final int retries, final int timeout, @Nonnull final TimeUnit timeUnit) {

        Exception lastError = null;

        for (int count = 0; count <= retries; ++count) {
            try {
                return update(processor);
            } catch (final Exception e) {
                lastError = e;
                Uninterruptibles.sleepUninterruptibly(timeout, timeUnit);
            }
        }

        // Give up
        throw new NifiClientRuntimeException("Unable to update processor: " + processor.getId(), lastError);

    }

}
