package com.thinkbiganalytics.nifi.core.api.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service-api
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

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

/**
 */
@Tags({"thinkbig", "metadata", "client", "feed", "dataset", "operation"})
@CapabilityDescription("Exposes the metadata providers to access and manipulate metadata related to "
                       + "feeds, datasets, and data operations.")
public interface MetadataProviderService extends ControllerService {

    /**
     * gets a provider for working with metadata
     *
     * @return the metadata provider
     */
    MetadataProvider getProvider();

    /**
     * get a meta data recorder for holding metadata that will eventually be persisted to the meta data store
     *
     * @return the metadata recorder
     */
    MetadataRecorder getRecorder();

    /**
     * get the nifi flow provider used for interacting with nifi flow events
     *
     * @return the nifi flow provider
     */
    KyloNiFiFlowProvider getKyloNiFiFlowProvider();
}
