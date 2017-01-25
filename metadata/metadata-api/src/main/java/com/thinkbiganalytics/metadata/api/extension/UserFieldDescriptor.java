package com.thinkbiganalytics.metadata.api.extension;

/*-
 * #%L
 * thinkbig-metadata-api
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
import javax.annotation.Nullable;

/**
 * A user-defined field on categories or feeds.
 */
public interface UserFieldDescriptor {

    /**
     * Gets a human-readable specification for this field.
     *
     * @return a human-readable specification
     */
    @Nullable
    String getDescription();

    /**
     * Gets a human-readable title for this field.
     *
     * @return a human-readable title
     */
    @Nullable
    String getDisplayName();

    /**
     * Gets the index of the display order for this field.
     *
     * <p>The first property will have a value of 0, with later fields increasing in value.</p>
     *
     * @return the display order index
     */
    int getOrder();

    /**
     * Indicates that this field is required.
     *
     * @return {@code true} if this field is required; {@code false} or {@code null} otherwise
     */
    boolean isRequired();

    /**
     * Gets the internal identifier for this field.
     *
     * @return the internal identifier
     */
    @Nonnull
    String getSystemName();
}
