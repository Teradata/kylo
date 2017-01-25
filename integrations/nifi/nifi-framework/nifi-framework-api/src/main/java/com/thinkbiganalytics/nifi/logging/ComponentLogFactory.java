package com.thinkbiganalytics.nifi.logging;

/*-
 * #%L
 * thinkbig-nifi-framework-api
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

import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessorInitializationContext;

import javax.annotation.Nonnull;

/**
 * Produces {@link ComponentLog} objects from various contexts.
 */
public interface ComponentLogFactory {

    /**
     * Returns a {@link ComponentLog} for the specified processor initialization context.
     *
     * @param context the processor initialization context
     * @return the component log
     */
    @Nonnull
    ComponentLog getLog(@Nonnull ProcessorInitializationContext context);
}
