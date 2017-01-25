package com.thinkbiganalytics.nifi.core.api.cleanup;

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

import javax.annotation.Nonnull;

/**
 * Event bus for cleanup events.
 */
public interface CleanupEventConsumer {

    /**
     * Adds the specified listener for cleanup events.
     *
     * @param category the category system name
     * @param feedName the feed system name
     * @param listener the listener to be added
     */
    void addListener(@Nonnull String category, @Nonnull String feedName, @Nonnull CleanupListener listener);

    /**
     * Removes the specified listener.
     *
     * @param listener the listener to be removed
     */
    void removeListener(@Nonnull CleanupListener listener);
}
