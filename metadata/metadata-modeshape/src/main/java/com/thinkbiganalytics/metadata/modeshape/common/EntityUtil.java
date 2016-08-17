package com.thinkbiganalytics.metadata.modeshape.common;


import com.thinkbiganalytics.metadata.modeshape.extension.ExtensionsConstants;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;

/**
 * Utility to get basic Paths for schema
 */
public class EntityUtil {

    public static String pathForCategory() {
        return Paths.get("/metadata", "feeds").toString();
    }

    public static String pathForCategory(String categorySystemName) {
        return Paths.get("/metadata", "feeds", categorySystemName).toString();
    }

    public static String pathForFeed(String categorySystemName, String feedSystemName) {
        return Paths.get("/metadata", "feeds", categorySystemName, feedSystemName).toString();
    }

    public static String pathForFeedSource(String categorySystemName, String feedSystemName) {
        return Paths.get(pathForFeed(categorySystemName, feedSystemName), "feedSource").toString();
    }

    public static String pathForFeedDestination(String categorySystemName, String feedSystemName) {
        return Paths.get(pathForFeed(categorySystemName, feedSystemName), "feedDestination").toString();
    }

    public static String pathForDataSource() {
        return Paths.get("/metadata", "datasources").toString();
    }

    public static String pathForTemplates() {
        return Paths.get("/metadata", "templates").toString();
    }

    public static String pathForExtensibleEntity() {
        return EntityUtil.pathForExtensibleEntity(null);
    }

    public static String pathForExtensibleEntity(String typeName) {
        if (StringUtils.isNotBlank(typeName)) {
            return Paths.get("/", ExtensionsConstants.ENTITIES, typeName).toString();
        } else {
            return Paths.get("/", ExtensionsConstants.ENTITIES).toString();
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








