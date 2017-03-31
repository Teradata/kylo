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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource;
import com.thinkbiganalytics.metadata.api.datasource.JdbcDatasource;
import com.thinkbiganalytics.metadata.core.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrObjectTypeResolver;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrTool;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.joda.time.DateTime;
import org.modeshape.common.text.Jsr283Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 */
public class JcrDatasourceProvider extends BaseJcrProvider<Datasource, Datasource.ID> implements DatasourceProvider {

    private static final Logger log = LoggerFactory.getLogger(JcrDatasourceProvider.class);
    private static final Map<Class<? extends Datasource>, Class<? extends JcrDatasource>> DOMAIN_TYPES_MAP;
    private static final Map<String, Class<? extends JcrDatasource>> NODE_TYPES_MAP;
    public static JcrObjectTypeResolver<? extends JcrDatasource> TYPE_RESOLVER = new JcrObjectTypeResolver<JcrDatasource>() {
        @Override
        public Class<? extends JcrDatasource> resolve(Node node) {
            try {
                if (NODE_TYPES_MAP.containsKey(node.getPrimaryNodeType().getName())) {
                    return NODE_TYPES_MAP.get(node.getPrimaryNodeType().getName());
                } else {
                    return JcrDatasource.class;
                }
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to determine type of node: " + node, e);
            }
        }
    };

    static {
        Map<Class<? extends Datasource>, Class<? extends JcrDatasource>> map = new HashMap<>();
        map.put(DerivedDatasource.class, JcrDerivedDatasource.class);
        map.put(JdbcDatasource.class, JcrJdbcDatasource.class);
        DOMAIN_TYPES_MAP = map;
    }

    static {
        Map<String, Class<? extends JcrDatasource>> map = new HashMap<>();
        map.put(JcrDerivedDatasource.NODE_TYPE, JcrDerivedDatasource.class);
        map.put(JcrJdbcDatasource.NODE_TYPE, JcrJdbcDatasource.class);
        NODE_TYPES_MAP = map;
    }

    @Inject
    private MetadataAccess metadataAccess;

    public static Class<? extends JcrEntity> resolveJcrEntityClass(String jcrNodeType) {
        if (NODE_TYPES_MAP.containsKey(jcrNodeType)) {
            return NODE_TYPES_MAP.get(jcrNodeType);
        } else {
            return JcrDatasource.class;
        }
    }

    public static Class<? extends JcrEntity> resolveJcrEntityClass(Node node) {
        try {
            return resolveJcrEntityClass(node.getPrimaryNodeType().getName());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to determine type of node: " + node, e);
        }
    }

    /**
     * Finds the derived ds by Type and System Name
     */
    public DerivedDatasource findDerivedDatasource(String datasourceType, String systemName) {
        String query = "SELECT * from " + EntityUtil.asQueryProperty(JcrDerivedDatasource.NODE_TYPE) + " as e "
                       + "WHERE e." + EntityUtil.asQueryProperty(JcrDerivedDatasource.TYPE_NAME) + " = $datasourceType "
                       + "AND e." + EntityUtil.asQueryProperty(JcrDatasource.SYSTEM_NAME) + " = $identityString";

        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("datasourceType", datasourceType);
        bindParams.put("identityString", systemName);
        return JcrQueryUtil.findFirst(getSession(), query, bindParams, JcrDerivedDatasource.class);
    }

    private JcrDerivedDatasource findDerivedDatasourceByNodeName(String nodeName) throws RepositoryException {
        Node parentNode = getSession().getNode(EntityUtil.pathForDerivedDatasource());
        try {
            Node child = parentNode.getNode(nodeName);
            if (child != null) {
                JcrDerivedDatasource jcrDerivedDatasource = new JcrDerivedDatasource(child);
                return jcrDerivedDatasource;
            }
        } catch (PathNotFoundException e) {
            //this is ok if we cant find it we will try to create it.
        }
        return null;
    }

