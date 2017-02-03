package com.thinkbiganalytics.nifi.core.api.precondition;

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

import com.thinkbiganalytics.metadata.rest.model.event.FeedPreconditionTriggerEvent;

/**
 * A listener which gets called for precondition events
 */
public interface PreconditionListener {

    /**
     * method is called by the service we registered with, when new precondition events arrive.
     *
     * @param event The event that occurred
     */
    void triggered(FeedPreconditionTriggerEvent event);
}
