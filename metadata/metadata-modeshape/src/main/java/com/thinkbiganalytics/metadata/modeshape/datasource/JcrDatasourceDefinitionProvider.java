package com.thinkbiganalytics.metadata.modeshape.datasource;

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
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 */
public class JcrDatasourceDefinitionProvider extends BaseJcrProvider<DatasourceDefinition, DatasourceDefinition.ID> implements DatasourceDefinitionProvider {

    private static final Logger log = LoggerFactory.getLogger(JcrDatasourceDefinitionProvider.class);

    @Override
    public Class<? extends DatasourceDefinition> getEntityClass() {
        return JcrDatasourceDefinition.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrDatasourceDefinition.class;
    }

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {

        return JcrDatasourceDefinition.NODE_TYPE;
    }

    @Override
    protected String getEntityQueryStartingPath() {
        return  EntityUtil.pathForDatasourceDefinition();
    }

    @Override
    public DatasourceDefinition.ID resolveId(Serializable fid) {
        return new JcrDatasourceDefinition.DatasourceDefinitionId((fid));
    }


    @Override
    public Set<DatasourceDefinition> getDatasourceDefinitions() {
        List<DatasourceDefinition> list = findAll();
        if (list != null) {
            return Sets.newHashSet(list);
        }
        return null;
    }


    public DatasourceDefinition ensureDatasourceDefinition(String processorType) {
        DatasourceDefinition dsDef = findByProcessorType(processorType);
        if (dsDef == null) {

            try {
                if (!getSession().getRootNode().hasNode("metadata/datasourceDefinitions")) {
                    getSession().getRootNode().addNode("metadata/datasourceDefinitions", "tba:datasourceDefinitionsFolder");
                }
            } catch (RepositoryException e) {
                log.error("Failed to create datasource definitions node", e);
            }

            String path = EntityUtil.pathForDatasourceDefinition();
            Node node = findOrCreateEntityNode(path, processorType, getJcrEntityClass());
            JcrDatasourceDefinition def = new JcrDatasourceDefinition(node);
            dsDef = def;
        }
        return dsDef;

    }


    /**
     * TODO change to go against Path directly
     */
    public JcrDatasourceDefinition findByProcessorType(String processorType) {
        String
            query =
            "SELECT * from " + EntityUtil.asQueryProperty(JcrDatasourceDefinition.NODE_TYPE) + " as e WHERE e." + EntityUtil.asQueryProperty(JcrDatasourceDefinition.PROCESSOR_TYPE)
            + " = $processorType";
        query = applyFindAllFilter(query,EntityUtil.pathForDatasourceDefinition());
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("processorType", processorType);
        return JcrQueryUtil.findFirst(getSession(), query, bindParams, JcrDatasourceDefinition.class);
    }


    public void removeAll() {
        try {
            getSession().removeItem(EntityUtil.pathForDatasourceDefinition());
        } catch (Exception e) {
            throw new MetadataRepositoryException("Unable to remove DatasourceDefinitions ", e);
        }
    }
}
