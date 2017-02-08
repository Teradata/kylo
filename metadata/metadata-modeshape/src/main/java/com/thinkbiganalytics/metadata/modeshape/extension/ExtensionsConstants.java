package com.thinkbiganalytics.metadata.modeshape.extension;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Constants for extensible entities and types.
 */
public interface ExtensionsConstants {

    Set<String> STD_PREFIXES = Collections.unmodifiableSet(Sets.newHashSet("jcr", "nt", "mix"));

    Pattern NAME_PATTERN = Pattern.compile("^(\\w*):(.*)");

    String EXTENSIONS = "metadata/extensions";
    String TYPES = EXTENSIONS + "/types";
    String ENTITIES = EXTENSIONS + "/entities";

    String EXTENSIBLE_ENTITY_TYPE = "tba:extensibleEntity";
    String TYPE_DESCRIPTOR_TYPE = "tba:typeDescriptor";
    String FIELD_DESCRIPTOR_TYPE = "tba:fieldDescriptor";

    /**
     * Name of the extensible type for all categories
     */
    String USER_CATEGORY = "usr:category";

    /**
     * Name of the extensible type for all feeds
     */
    String USER_FEED = "usr:feed";

    /**
     * Gets the name of the extensible type for all feeds within the specified category.
     *
     * @param categorySystemName the category's system name
     * @return the extensible type
     */
    static String getUserCategoryFeed(@Nonnull final String categorySystemName) {
        return "usr:category:" + categorySystemName + ":feed";
    }
}
