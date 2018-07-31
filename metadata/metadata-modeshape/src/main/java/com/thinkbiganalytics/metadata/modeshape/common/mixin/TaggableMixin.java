/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.metadata.api.Taggable;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;

import java.util.Set;

/**
 *
 */
public interface TaggableMixin extends WrappedNodeMixin, Taggable {

    default Set<String> addTag(String tag) {
        Set<String> tags = getTags();
        tags.add(tag);
        setTags(tags);
        return tags;
    }
    
    default Set<String> removeTag(String tag) {
        Set<String> tags = getTags();
        tags.remove(tag);
        setTags(tags);
        return tags;
    }

    default boolean hasTag(String tag) {
        Set<String> tags = getPropertyAsSet(JcrPropertyConstants.TAGGABLE, String.class);
        return tags.contains(tag);
    }

    default Set<String> getTags() {
        return getPropertyAsSet(JcrPropertyConstants.TAGGABLE, String.class);
    }

    default void setTags(Set<String> tags) {
        setProperty(JcrPropertyConstants.TAGGABLE, tags);
    }

}
