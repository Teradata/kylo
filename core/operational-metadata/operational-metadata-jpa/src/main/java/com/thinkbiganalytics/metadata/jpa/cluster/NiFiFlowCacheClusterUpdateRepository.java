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

import com.thinkbiganalytics.metadata.api.cluster.NiFiFlowCacheClusterSync;
import com.thinkbiganalytics.metadata.api.cluster.NiFiFlowCacheClusterUpdateItem;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedProcessorStats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Spring data repository for {@link JpaNifiFeedProcessorStats}
 */
public interface NiFiFlowCacheClusterUpdateRepository extends JpaRepository<JpaNiFiFlowCacheClusterSync, JpaNiFiFlowCacheClusterSync.NiFiFlowCacheKey> {


    @Query(value = "select case when(count(cache)) > 0 then true else false end "
                + " from JpaNiFiFlowCacheClusterSync as cache "
                + "where cache.updateApplied = false "
                + "and cache.clusterAddress = :clusterAddress")
    public boolean needsUpdate(@Param("clusterAddress") String clusterAddress);


    @Query(value="select item from JpaNiFiFlowCacheClusterUpdateItem as item "
                 + "join JpaNiFiFlowCacheClusterSync as cache on item.updateKey = cache.updateKey "
                 + " where cache.clusterAddress = :clusterAddress and cache.updateApplied = false")
    public List<NiFiFlowCacheClusterUpdateItem> findUpdateItems(@Param("clusterAddress") String clusterAddress);


    @Query(value = "select clusterUpdate from JpaNiFiFlowCacheClusterSync clusterUpdate where clusterUpdate.clusterAddress = :clusterAddress and clusterUpdate.updateKey in (:updateKeys) and clusterUpdate.updateApplied = false")
    List<NiFiFlowCacheClusterSync> findUnAppliedUpdates(@Param("clusterAddress") String clusterAddress, @Param("updateKeys") List<String>updateKeys);

    @Query(value = "select clusterUpdate from JpaNiFiFlowCacheClusterSync clusterUpdate where clusterUpdate.clusterAddress not in(:clusterAddresses)")
    List<JpaNiFiFlowCacheClusterSync> findStaleClusterUpdates(@Param("clusterAddresses") List<String>clusterAddresses);



    @Query("select cache.updateKey from JpaNiFiFlowCacheClusterSync as cache where cache.clusterAddress not in(:clusterAddresses)")
    Set<String> findStaleClusterUpdateItemKeys(@Param("clusterAddresses") List<String>clusterAddresses);


    @Modifying
    @Query("DELETE FROM JpaNiFiFlowCacheClusterSync as cache "
           + "where cache.clusterAddress not in(:clusterAddresses)")
    void deleteStaleCacheClusterSync(@Param("clusterAddresses") List<String>clusterAddresses);

    @Modifying
    @Query("DELETE FROM JpaNiFiFlowCacheClusterUpdateItem as item "
            + "where item.updateKey in (:updateKeys)")
    void deleteStaleClusterUpdateItems(@Param("updateKeys") Set<String>updateKeys);
}
