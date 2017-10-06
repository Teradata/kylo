package com.thinkbiganalytics.metadata.cache;
/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.servicemonitor.ServiceMonitorRepository;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Created by sr186054 on 9/27/17.
 */
public class ServiceStatusCache implements TimeBasedCache<ServiceStatusResponse> {

    @Inject
    private ServiceMonitorRepository serviceRepository;

    LoadingCache<Long, List<ServiceStatusResponse>> serviceStatusCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build(new CacheLoader<Long, List<ServiceStatusResponse>>() {
        @Override
        public List<ServiceStatusResponse> load(Long millis) throws Exception {
            return serviceRepository.listServices();
        }
    });

    public List<ServiceStatusResponse> getServiceStatus(Long time) {
        return serviceStatusCache.getUnchecked(time);
    }

    @Override
    public List<ServiceStatusResponse> getCache(Long time) {
        return getServiceStatus(time);
    }

    @Override
    public List<ServiceStatusResponse> getUserCache(Long time) {
        return getServiceStatus(time);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
