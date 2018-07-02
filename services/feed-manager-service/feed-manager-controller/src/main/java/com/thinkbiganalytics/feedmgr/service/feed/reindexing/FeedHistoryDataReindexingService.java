/*-
 * #%L
 * thinkbig-ui-feed-manager
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
package com.thinkbiganalytics.feedmgr.service.feed.reindexing;

import com.thinkbiganalytics.feedmgr.config.HistoryDataReindexingFeedsAvailableCache;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedStats;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiFeedStatisticsRepository;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.HistoryReindexingState;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;

import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Set;

import javax.inject.Inject;

/**
 * Service to support data history reindexing
 */
@Component
public class FeedHistoryDataReindexingService {

    private static final Logger log = LoggerFactory.getLogger(FeedHistoryDataReindexingService.class);
    private static final String DISTRIBUTED_MAP_CACHE_SERVER_CONTROLLER_SERVICE_TYPE = "org.apache.nifi.distributed.cache.server.map.DistributedMapCacheServer";

    @org.springframework.beans.factory.annotation.Value("${search.history.data.reindexing.enabled:true}")
    private boolean historyDataReindexingEnabled;

    @Inject
    private NifiFeedStatisticsRepository nifiFeedStatisticsRepository;

    @Inject
    private HistoryDataReindexingFeedsAvailableCache historyDataReindexingFeedsAvailableCache;

    @Inject
    private NiFiRestClient niFiRestClient;

    @Inject
    private Environment environment;

    public FeedHistoryDataReindexingService() {

    }

    public boolean isHistoryDataReindexingEnabled() {
        return historyDataReindexingEnabled;
    }

    private boolean areHistoryDataReindexingFeedsAvailable() {
        return historyDataReindexingFeedsAvailableCache.areKyloHistoryDataReindexingFeedsAvailable();
    }

    public void setHistoryDataReindexingEnabled(boolean historyDataReindexingEnabled) {
        this.historyDataReindexingEnabled = historyDataReindexingEnabled;
    }

    public boolean isFeedRunning(FeedMetadata feedMetadata) {
        JpaNifiFeedStats latestFeedStatus = nifiFeedStatisticsRepository.findLatestForFeedWithoutAcl(feedMetadata.getCategoryAndFeedName());
        if (latestFeedStatus != null) {
            return latestFeedStatus.isFeedRunning();
        }
        return false;
    }

    //Accept feed data history reindexing request only if:
    // (1) enabled via Kylo configuration
    // (2) feeds supporting history data reindexing are available
    // (3) feed is not currently running
    public void checkAndEnsureFeedHistoryDataReindexingRequestIsAcceptable(FeedMetadata feedMetadata) {
        if (!feedMetadata.getHistoryReindexingStatus().equals(HistoryReindexingState.DIRTY.toString())) {
            return;
        }

        if (!isHistoryDataReindexingEnabled()) {
            throw new FeedHistoryDataReindexingNotEnabledException();
        }

        if (!areHistoryDataReindexingFeedsAvailable()) {
            throw new FeedHistoryDataReindexingFeedsNotAvailableException();
        }

        if (isFeedRunning(feedMetadata)) {
            throw new FeedCurrentlyRunningException(feedMetadata.getCategoryName(), feedMetadata.getFeedName());
        }
    }

    public void updateHistoryDataReindexingFeedsAvailableCache(FeedMetadata feedMetadata) {
        historyDataReindexingFeedsAvailableCache.updateCache(feedMetadata.getCategory().getSystemName(), feedMetadata.getSystemFeedName());
    }

    public void updateHistoryDataReindexingFeedsAvailableCache(String categorySystemName, String feedSystemName) {
        historyDataReindexingFeedsAvailableCache.updateCache(categorySystemName, feedSystemName);
    }

    public void checkAndConfigureNiFi(FeedMetadata feedMetadata) {
        if (checkIfFeedIsRelatedToIndexing(feedMetadata)) {
            if (kyloHistoryReindexSupportPropertyAvailable(feedMetadata)) {
                log.info("Regular/history index feed detected, with history support");
                checkForNiFiVersionSupport();
                checkAndCreateDistributedMapCacheServerControllerService();
            } else {
                log.debug("Regular/history index feed detected, but not marked with history support.");
            }
        }
    }

    private boolean checkIfFeedIsRelatedToIndexing(FeedMetadata feedMetadata) {
        return (checkIfFeedIsRegularIndexFeed(feedMetadata) || checkIfFeedIsHistoryReindexFeed(feedMetadata));
    }

    private boolean checkIfFeedIsRegularIndexFeed(FeedMetadata feedMetadata) {
        return (
            (historyDataReindexingFeedsAvailableCache.getHistoryDataReindexingRegularFeedCategory().equals(feedMetadata.getSystemCategoryName()))
            &&
            (historyDataReindexingFeedsAvailableCache.getHistoryDataReindexingRegularFeedSystemName().equals(feedMetadata.getSystemFeedName()))
        );
    }

    private boolean checkIfFeedIsHistoryReindexFeed(FeedMetadata feedMetadata) {
        return (
            (historyDataReindexingFeedsAvailableCache.getHistoryDataReindexingReindexFeedCategory().equals(feedMetadata.getSystemCategoryName()))
            &&
            (historyDataReindexingFeedsAvailableCache.getHistoryDataReindexingReindexFeedSystemName().equals(feedMetadata.getSystemFeedName()))
        );
    }

    private boolean kyloHistoryReindexSupportPropertyAvailable(FeedMetadata feedMetadata) {
        for (UserProperty userProperty : feedMetadata.getUserProperties()) {
            if (userProperty.getSystemName().equals(HistoryDataReindexingFeedsAvailableCache.REINDEX_SUPPORT_PROPERTY_NAME_FOR_CHECK)
                && userProperty.getValue().equals(HistoryDataReindexingFeedsAvailableCache.REINDEX_SUPPORT_PROPERTY_VALUE_FOR_CHECK)) {
                log.debug("History reindex support property available");
                return true;
            }
        }
        return false;
    }

    private void checkForNiFiVersionSupport() {
        if (environment == null) {
            throw new NiFiConfigurationForHistoryDataReindexingException(
                "Unable to detect environment options required to support history data reindexing. Please check Kylo services properties, and attempt restarting Kylo.");
        }

        String[] activeKyloProfiles = environment.getActiveProfiles();
        for (String activeKyloProfile : activeKyloProfiles) {
            if ((activeKyloProfile.equals("nifi-v1")) || (activeKyloProfile.equals("nifi-v1.1"))) {
                log.error("Skipping attempt to create distributed map cache controller service (not supported with configured version [{}] of NiFi)", activeKyloProfile);
                throw new NiFiConfigurationForHistoryDataReindexingException("History data reindexing not supported with configured version of NiFi: " + activeKyloProfile);
            }
        }
    }

    private void checkAndCreateDistributedMapCacheServerControllerService() {
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
                log.warn("Distributed Map Cache Server Controller Service in NiFi with id {} detected in {} state in NiFi. Verify that it has enabled successfully in NiFi.",
                         existingOrNewControllerServiceId, lastControllerServiceState);
            }
        }
    }
}
