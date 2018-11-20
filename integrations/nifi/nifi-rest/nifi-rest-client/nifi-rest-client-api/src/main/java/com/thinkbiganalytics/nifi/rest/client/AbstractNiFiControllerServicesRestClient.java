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

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ControllerServiceReferencingComponentDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.ReportingTaskDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentEntity;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentsEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.ReportingTaskEntity;
import org.apache.nifi.web.api.entity.UpdateControllerServiceReferenceRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.ws.rs.ClientErrorException;

/**
 * Provides a standard implementation of {@link NiFiControllerServicesRestClient} that can be extended for different NiFi versions.
 */
public abstract class AbstractNiFiControllerServicesRestClient implements NiFiControllerServicesRestClient {

    private static final Logger log = LoggerFactory.getLogger(AbstractNiFiControllerServicesRestClient.class);

    private static final String REFERENCE_TYPE_CONTROLLER_SERVICE = "ControllerService";

    private static final String REFERENCE_TYPE_PROCESSOR = "Processor";

    private static final String REFERENCE_TYPE_REPORTING_TASK = "ReportingTask";

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
     * Updates a controller service.
     *
     * <p>This will first stop all referencing components, disable the service, update the service, enable the service, then reset the state of the components back to their prior state.</p>
     *
     * @param controllerService the service to update with the updated properties
     * @return the updated service
     */
    @Nonnull
    @Override
    public ControllerServiceDTO updateServiceAndReferencingComponents(@Nonnull final ControllerServiceDTO controllerService) {
        // Recursively get all references to this controller service. This will include processors, other controller services, and reporting tasks.
        final Optional<ControllerServiceReferencingComponentsEntity> referencesEntity = getReferences(controllerService.getId());
        final Set<ControllerServiceReferencingComponentEntity> references = referencesEntity.isPresent()
                                                                            ? flattenReferencingComponents(referencesEntity.get()).collect(Collectors.toSet())
                                                                            : Collections.emptySet();

        //build the reference state and revision maps prior to making this update
        final Map<String, RevisionDTO> referencingSchedulableComponentRevisions = new HashMap<>();
        final Map<String, RevisionDTO> referencingServiceRevisions = new HashMap<>();
        references.forEach(reference -> {
            if (REFERENCE_TYPE_CONTROLLER_SERVICE.equals(reference.getComponent().getReferenceType())) {
                referencingServiceRevisions.put(reference.getId(), reference.getRevision());
            } else {
                referencingSchedulableComponentRevisions.put(reference.getId(), reference.getRevision());
            }
        });

        // Update service and referencing components
        ControllerServiceDTO updatedService = null;
        Exception updateException = null;
        try {
            // Stop the referencing processors and ensure they are in the stopped state
            log.info("Stopping all component references to controller service {} ", controllerService.getName());
            if (!referencingSchedulableComponentRevisions.isEmpty() && !updateReferencingSchedulableComponents(controllerService, referencingSchedulableComponentRevisions, false)) {
                //error unable to change the state of the references. ... error
                throw new NifiClientRuntimeException("Unable to stop processor references to this controller service " + controllerService.getName() + " before making the update");
            }

            // Disable any controller service references
            if (!referencingServiceRevisions.isEmpty() && !updateReferencingServices(controllerService, referencingServiceRevisions, false)) {
                //error unable to change the state of the references. ... error
                throw new NifiClientRuntimeException("Unable to disable other controller service references to this controller service " + controllerService.getName() + " before making the update");
            }

            // Update the service and mark it disabled. This will throw a RuntimeException if it is not successful.
            log.info("Disabling the controller service  {} ", controllerService.getName());
            updateStateByIdWithRetries(controllerService.getId(), State.DISABLED.name(), 5, 500, TimeUnit.MILLISECONDS);

            // Perform the update to this controller service
            log.info("Updating the controller service  {} ", controllerService.getName());
            updatedService = update(controllerService);

            // Enable the service
            updateStateById(controllerService.getId(), State.ENABLED);
        } catch (final Exception e) {
            log.error("Reverting changes after controller service {} [{}] failed to update: ", controllerService.getId(), controllerService.getName(), e);
            updateException = e;
        }

        // Enable any controller service references
        boolean servicesUpdate;
        try {
            log.info("Enabling other controller services referencing this controller service  {} ", controllerService.getName());
            servicesUpdate = updateReferencingServices(controllerService, referencingServiceRevisions, true);
        } catch (final Exception e) {
            log.debug("Failed to restore referencing service states for controller service {} [{}]: {}", controllerService.getId(), controllerService.getName(), e, e);
            servicesUpdate = false;
        }

        // Update references back to previous states
        boolean componentsUpdate;
        try {
            final Set<ControllerServiceReferencingComponentEntity> runningComponents = references.stream()
                .filter(reference -> !REFERENCE_TYPE_CONTROLLER_SERVICE.equals(reference.getComponent().getReferenceType()))
                .filter(reference -> NifiProcessUtil.PROCESS_STATE.RUNNING.name().equals(reference.getComponent().getState()))
                .collect(Collectors.toSet());
            if (runningComponents.size() == referencingSchedulableComponentRevisions.size()) {
                log.info("Updating all component references to be RUNNING for controller service  {} ", controllerService.getName());
                componentsUpdate = updateReferencingSchedulableComponents(controllerService, referencingSchedulableComponentRevisions, true);
            } else {
                log.info("The controller service component references ({} total) had mixed states prior to updating. Going through each processor/component and setting its state back to"
                         + " what it was prior to the update.", referencingSchedulableComponentRevisions.size());
                componentsUpdate = startSchedulableComponents(runningComponents);
                log.info("Successfully updated controller service {} and updated {} components back to their prior state ", controllerService.getName(), runningComponents.size());
            }
        } catch (final Exception e) {
            log.debug("Failed to restore referencing component states for controller service {} [{}]: {}", controllerService.getId(), controllerService.getName(), e, e);
            componentsUpdate = false;
        }

        // Verify final state
        if (updateException != null) {
            throw Throwables.propagate(updateException);
        }
        if (!servicesUpdate) {
            //error unable to change the state of the references. ... error
            throw new NifiClientRuntimeException("Kylo was unable to enable other controller service references to the " + controllerService.getName() + " controller service. Please visit NiFi"
                                                 + " to reconcile and fix any controller service issues.");
        }
        if (!componentsUpdate) {
            //error unable to change the state of the references. ... error
            throw new NifiClientRuntimeException("Kylo was unable to update the state of the processors as RUNNING. Please visit NiFi to reconcile and fix any controller service issues. "
                                                 + controllerService.getName());
        }

        log.info("Successfully updated controller service {}", controllerService.getName());
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
            log.debug("Waiting for controller service {} to exit pending state {}. Try {} of {}.", id, controllerService.getState(), count + 1, retries);
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

    /**
     * Recursively verifies the state of all referencing components.
     */
    @Nonnull
    private Set<ControllerServiceReferencingComponentEntity> pollReferencingComponents(@Nonnull final String controllerServiceId,
                                                                                       @Nonnull final Predicate<String> referenceTypeFilter,
                                                                                       @Nonnull final Predicate<ControllerServiceReferencingComponentEntity> statePredicate,
                                                                                       final int retries,
                                                                                       final int timeout,
                                                                                       @Nonnull final TimeUnit timeUnit) {
        int count = 0;
        boolean last;
        final Predicate<ControllerServiceReferencingComponentEntity> preFilter = entity -> referenceTypeFilter.test(entity.getComponent().getReferenceType());

        do {
            last = ++count >= retries;

            final ControllerServiceReferencingComponentsEntity referencesEntity = getReferences(controllerServiceId)
                .orElseThrow(() -> new NifiClientRuntimeException("Failed to retrieve references for controller service " + controllerServiceId));
            final Stream<ControllerServiceReferencingComponentEntity> references = flattenReferencingComponents(referencesEntity);

            if (last) {
                return references.filter(preFilter).filter(statePredicate.negate()).collect(Collectors.toSet());
            } else if (references.filter(preFilter).allMatch(statePredicate)) {
                return Collections.emptySet();
            } else {
                Uninterruptibles.sleepUninterruptibly(timeout, timeUnit);
            }
        } while (true);
    }

    /**
     * Get all referencing components
     */
    @Nonnull
    private Stream<ControllerServiceReferencingComponentEntity> flattenReferencingComponents(@Nonnull final ControllerServiceReferencingComponentEntity entity) {
        final Set<ControllerServiceReferencingComponentEntity> refs = entity.getComponent().getReferencingComponents();
        return refs != null ? Stream.concat(refs.stream(), refs.stream().flatMap(this::flattenReferencingComponents)) : Stream.empty();
    }

    /**
     * Get all referencing components
     */
    @Nonnull
    private Stream<ControllerServiceReferencingComponentEntity> flattenReferencingComponents(@Nonnull final ControllerServiceReferencingComponentsEntity entity) {
        final Set<ControllerServiceReferencingComponentEntity> refs = entity.getControllerServiceReferencingComponents();
        return refs != null ? Stream.concat(refs.stream(), refs.stream().flatMap(this::flattenReferencingComponents)) : Stream.empty();
    }

    /**
     * Updates the scheduled state of the processors and reporting tasks referencing the specified controller service.
     *
     * @param controllerService    controller service
     * @param referencingRevisions revisions of referencing schedulable components to update
     * @param running              {@code true} to start components, or {@code false} to stop
     * @return {@code true} if all components were updated successfully
     */
    private boolean updateReferencingSchedulableComponents(@Nonnull final ControllerServiceDTO controllerService, @Nonnull final Map<String, RevisionDTO> referencingRevisions, final boolean running) {
        // Issue request to update referencing components
        final UpdateControllerServiceReferenceRequestEntity referenceEntity = new UpdateControllerServiceReferenceRequestEntity();
        referenceEntity.setId(controllerService.getId());
        referenceEntity.setState(running ? NifiProcessUtil.PROCESS_STATE.RUNNING.name() : NifiProcessUtil.PROCESS_STATE.STOPPED.name());
        referenceEntity.setReferencingComponentRevisions(referencingRevisions);

        try {
            updateReferences(controllerService.getId(), referenceEntity);
        } catch (final Exception e) {
            log.error("Failed to update referencing schedulable components for controller service {} [{}]: {}", controllerService.getId(), controllerService.getName(), e, e);
            return false;
        }

        // Wait for states to change
        if (running) {
            return true;
        } else {
            return stopReferencingSchedulableComponents(controllerService, 5, 500, TimeUnit.MILLISECONDS);
        }
    }

    private boolean startSchedulableComponents(@Nonnull final Set<ControllerServiceReferencingComponentEntity> entities) {
        boolean updated = true;

        // Update references back to previous states
        for (final ControllerServiceReferencingComponentEntity entity : entities) {
            final ControllerServiceReferencingComponentDTO component = entity.getComponent();

            if (StringUtils.equals(component.getReferenceType(), REFERENCE_TYPE_PROCESSOR)) {
                final ProcessorDTO processorDetails = new ProcessorDTO();
                processorDetails.setId(component.getId());
                processorDetails.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());

                final ProcessorEntity processorEntity = new ProcessorEntity();
                processorEntity.setId(entity.getId());
                processorEntity.setRevision(entity.getRevision());
                processorEntity.setComponent(processorDetails);

                try {
                    getClient().processors().updateWithRetry(processorEntity, 3, 500, TimeUnit.MILLISECONDS);
                } catch (final Exception e) {
                    updated = false;
                    log.warn("Failed to start processor {} [{}]: {}", component.getId(), component.getName(), e);
                }
            } else if (StringUtils.equals(component.getReferenceType(), REFERENCE_TYPE_REPORTING_TASK)) {
                final ReportingTaskDTO reportingTaskDetails = new ReportingTaskDTO();
                reportingTaskDetails.setId(component.getId());
                reportingTaskDetails.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());

                final ReportingTaskEntity reportingTaskEntity = new ReportingTaskEntity();
                reportingTaskEntity.setId(entity.getId());
                reportingTaskEntity.setRevision(entity.getRevision());
                reportingTaskEntity.setComponent(reportingTaskDetails);

                try {
                    getClient().reportingTasks().update(reportingTaskDetails);
                } catch (final Exception e) {
                    updated = false;
                    log.warn("Failed to start reporting task {} [{}]: {}", component.getId(), component.getName(), e);
                }
            } else {
                log.warn("Cannot start entity {} of unknown type: {}", entity.getId(), component.getReferenceType());
            }
        }

