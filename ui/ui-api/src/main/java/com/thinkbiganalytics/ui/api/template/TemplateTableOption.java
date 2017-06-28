package com.thinkbiganalytics.ui.api.template;

/*-
 * #%L
 * kylo-ui-api
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

import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;
import com.thinkbiganalytics.metadata.MetadataField;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Extends the options available when creating or editing a feed.
 */
@SuppressWarnings("unused")
public interface TemplateTableOption {

    /**
     * Gets a human-readable summary of this option.
     *
     * <p>This is displayed as the hint when registering a template.</p>
     */
    @Nonnull
    String getDescription();

    /**
     * Gets a human-readable title of this option.
     *
     * <p>This is displayed as the name of this option when registering a template.</p>
     */
    @Nonnull
    String getDisplayName();

    /**
     * Gets the template URL containing sections for viewing or editing a feed.
     */
    @Nullable
    default String getFeedDetailsTemplateUrl() {
        return null;
    }

    /**
     * Gets the list of metadata properties that can be used in NiFi property expressions.
     */
    @Nonnull
    default List<AnnotatedFieldProperty<MetadataField>> getMetadataProperties() {
        return Collections.emptyList();
    }

    /**
     * Gets the template URL containing steps for creating or editing a feed.
     */
    @Nullable
    default String getStepperTemplateUrl() {
        return null;
    }

    /**
     * Gets the number of additional steps for creating or editing a feed.
     */
    default int getTotalSteps() {
        return 0;
    }

    /**
     * Gets a unique identifier for this option.
     */
    @Nonnull
    String getType();
}
