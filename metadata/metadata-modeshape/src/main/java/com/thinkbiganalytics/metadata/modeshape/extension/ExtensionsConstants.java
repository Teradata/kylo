/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

/**
 *
 * @author Sean Felten
 */
public interface ExtensionsConstants {
    
    static final Set<String> STD_PREFIXES = Collections.unmodifiableSet(Sets.newHashSet("jcr", "nt", "mix"));
    
    static final Pattern NAME_PATTERN = Pattern.compile("^(\\w*):(.*)");

    static final String EXTENSIONS = "metadata/extensions";
    static final String TYPES = EXTENSIONS + "/types";
    static final String ENTITIES = EXTENSIONS + "/entities";

    static final String EXTENSIBLE_ENTITY_TYPE = "tba:extensibleEntity";
    static final String TYPE_DESCRIPTOR_TYPE = "tba:typeDescriptor";
    static final String FIELD_DESCRIPTOR_TYPE = "tba:fieldDescriptor";


}
