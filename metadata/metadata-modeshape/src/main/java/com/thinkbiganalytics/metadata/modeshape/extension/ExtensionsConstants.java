package com.thinkbiganalytics.metadata.modeshape.extension;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.google.common.collect.Sets;

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

    /** Name of the extensible type for all categories */
    String USER_CATEGORY = "usr:category";

    /** Name of the extensible type for all feeds */
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
