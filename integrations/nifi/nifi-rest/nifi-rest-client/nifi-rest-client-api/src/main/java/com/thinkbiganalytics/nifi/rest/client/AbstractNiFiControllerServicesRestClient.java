package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * kylo-nifi-rest-client-api
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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.BulletinDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.ws.rs.ClientErrorException;

/**
 * Provides a standard implementation of {@link NiFiControllerServicesRestClient} that can be extended for different NiFi versions.
 */
public abstract class AbstractNiFiControllerServicesRestClient implements NiFiControllerServicesRestClient {

    private static final Logger log = LoggerFactory.getLogger(AbstractNiFiControllerServicesRestClient.class);

    /**
     * Asynchronous executor
     */
    @Nonnull
    protected final ExecutorService executor = Executors.newSingleThreadExecutor(
        new ThreadFactoryBuilder()
            .setThreadFactory(Executors.defaultThreadFactory())
            .setDaemon(true)
            .setNameFormat("nifi-controller-services-client-pool-%d")
            .build()
    );

    @Nonnull
    @Override
    public Future<Optional<ControllerServiceDTO>> disableAndDeleteAsync(@Nonnull final String id) {
        return executor.submit(() -> {
            try {
                updateStateById(id, State.DISABLED);
            } catch (final Exception e) {
                log.error("Failed to disable controller service: {}", id, e);
            }
            return delete(id);
        });
    }

    @Override
    public ControllerServiceDTO updateStateById(@Nonnull final String id, @Nonnull final State state) {
        if (state == State.DISABLED) {
            return updateStateByIdWithRetries(id, state.name(), 60, 1, TimeUnit.SECONDS);
        } else {
            return updateStateByIdWithRetries(id, state.name(), 3, 300, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Sends a request to update the state of the specified controller service and waits for it to finish updating.
     *
     * @param id       the controller service id
     * @param state    the new state
     * @param retries  number of retries, at least 0; will try {@code retries} + 1 times
     * @param timeout  duration to wait between retries
     * @param timeUnit unit of time for {@code timeout}
     * @return the controller service, if updated
     * @throws NifiClientRuntimeException     if the state cannot be changed
     * @throws NifiComponentNotFoundException if the controller service does not exist
     */
    protected ControllerServiceDTO updateStateByIdWithRetries(@Nonnull final String id, @Nonnull final String state, final int retries, final int timeout, @Nonnull final TimeUnit timeUnit) {
        // Update controller service
        ControllerServiceDTO controllerService = new ControllerServiceDTO();
        controllerService.setId(id);
        controllerService.setState(state);
        controllerService = update(controllerService);

        // Wait for finished
        for (int count = 0; isPendingState(controllerService.getState(), state) && count < retries; ++count) {
            Uninterruptibles.sleepUninterruptibly(timeout, timeUnit);
            controllerService = findById(id).orElseThrow(() -> new NifiComponentNotFoundException(id, NifiConstants.NIFI_COMPONENT_TYPE.CONTROLLER_SERVICE, null));
        }

        // Verify state change or throw bulletin message
        if (state.equals(controllerService.getState())) {
            return controllerService;
        } else {
            String msg = id;
            try {
                final List<BulletinDTO> bulletins = getClient().getBulletins(id);
                if (!bulletins.isEmpty()) {
                    msg = bulletins.get(0).getMessage();
                }
            } catch (final ClientErrorException e) {
                // ignored
            }
            throw new NifiClientRuntimeException("Timeout waiting for controller service to be " + state + ": " + msg);
        }
    }

    /**
     * Indicates if a controller service is pending a change to the specified state.
     *
     * @param currentState the current state of the controller service
     * @param finalState   the expected state of the controller service
     * @return {@code true} if the controller service is still transitioning to the final state, or {@code false} otherwise
     */
    private boolean isPendingState(@Nonnull final String currentState, @Nonnull final String finalState) {
        return ("ENABLING".equals(currentState) && "ENABLED".equals(finalState)) || ("DISABLING".equals(currentState) && "DISABLED".equals(finalState));
    }
}
