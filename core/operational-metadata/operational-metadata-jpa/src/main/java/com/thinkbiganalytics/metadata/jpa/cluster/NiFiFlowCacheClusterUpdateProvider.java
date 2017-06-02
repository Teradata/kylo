package com.thinkbiganalytics.metadata.jpa.cluster;
/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.NiFiFlowCacheUpdateType;
import com.thinkbiganalytics.metadata.api.cluster.NiFiFlowCacheClusterSync;
import com.thinkbiganalytics.metadata.api.cluster.NiFiFlowCacheClusterUpdateItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

@Service
public class NiFiFlowCacheClusterUpdateProvider {

    private static final Logger log = LoggerFactory.getLogger(NiFiFlowCacheClusterUpdateProvider.class);

    @Inject
    private ClusterService clusterService;

    private NiFiFlowCacheClusterUpdateRepository niFiFlowCacheRepository;

    private NiFiFlowCacheClusterUpdateItemRepository niFiFlowCacheUpdateItemRepository;

    @Autowired
    public NiFiFlowCacheClusterUpdateProvider(NiFiFlowCacheClusterUpdateRepository niFiFlowCacheRepository, NiFiFlowCacheClusterUpdateItemRepository niFiFlowCacheUpdateItemRepository) {
        this.niFiFlowCacheRepository = niFiFlowCacheRepository;
        this.niFiFlowCacheUpdateItemRepository = niFiFlowCacheUpdateItemRepository;
    }

    /**
     * Insert a record to notify other clusters it needs to be synchronized
     * @param type the type of update
     * @param message the json string for the update
     * @return the object representing the sync
     */
    public NiFiFlowCacheClusterSync updatedCache(NiFiFlowCacheUpdateType type, String message) {
        JpaNiFiFlowCacheClusterUpdateItem updateItem = new JpaNiFiFlowCacheClusterUpdateItem();
        updateItem.setUpdateType(type);
        updateItem.setUpdateValue(message);
        updateItem = niFiFlowCacheUpdateItemRepository.save(updateItem);
        String clusterAddress = clusterService.getAddressAsString();
        NiFiFlowCacheClusterSync thisClusterUpdate = null;
        for (String address : clusterService.getMembersAsString()) {
            boolean thisCluster = address.equalsIgnoreCase(clusterAddress);
            JpaNiFiFlowCacheClusterSync cache = new JpaNiFiFlowCacheClusterSync(address, updateItem.getUpdateKey(), thisCluster ? true : false);
            cache.setInitiatingCluster(thisCluster);
            cache = niFiFlowCacheRepository.save(cache);
            if(thisCluster) {
             thisClusterUpdate = cache;
            }
        }
        return thisClusterUpdate;
    }

    /**
     * Find any updates that have not been applied for this cluster
     * @return the list of updates needed
     */
    public List<NiFiFlowCacheClusterUpdateItem> findUpdates(){
        return niFiFlowCacheRepository.findUpdateItems(clusterService.getAddressAsString());
    }

    /**
     * Does this cluster need its cache updated
     * @return true if cache needs updating, false if not
     */
    public boolean needsUpdate(){
        return niFiFlowCacheRepository.needsUpdate(clusterService.getAddressAsString());
    }

    /**
     * Mark the updateKeys as applied for this cluster
     * @param updateKeys the keys that have been updated
     */
    public void appliedUpdates(List<String> updateKeys){
        String clusterAddress = clusterService.getAddressAsString();
        List<NiFiFlowCacheClusterSync> unappliedUpdates = niFiFlowCacheRepository.findUnAppliedUpdates(clusterAddress, updateKeys);
        if(unappliedUpdates != null){
            List<JpaNiFiFlowCacheClusterSync> appliedUpdates = new ArrayList<>();
           for(NiFiFlowCacheClusterSync update: unappliedUpdates) {
               JpaNiFiFlowCacheClusterSync u = (JpaNiFiFlowCacheClusterSync) update;
               u.setUpdateApplied(true);
               u.setModifiedTime(DateTimeUtil.getNowUTCTime());
               appliedUpdates.add(u);
           }
           niFiFlowCacheRepository.save(appliedUpdates);
        }
    }

    /**
     * Removes all cluster records that are not part of the current cluster members
     */
    public void resetClusterSyncUpdates(){

        List<String> members = clusterService.getMembersAsString();
        if(members != null && !members.isEmpty()) {
            Set<String> updateKeys = niFiFlowCacheRepository.findStaleClusterUpdateItemKeys(members);
            niFiFlowCacheRepository.deleteStaleCacheClusterSync(members);
            if(updateKeys != null && !updateKeys.isEmpty()) {
                niFiFlowCacheRepository.deleteStaleClusterUpdateItems(updateKeys);
            }
        }

    }

}
