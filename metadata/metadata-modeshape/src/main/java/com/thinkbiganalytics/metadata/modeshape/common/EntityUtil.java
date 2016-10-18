package com.thinkbiganalytics.metadata.modeshape.common;


import org.apache.commons.lang3.StringUtils;

import com.thinkbiganalytics.metadata.modeshape.extension.ExtensionsConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 * Utility to get basic Paths for schema
 */
public class EntityUtil {

    public static String pathForCategory() {
        return JcrUtil.path("/metadata", "feeds").toString();
    }

    public static String pathForCategory(String categorySystemName) {
        return JcrUtil.path("/metadata", "feeds", categorySystemName).toString();
    }

    public static String pathForFeed(String categorySystemName, String feedSystemName) {
        return JcrUtil.path("/metadata", "feeds", categorySystemName, feedSystemName).toString();
    }

    public static String pathForFeedSource(String categorySystemName, String feedSystemName) {
        return JcrUtil.path(pathForFeed(categorySystemName, feedSystemName), "feedSource").toString();
    }

    public static String pathForFeedDestination(String categorySystemName, String feedSystemName) {
        return JcrUtil.path(pathForFeed(categorySystemName, feedSystemName), "feedDestination").toString();
    }

    public static String pathForDataSource() {
        return JcrUtil.path("/metadata", "datasources").toString();
    }

    public static String pathForTemplates() {
        return JcrUtil.path("/metadata", "templates").toString();
    }

    public static String pathForHadoopSecurityGroups() {
        return JcrUtil.path("/metadata", "hadoopSecurityGroups").toString();
    }


    public static String pathForAccessibleFunctions() {
        return JcrUtil.path("/metadata", "accessibleFunctions").toString();
    }

    public static String pathForExtensibleEntity() {
        return EntityUtil.pathForExtensibleEntity(null);
    }

    public static String pathForExtensibleEntity(String typeName) {
        if (StringUtils.isNotBlank(typeName)) {
            return JcrUtil.path("/", ExtensionsConstants.ENTITIES, typeName).toString();
        } else {
            return JcrUtil.path("/", ExtensionsConstants.ENTITIES).toString();
        }
    }

    public static String asQueryProperty(String prop) {
        return "[" + prop + "]";
    }

    /**
     * Instances of {@code EntityUtil} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private EntityUtil() {
        throw new UnsupportedOperationException();
    }
}