    /**
     * gets or creates the Derived datasource
     */
    public DerivedDatasource ensureDerivedDatasource(String datasourceType, String identityString, String title, String desc, Map<String, Object> properties) {
        //ensure the identity String is not null
        if (identityString == null) {
            identityString = "";
        }
        if (datasourceType == null) {
            datasourceType = "Datasource";
        }

        DerivedDatasource derivedDatasource = findDerivedDatasource(datasourceType, identityString);
        if (derivedDatasource == null) {
            try {
                if (!getSession().getRootNode().hasNode("metadata/datasources/derived")) {

                    if (!getSession().getRootNode().hasNode("metadata/datasources")) {
                        getSession().getRootNode().addNode("metadata", "datasources");
                    }
                    getSession().getRootNode().getNode("metadata/datasources").addNode("derived");
                }
                Node parentNode = getSession().getNode(EntityUtil.pathForDerivedDatasource());
                String nodeName = datasourceType + "-" + identityString;
                if (Jsr283Encoder.containsEncodeableCharacters(identityString)) {
                    nodeName = new Jsr283Encoder().encode(nodeName);
                }
                JcrDerivedDatasource jcrDerivedDatasource = null;
                try {
                    jcrDerivedDatasource = findDerivedDatasourceByNodeName(nodeName);
                } catch (RepositoryException e) {
                    log.warn("An exception ocurred trying to find the DerivedDatasource by node name {}.  {} ", nodeName, e.getMessage());
                }
                derivedDatasource = jcrDerivedDatasource;
                if (jcrDerivedDatasource == null) {
                    Node derivedDatasourceNode = JcrUtil.createNode(parentNode, nodeName, JcrDerivedDatasource.NODE_TYPE);
                    jcrDerivedDatasource = new JcrDerivedDatasource(derivedDatasourceNode);
                    jcrDerivedDatasource.setSystemName(identityString);
                    jcrDerivedDatasource.setDatasourceType(datasourceType);
                    jcrDerivedDatasource.setTitle(title);
                    jcrDerivedDatasource.setDescription(desc);
                    derivedDatasource = jcrDerivedDatasource;
                }
            } catch (RepositoryException e) {
                log.error("Failed to create Derived Datasource for DatasourceType: {}, IdentityString: {}, Error: {}", datasourceType, identityString, e.getMessage(), e);
            }
        }
        if (derivedDatasource != null) {
            // ((JcrDerivedDatasource)derivedDatasource).mergeProperties()
            if (properties != null) {
                derivedDatasource.setProperties(properties);
            }
            derivedDatasource.setTitle(title);
        }

        return derivedDatasource;
    }

    @Override
    public Class<? extends Datasource> getEntityClass() {
        return JcrDatasource.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrDatasource.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass(String jcrNodeType) {
        return resolveJcrEntityClass(jcrNodeType);
    }

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
        try {
            Field folderField = FieldUtils.getField(jcrEntityType, "NODE_TYPE", true);
            String jcrType = (String) folderField.get(null);
            return jcrType;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Shouldn't really happen.
            throw new MetadataException("Unable to determine JCR node the for entity class: " + jcrEntityType, e);
        }
    }

