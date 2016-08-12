package com.thinkbiganalytics.metadata.modeshape.datasource;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.core.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.datasource.hive.JcrHiveTableDatasource;
import com.thinkbiganalytics.metadata.modeshape.support.JcrTool;

/**
 * Created by sr186054 on 6/7/16.
 */
public class JcrDatasourceProvider extends BaseJcrProvider<Datasource, Datasource.ID> implements DatasourceProvider {
    
    private static final Map<Class<? extends Datasource>, Class<? extends JcrDatasource>> DOMAIN_TYPES_MAP;
    static {
        Map<Class<? extends Datasource>, Class<? extends JcrDatasource>> map = new HashMap<>();
        map.put(HiveTableDatasource.class, JcrHiveTableDatasource.class);
        DOMAIN_TYPES_MAP = map;
    }
    
    private static final Map<String, Class<? extends JcrDatasource>> NODE_TYPES_MAP;
    static {
        Map<String, Class<? extends JcrDatasource>> map = new HashMap<>();
        map.put(JcrDatasource.NODE_TYPE, JcrDatasource.class);
        map.put(JcrHiveTableDatasource.NODE_TYPE, JcrHiveTableDatasource.class);
        NODE_TYPES_MAP = map;
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
        if (NODE_TYPES_MAP.containsKey(jcrNodeType)) {
            return NODE_TYPES_MAP.get(jcrNodeType);
        } else {
            return JcrDatasource.class;
        }
    }

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
        try {
            Field folderField = FieldUtils.getField(jcrEntityType, "NODE_TYPE", true);
            String jcrType = (String) folderField.get(null);
            return jcrType;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Shouldn't really happen.
            throw new MetadataException("Unavle to determing JCR node the for entity class: " + jcrEntityType, e);
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
//
//
//    @Override
//    public DirectoryDatasource ensureDirectoryDatasource(String name, String descr, Path dir) {
//        return null;
//    }
//
//    @Override
//    public HiveTableDatasource ensureHiveTableDatasource(String name, String descr, String database, String table) {
//        return null;
//    }
//
//    @Override
//    public DirectoryDatasource asDirectoryDatasource(Datasource.ID dsId, Path dir) {
//        return null;
//    }
//
//    @Override
//    public HiveTableDatasource asHiveTableDatasource(Datasource.ID dsId, String database, String table) {
//        return null;
//    }

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
            
            @SuppressWarnings("unchecked")
            J datasource = (J) findOrCreateEntity(subfolderNode.getPath(), name, implType, props);
            
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
            if (this.type != null && ! this.type.isAssignableFrom(input.getClass())) return false;
            if (this.name != null && ! name.equals(input.getName())) return false;
            if (this.createdOn != null && ! this.createdOn.equals(input.getCreatedTime())) return false;
            if (this.createdAfter != null && ! this.createdAfter.isBefore(input.getCreatedTime())) return false;
            if (this.createdBefore != null && ! this.createdBefore.isBefore(input.getCreatedTime())) return false;
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
