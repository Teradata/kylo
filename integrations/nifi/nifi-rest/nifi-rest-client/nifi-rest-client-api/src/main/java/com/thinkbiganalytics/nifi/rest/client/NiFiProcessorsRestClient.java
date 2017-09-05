package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.entity.ProcessorEntity;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Exposes the NiFi Processors REST endpoint as a Java class.
 */
public interface NiFiProcessorsRestClient {

    /**
     * Gets a processor.
     *
     * @param processGroupId the process group id
     * @param processorId    the processor id
     * @return the processor, if found
     */
    @Nonnull
    Optional<ProcessorDTO> findById(@Nonnull String processGroupId, @Nonnull String processorId);

    /**
     * Updates a processor.
     *
     * @param processor the processor
     * @return the updated processor
     * @throws NifiComponentNotFoundException if the processor does not exist
     */
    @Nonnull
    ProcessorDTO update(@Nonnull ProcessorDTO processor);


    /**
     * Updates a processor.
     *
     * @param processorEntity the processor
     * @return the updated processor
     */
    Optional<ProcessorDTO> update(@Nonnull final ProcessorEntity processorEntity);


    /**
     * Updates the schedule for a processor
     * @param schedule
     * @return
     */
    ProcessorDTO schedule(NifiProcessorSchedule schedule);

    /**
     * Updates a processor and retries {@code retryAmount} number of times if the update is unsuccessful
     *
     * @param processor   the processor
     * @param retryNumber the current attempt
     * @param retryAmount the max retry amount
     * @return the updated processor
     */
    @Nonnull
    ProcessorDTO updateWithRetry(@Nonnull ProcessorDTO processor, final int retries, final int timeout, @Nonnull final TimeUnit timeUnit);
}