        return updated;
    }

    /**
     * Updates the referencing services with the specified state.
     *
     * @param controllerService    controller service
     * @param referencingRevisions revisions of referencing services
     * @param enabled              {@code true} to enable services, or {@code false} to disable
     * @return {@code true} if all services were updated successfully
     */
    private boolean updateReferencingServices(@Nonnull final ControllerServiceDTO controllerService, @Nonnull final Map<String, RevisionDTO> referencingRevisions, final boolean enabled) {
        // Issue request to update referencing components
        final UpdateControllerServiceReferenceRequestEntity referenceEntity = new UpdateControllerServiceReferenceRequestEntity();
        referenceEntity.setId(controllerService.getId());
        referenceEntity.setState(enabled ? State.ENABLED.name() : State.DISABLED.name());
        referenceEntity.setReferencingComponentRevisions(referencingRevisions);

        try {
            updateReferences(controllerService.getId(), referenceEntity);
        } catch (final Exception e) {
            log.error("Failed to update referencing services for controller service {} [{}]: {}", controllerService.getId(), controllerService.getName(), e, e);
            return false;
        }

        // Wait for states to change
        final Predicate<ControllerServiceReferencingComponentEntity> predicate;
        if (enabled) {
            predicate = entity -> StringUtils.equalsAny(entity.getComponent().getState(), "ENABLING", State.ENABLED.name());
        } else {
            predicate = entity -> StringUtils.equals(entity.getComponent().getState(), State.DISABLED.name());
        }

        final Predicate<String> typeFilter = REFERENCE_TYPE_CONTROLLER_SERVICE::equals;
        final Set<ControllerServiceReferencingComponentEntity> failed = pollReferencingComponents(controllerService.getId(), typeFilter, predicate, 5, 500, TimeUnit.MILLISECONDS);
        if (failed.isEmpty()) {
            return true;
        } else {
            if (log.isErrorEnabled()) {
                final String referenceIds = failed.stream().map(ControllerServiceReferencingComponentEntity::getId).collect(Collectors.joining(", "));
                log.error("Failed to stop referencing services for controller service {} [{}]: {}", controllerService.getId(), controllerService.getName(), referenceIds);
            }
            return false;
        }
    }

    /**
     * Polls the specified controller service until all referencing schedulable components are stopped (not scheduled and 0 active threads).
     */
    private boolean stopReferencingSchedulableComponents(@Nonnull final ControllerServiceDTO controllerService, final int retries, final int timeout, @Nonnull final TimeUnit timeUnit) {
        final Predicate<String> typeFilter = type -> StringUtils.equalsAny(type, REFERENCE_TYPE_PROCESSOR, REFERENCE_TYPE_REPORTING_TASK);
        final Predicate<ControllerServiceReferencingComponentEntity> predicate = entity -> {
            final ControllerServiceReferencingComponentDTO component = entity.getComponent();
            return !StringUtils.equals(component.getState(), NifiProcessUtil.PROCESS_STATE.RUNNING.name())
                   && (component.getActiveThreadCount() == null || component.getActiveThreadCount() == 0);
        };
        final Set<ControllerServiceReferencingComponentEntity> running = pollReferencingComponents(controllerService.getId(), typeFilter, predicate, retries, timeout, timeUnit);

        if (running.isEmpty()) {
            return true;
        } else {
            if (log.isErrorEnabled()) {
                final String referenceIds = running.stream().map(ControllerServiceReferencingComponentEntity::getId).collect(Collectors.joining(", "));
                log.error("Failed to stop referencing schedulable components for controller service {} [{}]: {}", controllerService.getId(), controllerService.getName(), referenceIds);
            }
            return false;
        }
    }
}
