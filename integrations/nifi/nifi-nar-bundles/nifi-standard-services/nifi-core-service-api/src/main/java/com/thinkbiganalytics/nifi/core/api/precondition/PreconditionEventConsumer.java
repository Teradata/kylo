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

/**
 * For adding listener's to precondition events
 */
public interface PreconditionEventConsumer {

    /**
     * Adds listener for acting on precondition events
     *
     * @param category The category of the feed
     * @param feedName The name of the feed
     * @param listener The listener to be notified of precondition events
     */
    void addListener(String category, String feedName, PreconditionListener listener);

    /**
     * Removes the listener on precondition events
     *
     * @param listener The listener to be removed
     */
    void removeListener(PreconditionListener listener);
}
