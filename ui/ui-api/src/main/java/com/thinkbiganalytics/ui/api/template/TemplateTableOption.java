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
     * Gets the template URL containing any pre steps for creating a feed
     * Pre steps are rendered before the General Info step.
     * These are useful if you want to create a form to determine if a user should continue and create the feed or not
     */
    @Nullable
    default String getPreStepperTemplateUrl() {
        return null;
    }

    default String getPreFeedDetailsTemplateUrl() { return null;}

    /**
     * The Total Number of PreSteps
     * @return
     */
    default int getTotalPreSteps() { return 0;}

    /**
     * Gets the number of additional steps for creating or editing a feed.
     * This needs to include all the presteps
     */
    default int getTotalSteps() {
        return 0;
    }

    /**
     * Gets a script location for the initialize js
     * This will get called both for Creating and Editing a feed to let you preset model defaults or enable/disable sections
     * @return
     */
    default String getInitializeScript() {return null;}


    /**
     * The name of the angular service used in the initialize Script
     * This is the service name that is part of the getInitializeScript()
     * @return
     */
    default String getInitializeServiceName() { return null;}

    /**
     * Gets a unique identifier for this option.
     */
    @Nonnull
    String getType();

    /**
     * Gets the resource context for the files
     * @return
     */
    default String getResourceContext() { return null;};


}
