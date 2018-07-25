/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

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
