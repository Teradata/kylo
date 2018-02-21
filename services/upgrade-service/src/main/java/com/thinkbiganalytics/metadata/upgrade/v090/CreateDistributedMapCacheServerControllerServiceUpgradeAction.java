package com.thinkbiganalytics.metadata.upgrade.v090;

/*-
 * #%L
 * kylo-upgrade-service
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Set;

import javax.inject.Inject;

import static com.thinkbiganalytics.server.upgrade.KyloUpgrader.KYLO_UPGRADE;

/**
 * Upgrade action to support reindexing
 * Ensure that Distributed Map Cache Server Controller Service is available
 */
@Component("createDistributedMapCacheServerControllerServiceUpgradeAction90")
@Order(Ordered.LOWEST_PRECEDENCE - 99)
@Profile({KYLO_UPGRADE})
public class CreateDistributedMapCacheServerControllerServiceUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(CreateDistributedMapCacheServerControllerServiceUpgradeAction.class);

    private static final String DISTRIBUTED_MAP_CACHE_SERVER_CONTROLLER_SERVICE_TYPE = "org.apache.nifi.distributed.cache.server.map.DistributedMapCacheServer";

    @Inject
    private NiFiRestClient niFiRestClient;

    @Inject
    Environment environment;

    @Override
    public boolean isTargetVersion(KyloVersion version) {

        if (environment == null) {
            return false;
        }

        String[] activeKyloProfiles = environment.getActiveProfiles();
        for (String activeKyloProfile : activeKyloProfiles) {
            if ((activeKyloProfile.equals("nifi-v1")) || (activeKyloProfile.equals("nifi-v1.1"))) {
                log.info("Skipping upgrade action to create distributed map cache controller service (not supported with configured version [{}] of NiFi)", activeKyloProfile);
                return false;
            }
        }
        return version.matches("0.9", "0", "");
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("****************** Start: Create Distributed Map Cache Server Controller Service Upgrade Action ****************");

        boolean doesControllerServiceExist = false;
        String existingOrNewControllerServiceId = "";
        String stateOfExistingOrNewControllerService = "DISABLED";

        //Check if controller service already exists
        log.info("Checking if controller service already exists.");

        Set<ControllerServiceDTO> existingControllerServicesInNiFi = niFiRestClient.processGroups().getControllerServices("root");
        for (ControllerServiceDTO existingControllerService : existingControllerServicesInNiFi) {
            if (existingControllerService.getType().equals(DISTRIBUTED_MAP_CACHE_SERVER_CONTROLLER_SERVICE_TYPE)) {
                doesControllerServiceExist = true;
                existingOrNewControllerServiceId = existingControllerService.getId();
                stateOfExistingOrNewControllerService = existingControllerService.getState();
                log.info("Existing controller service found with id: {}, in state: {}", existingOrNewControllerServiceId, stateOfExistingOrNewControllerService);
                break;
            }
        }

        if (!doesControllerServiceExist) {
            log.info("Did not find existing controller service. Creating it.");
            ControllerServiceDTO distributedMapCacheServerControllerService = new ControllerServiceDTO();
            distributedMapCacheServerControllerService.setType(DISTRIBUTED_MAP_CACHE_SERVER_CONTROLLER_SERVICE_TYPE);
            ControllerServiceDTO createdDistributedMapCacheServerControllerService = niFiRestClient.controllerServices().create(distributedMapCacheServerControllerService);
            existingOrNewControllerServiceId = createdDistributedMapCacheServerControllerService.getId();
            stateOfExistingOrNewControllerService = createdDistributedMapCacheServerControllerService.getState();
            log.info("Created controller service with id: {}, in state: {}", existingOrNewControllerServiceId, stateOfExistingOrNewControllerService);
        }

        if (!stateOfExistingOrNewControllerService.equals("ENABLED")) {
            int numRetries = 1; //rest client throws exception if trying to enable an already enabled service, hence trying only 1 time
            int retryCount = 0;
            boolean serviceEnabled = false;
            String lastControllerServiceState = stateOfExistingOrNewControllerService;
            while ((retryCount < numRetries) && (!serviceEnabled)) {
                log.info("Enabling controller service with id: {}", existingOrNewControllerServiceId);
                ControllerServiceDTO controllerServiceDTO = new ControllerServiceDTO();
                controllerServiceDTO.setId(existingOrNewControllerServiceId);
                controllerServiceDTO.setState("ENABLED");
                ControllerServiceDTO updatedControllerServiceDTO = niFiRestClient.controllerServices().update(controllerServiceDTO);
                retryCount++;
                lastControllerServiceState = updatedControllerServiceDTO.getState();
                log.info("Updated status controller service with id: {} is: {}", updatedControllerServiceDTO.getId(), lastControllerServiceState);
                if (!lastControllerServiceState.equals("ENABLED")) {
                    log.debug("Tried {} time/s to enable controller service, it is still in {} state in NiFi. Will try for total {} times.", retryCount, lastControllerServiceState,
                             numRetries);
                    serviceEnabled = false;
                } else {
                    log.info("Controller service detected as enabled in NiFi.");
                    serviceEnabled = true;
                }
            }
            if (!serviceEnabled) {
                log.warn("Distributed Map Cache Server Controller Service in NiFi with id {} detected in {} state in NiFi. Verify that it has enabled successfully in NiFi.", existingOrNewControllerServiceId, lastControllerServiceState);
            }
        }
        log.info("****************** End: Create Distributed Map Cache Server Controller Service Upgrade Action ****************");
    }
}
