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
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.nifi.web.api.dto.BulletinDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ControllerServiceReferencingComponentDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentEntity;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentsEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.UpdateControllerServiceReferenceRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
     * Updates a controller service
     * This will first disable the service, stop all referencing components, enable the service, reset the state of the components back to their prior state
     *
     * @param controllerService the service to update with the updated properties
     * @return the updates service
     */
    @Override
    public ControllerServiceDTO updateServiceAndReferencingComponents(ControllerServiceDTO controllerService) {
        String id = controllerService.getId();
        //Get all references to this controller service.  this will include processors, other controller services, and reporting tasks
        Optional<ControllerServiceReferencingComponentsEntity> references = getReferences(id);

        //state of the processors prior to update
        Map<String, String> previousProcessorState = null;
        //revisions for the processor references
        Map<String, RevisionDTO> processorRevisions = null;

        //revisions for the controller service references
        Map<String, RevisionDTO> controllerServiceRevisions = null;

        //a map of component id to reference entity.  this will include all referencing processors, controller services, and reporting tasks
        Map<String, ControllerServiceReferencingComponentEntity> referencingComponentEntityMap = null;

        //Stop all processor references and also disable any other controller service references
        if (references.isPresent()) {
            //build the reference state and revision maps prior to making this update
            referencingComponentEntityMap = getReferencingComponents(references.get().getControllerServiceReferencingComponents());

            previousProcessorState =
                referencingComponentEntityMap.values().stream().filter(e -> e.getComponent().getReferenceType().equalsIgnoreCase("PROCESSOR")).map(c -> c.getComponent()).collect(Collectors.toMap(
                    ControllerServiceReferencingComponentDTO::getId, ControllerServiceReferencingComponentDTO::getState));

            processorRevisions = referencingComponentEntityMap.values().stream().filter(e -> e.getComponent().getReferenceType().equalsIgnoreCase("PROCESSOR")).collect(Collectors.toMap(
                ControllerServiceReferencingComponentEntity::getId, ControllerServiceReferencingComponentEntity::getRevision));

            controllerServiceRevisions =
                referencingComponentEntityMap.values().stream().filter(e -> e.getComponent().getReferenceType().equalsIgnoreCase("ControllerService")).collect(Collectors.toMap(
                    ControllerServiceReferencingComponentEntity::getId, ControllerServiceReferencingComponentEntity::getRevision));

            //Stop the referencing processors and ensure they are in the stopped state
            if (!processorRevisions.isEmpty()) {
                String state = NifiProcessUtil.PROCESS_STATE.STOPPED.name();
                log.info("Stopping all component references to controller service {} ", controllerService.getName());
                UpdateControllerServiceReferenceRequestEntity stopComponentsRequest = new UpdateControllerServiceReferenceRequestEntity();
                stopComponentsRequest.setState(state);
                stopComponentsRequest.setId(controllerService.getId());
                stopComponentsRequest.setReferencingComponentRevisions(processorRevisions);
                updateReferences(id, stopComponentsRequest);
                boolean updatedReferences = ensureComponentsAreOfState(id, "Processor", state, 5, 500, TimeUnit.MILLISECONDS);
                if (!updatedReferences) {
                    //error unable to change the state of the references. ... error
                    throw new NifiClientRuntimeException("Unable to stop processor references to this controller service " + controllerService.getName() + " before making the update");
                }
            }

            //disable any controller service references
            if (!controllerServiceRevisions.isEmpty()) {
                UpdateControllerServiceReferenceRequestEntity stopComponentsRequest = new UpdateControllerServiceReferenceRequestEntity();
                stopComponentsRequest.setState("DISABLED");
                stopComponentsRequest.setId(controllerService.getId());
                stopComponentsRequest.setReferencingComponentRevisions(controllerServiceRevisions);
                updateReferences(id, stopComponentsRequest);
                boolean updatedReferences = ensureComponentsAreOfState(id, "ControllerService", "DISABLED", 5, 500, TimeUnit.MILLISECONDS);
                if (!updatedReferences) {
                    //error unable to change the state of the references. ... error
                    throw new NifiClientRuntimeException(
                        "Unable to disable other controller service references to this controller service " + controllerService.getName() + " before making the update");
                }
            }

        }
        //mark this controller service as disabled
        log.info("Disabling the controller service  {} ", controllerService.getName());
        //update the service and mark it disabled.  this will throw a RuntimeException if it is not successful
        ControllerServiceDTO updatedService = updateStateByIdWithRetries(controllerService.getId(), "DISABLED", 5, 500, TimeUnit.MILLISECONDS);

        //Perform the update to this controller service
        log.info("Updating the controller service  {} ", controllerService.getName());
        updatedService = update(controllerService);
        //Enable the service
        updateStateById(controllerService.getId(), NiFiControllerServicesRestClient.State.ENABLED);

        //Enable any controller service references
        if (!controllerServiceRevisions.isEmpty()) {
            log.info("Enabling other controller services referencing this controller service  {} ", controllerService.getName());
            UpdateControllerServiceReferenceRequestEntity enableReferenceServicesRequest = new UpdateControllerServiceReferenceRequestEntity();
            enableReferenceServicesRequest.setId(controllerService.getId());
            enableReferenceServicesRequest.setState(NiFiControllerServicesRestClient.State.ENABLED.name());
            enableReferenceServicesRequest.setReferencingComponentRevisions(controllerServiceRevisions);
            updateReferences(id, enableReferenceServicesRequest);
            boolean updatedReferences = ensureComponentsAreOfState(id, "ControllerService", NiFiControllerServicesRestClient.State.ENABLED.name(), 5, 500, TimeUnit.MILLISECONDS);
            if (!updatedReferences) {
                //error unable to change the state of the references. ... error
                throw new NifiClientRuntimeException("The controller service " + controllerService.getName()
                                                     + " was updated, but it was unable to enable other controller service references to this controller service.  Please visit NiFi to reconcile and fix any controller service issues. "
                                                     + controllerService.getName());
            }
        }
        if (references.isPresent() && previousProcessorState != null) {
            //reset the processor state
            Map<String, RevisionDTO> finalComponentRevisions = processorRevisions;

            //if all referencing processors of of the same state we can call the quick rest endpoint to update all of them
            Set<String> distinctStates = previousProcessorState.values().stream().collect(Collectors.toSet());
            if (distinctStates.size() > 0) {
                if (distinctStates.size() == 1) {
                    String state = new ArrayList<>(distinctStates).get(0);
                    if(!NifiProcessUtil.PROCESS_STATE.STOPPED.name().equals(state)) {
                        UpdateControllerServiceReferenceRequestEntity startComponentsRequest = new UpdateControllerServiceReferenceRequestEntity();

                        startComponentsRequest.setState(state);
                        startComponentsRequest.setReferencingComponentRevisions(finalComponentRevisions);
                        startComponentsRequest.setId(controllerService.getId());
                        updateReferences(id, startComponentsRequest);
                        log.info("Updating all component references to be {} for controller service  {} ", state, controllerService.getName());
                        boolean updatedReferences = ensureComponentsAreOfState(id, "Processor", state, 5, 500, TimeUnit.MILLISECONDS);
                        if (!updatedReferences) {
                            //error unable to change the state of the references. ... error
                            throw new NifiClientRuntimeException("The controller service {} was updated, but it was unable to update the state of the processors as " + state
                                                                 + ".  Please visit NiFi to reconcile and fix any controller service issues. " + controllerService.getName());
                        }
                        log.info("Successfully updated controller service {}", controllerService.getName());
                    }
                } else {
                    log.info(
                        "The controller service component references ({} total) had mixed states prior to updating {}.  Going through each processor/component and setting its state back to what it was prior to the update.",
                        previousProcessorState.size(), distinctStates);
                    //update references back to previous states
                    List<ProcessorEntity> updatedProcessors = references.get().getControllerServiceReferencingComponents().stream()
                        .filter(c -> c.getComponent().getReferenceType().equalsIgnoreCase("PROCESSOR"))
                        .filter(c-> !c.getComponent().getState().equals(NifiProcessUtil.PROCESS_STATE.STOPPED.name()))
                        .map(referencingComponentEntity -> referencingComponentEntity.getComponent()).map(c -> {
                            ProcessorEntity entity = new ProcessorEntity();
                            entity.setRevision(finalComponentRevisions.get(c.getId()));
                            entity.setId(c.getId());
                            ProcessorDTO processorDTO = new ProcessorDTO();
                            processorDTO.setId(c.getId());
                            processorDTO.setState(c.getState());
                            entity.setComponent(processorDTO);
                            return entity;
                        }).collect(Collectors.toList());

                    updatedProcessors.stream().forEach(p -> getClient().processors().update(p));
                    log.info("Successfully updated controller service {} and updated {} components back to their prior state ", controllerService.getName(),
                             previousProcessorState.size());
                }
            }

        }
        return updatedService;

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

        //Mark the Service as DISABLED
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


    private boolean ensureComponentsAreOfState(final String id, final String referenceType, final String state, final int retries, final int timeout, @Nonnull final TimeUnit timeUnit) {
        return ensureComponentsAreOfState(id, referenceType, null, state, retries, timeout, timeUnit);
    }


    private boolean ensureComponentsAreOfState(final String id, final String referenceType, final Set<String> ids, final String state, final int retries, final int timeout,
                                               @Nonnull final TimeUnit timeUnit) {

        boolean allMatch = false;
        for (int count = 0; count < retries; ++count) {
            Optional<ControllerServiceReferencingComponentsEntity> references = getReferences(id);
            if (references.isPresent()) {
                allMatch =
                    references.get().getControllerServiceReferencingComponents().stream().filter(c -> c.getComponent().getReferenceType().equalsIgnoreCase(referenceType))
                        .filter(c -> ids != null ? ids.contains(c.getComponent().getId()) : true).allMatch(c -> state.equalsIgnoreCase(c.getComponent().getState()));
                if (allMatch) {
                    break;
                } else if (!allMatch && count <= retries) {
                    Uninterruptibles.sleepUninterruptibly(timeout, timeUnit);
                }
            }
        }
        return allMatch;

    }

    /**
     * get all referencing components in a single hashmap
     *
     * @param references references
     * @return the map of id to component entity
     */
    private Map<String, ControllerServiceReferencingComponentEntity> getReferencingComponents(Collection<ControllerServiceReferencingComponentEntity> references) {
        Map<String, ControllerServiceReferencingComponentEntity> map = new HashMap<>();
        references.stream().forEach(c -> {
            map.put(c.getId(), c);
            if (c.getComponent().getReferencingComponents() != null && !c.getComponent().getReferencingComponents().isEmpty()) {
                map.putAll(getReferencingComponents(c.getComponent().getReferencingComponents()));
            }
        });
        return map;
    }


}