    @Override
    public DatasourceCriteria datasetCriteria() {
        return new Criteria();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends Datasource> D ensureDatasource(String name, String descr, Class<D> type) {
        JcrDatasource datasource = createImpl(name, descr, type);
        datasource.setDescription(descr);
        return (D) datasource;
    }

    @Override
    public Datasource getDatasource(Datasource.ID id) {
        return findById(id);
    }

    @Override
    public List<Datasource> getDatasources() {
        return findAll();
    }

    @Override
    public List<Datasource> getDatasources(DatasourceCriteria criteria) {
        return findAll().stream().filter((Criteria) criteria).collect(Collectors.toList());
    }

    @Override
    public Datasource.ID resolve(Serializable id) {
        return resolveId(id);
    }

    @Override
    public void removeDatasource(Datasource.ID id) {
        Datasource ds = getDatasource(id);
        if (ds != null) {
            try {
                JcrMetadataAccess.ensureCheckoutNode(((JcrDatasource) ds).getNode().getParent());
                ((JcrDatasource) ds).getNode().remove();
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Unable to remove Datasource: " + id);
            }
        }
    }


    @Override
    public DerivedDatasource ensureGenericDatasource(String name, String descr) {
        DerivedDatasource genericDatasource = ensureDatasource(name, descr, DerivedDatasource.class);
        return genericDatasource;
    }


    public Datasource.ID resolveId(Serializable fid) {
        return new JcrDatasource.DatasourceId(fid);
    }

    private <J extends JcrDatasource> J createImpl(String name, String descr, Class<? extends Datasource> type) {
        try {
            JcrTool tool = new JcrTool();
            Class<J> implType = deriveImplType(type);
            Field folderField = FieldUtils.getField(implType, "PATH_NAME", true);
            String subfolderName = (String) folderField.get(null);
            String dsPath = EntityUtil.pathForDataSource();
            Node dsNode = getSession().getNode(dsPath);
            Node subfolderNode = tool.findOrCreateChild(dsNode, subfolderName, "nt:folder");

            Map<String, Object> props = new HashMap<>();
            props.put(JcrDatasource.SYSTEM_NAME, name);

            String encodedName = org.modeshape.jcr.value.Path.DEFAULT_ENCODER.encode(name);

            @SuppressWarnings("unchecked")
            J datasource = (J) findOrCreateEntity(subfolderNode.getPath(), encodedName, implType, props);

            datasource.setTitle(name);
            datasource.setDescription(descr);
            return datasource;
        } catch (IllegalArgumentException | IllegalAccessException | RepositoryException e) {
            throw new MetadataException("Unable to create datasource: " + type, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <J extends JcrDatasource> Class<J> deriveImplType(Class<? extends Datasource> domainType) {
        Class<? extends JcrDatasource> implType = DOMAIN_TYPES_MAP.get(domainType);

        if (implType != null) {
            return (Class<J>) implType;
        } else {
            throw new MetadataException("No datasource implementation found for type: " + domainType);
        }
    }


    // TODO Replace this implementation with a query restricting version.  This is just a 
    // workaround that filters on the results set.
    private static class Criteria extends AbstractMetadataCriteria<DatasourceCriteria>
        implements DatasourceCriteria, Predicate<Datasource>, Comparator<Datasource> {

        private String name;
        private DateTime createdOn;
        private DateTime createdAfter;
        private DateTime createdBefore;
        private Class<? extends Datasource> type;

        @Override
        public boolean test(Datasource input) {
            if (this.type != null && !this.type.isAssignableFrom(input.getClass())) {
                return false;
            }
            if (this.name != null && !name.equals(input.getName())) {
                return false;
            }
            if (this.createdOn != null && !this.createdOn.equals(input.getCreatedTime())) {
                return false;
            }
            if (this.createdAfter != null && !this.createdAfter.isBefore(input.getCreatedTime())) {
                return false;
            }
            if (this.createdBefore != null && !this.createdBefore.isBefore(input.getCreatedTime())) {
                return false;
            }
            return true;
        }

        @Override
        public int compare(Datasource o1, Datasource o2) {
            return o2.getCreatedTime().compareTo(o1.getCreatedTime());
        }

        @Override
        public DatasourceCriteria name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public DatasourceCriteria createdOn(DateTime time) {
            this.createdOn = time;
            return this;
        }

        @Override
        public DatasourceCriteria createdAfter(DateTime time) {
            this.createdAfter = time;
            return this;
        }

        @Override
        public DatasourceCriteria createdBefore(DateTime time) {
            this.createdBefore = time;
            return this;
        }

        @Override
        public DatasourceCriteria type(Class<? extends Datasource> type) {
            this.type = type;
            return this;
        }
    }
}
