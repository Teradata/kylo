package com.thinkbiganalytics.metadata.modeshape.common;

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


import com.thinkbiganalytics.metadata.modeshape.extension.ExtensionsConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility to get basic Paths for schema
 */
public class EntityUtil {

    /**
     * Instances of {@code EntityUtil} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private EntityUtil() {
        throw new UnsupportedOperationException();
    }

    public static String pathForCategory() {
        return JcrUtil.path("/metadata", "feeds").toString();
    }

    public static String pathForCategory(String categorySystemName) {
        return JcrUtil.path("/metadata", "feeds", categorySystemName).toString();
    }

    public static String pathForCategoryDetails(String categorySystemName) {
        return JcrUtil.path("/metadata", "feeds", categorySystemName, "tba:details").toString();
    }

    public static String pathForDatasourceDefinition() {
        return JcrUtil.path("/metadata", "datasourceDefinitions").toString();
    }

    public static String pathForFeed(String categorySystemName, String feedSystemName) {
        return JcrUtil.path(pathForCategoryDetails(categorySystemName), feedSystemName).toString();
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

    public static String pathForDerivedDatasource() {
        return JcrUtil.path("/metadata", "datasources", "derived").toString();
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

    public static String pathForDomainTypes() {
        return JcrUtil.path("/metadata", "domainTypes").toString();
    }

    public static String pathForUsers() {
        return JcrUtil.path("/users").toString();
    }

    public static String pathForGroups() {
        return JcrUtil.path("/groups").toString();
    }

    public static String pathForSla() {
        return JcrUtil.path("/metadata","sla").toString();
    }

    public static String asQueryProperty(String prop) {
        return "[" + prop + "]";
    }
}








