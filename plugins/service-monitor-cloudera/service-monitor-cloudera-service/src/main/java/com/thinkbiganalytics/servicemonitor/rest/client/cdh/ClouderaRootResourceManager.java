package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

/*-
 * #%L
 * thinkbig-service-monitor-cloudera
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

import com.cloudera.api.ApiRootResource;
import com.cloudera.api.v1.RootResourceV1;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cloudera resource manager which first attempts to get configured Cloudera API version and if that fails it
 * falls back to first API version.
 */
class ClouderaRootResourceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ClouderaRootResourceManager.class);


    /**
     * Will attempt to get configured api version. If that fails it will fall back to API v1.
     */
    static ClouderaRootResource getRootResource(ApiRootResource apiRootResource) {

        String version = apiRootResource.getCurrentVersion();
        Integer numericVersion = new Integer(StringUtils.substringAfter(version, "v"));
        RootResourceV1 rootResource = null;
        Integer maxVersion = 10;
        if (numericVersion > maxVersion) {
            numericVersion = maxVersion;
        }

        try {
            rootResource = (RootResourceV1) MethodUtils.invokeMethod(apiRootResource, "getRootV" + numericVersion);
        } catch (Exception ignored) {

        }
        if (rootResource == null) {
            LOG.info("Unable to get RootResource for version {}, returning version 1", numericVersion);
            rootResource = apiRootResource.getRootV1();
        }

        return new DefaultClouderaRootResource(rootResource);


    }


}
