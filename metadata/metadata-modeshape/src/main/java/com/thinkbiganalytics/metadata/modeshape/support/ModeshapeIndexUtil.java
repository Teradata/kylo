package com.thinkbiganalytics.metadata.modeshape.support;
/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import org.modeshape.jcr.api.index.IndexColumnDefinition;
import org.modeshape.jcr.api.index.IndexDefinition;
import org.modeshape.jcr.api.index.IndexDefinitionTemplate;
import org.modeshape.jcr.api.index.IndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 8/31/17.
 */
public class ModeshapeIndexUtil {

    private static final Logger log = LoggerFactory.getLogger(ModeshapeIndexUtil.class);

    // registerIndex(indexManager(), "ntusysname", IndexDefinition.IndexKind.VALUE, "local", "tba::unstructured", "", null, "sysName", PropertyType.STRING);


    /**
     *
     * @param indexManager
     * @param indexName
     * @param indexedNodeType
     * @param propertyName
     * @param propertyType
     * @throws RepositoryException
     */
    public static void registerValueIndex(IndexManager indexManager, String indexName, String indexedNodeType, String propertyName, int propertyType) throws RepositoryException {
        registerIndex(indexManager, indexName, IndexDefinition.IndexKind.VALUE, "local", indexedNodeType, indexName + " description", null, propertyName, propertyType);

    }

    /**
     *
     * @param indexManager
     * @param indexName
     * @param kind
     * @param providerName
     * @param indexedNodeType
     * @param desc
     * @param workspaceNamePattern
     * @param propertyName
     * @param propertyType
     * @throws RepositoryException
     */
    public static void registerIndex(IndexManager indexManager, String indexName, IndexDefinition.IndexKind kind, String providerName, String indexedNodeType, String desc, String workspaceNamePattern,
                                     String propertyName, int propertyType) throws RepositoryException {

        if (indexManager.getIndexDefinitions().containsKey(indexName)) {
            return;
        }

        log.info("registering index on property {} and type {}", propertyName, indexedNodeType);

        IndexDefinitionTemplate template = indexManager.createIndexDefinitionTemplate();
        template.setName(indexName);
        template.setKind(kind);
        template.setNodeTypeName(indexedNodeType);
        template.setProviderName(providerName);
        if (workspaceNamePattern != null) {
            template.setWorkspaceNamePattern(workspaceNamePattern);
        } else {
            template.setAllWorkspaces();
        }
        if (desc != null) {
            template.setDescription(desc);
        }

        // Set up the columns ...
        IndexColumnDefinition colDefn = indexManager.createIndexColumnDefinitionTemplate().setPropertyName(propertyName).setColumnType(propertyType);
        template.setColumnDefinitions(colDefn);

        // Register the index, allow update:true  ...
        indexManager.registerIndex(template, true);
    }

    public static void unregisterIndex(IndexManager indexManager, String indexName) throws RepositoryException {
        indexManager.unregisterIndexes(indexName);
    }


}
