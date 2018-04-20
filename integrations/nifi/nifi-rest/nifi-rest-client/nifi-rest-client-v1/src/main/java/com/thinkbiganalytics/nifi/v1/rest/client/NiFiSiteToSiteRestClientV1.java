package com.thinkbiganalytics.nifi.v1.rest.client;
/*-
 * #%L
 * thinkbig-nifi-rest-client-v1
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
import com.thinkbiganalytics.nifi.rest.client.NiFiSiteToSiteRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.entity.ControllerEntity;
import org.apache.nifi.web.api.entity.PeersEntity;
import org.apache.nifi.web.api.entity.PortEntity;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

public class NiFiSiteToSiteRestClientV1 implements NiFiSiteToSiteRestClient {

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV1 client;

    /**
     * Constructs a {@code NiFiPortsRestClientV1} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiSiteToSiteRestClientV1(@Nonnull final NiFiRestClientV1 client) {
        this.client = client;
    }

    @Override
    public ControllerEntity details() {
        return client.get("/site-to-site", null, ControllerEntity.class);

    }

    @Override
    public PeersEntity peers() {
        return client.get("/site-to-site/peers", null, PeersEntity.class);
    }
}
