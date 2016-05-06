/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Predicate;
import com.thinkbiganalytics.metadata.api.DatasourceAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.Datasource.ID;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.jpa.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.jpa.datasource.files.JpaDirectoryDatasource;
import com.thinkbiganalytics.metadata.jpa.datasource.hive.JpaHiveTableDatasource;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author Sean Felten
 */
public class JpaDatasourceProvider implements DatasourceProvider {
    
    @Inject
    @Named("metadataDateTimeFormatter")
    private DateTimeFormatter dateTimeFormatter;

    @Inject
    @Qualifier("metadataEntityManager")
    private EntityManager entityMgr;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#datasetCriteria()
     */
    @Override
    public DatasourceCriteria datasetCriteria() {
        return new Criteria(this.dateTimeFormatter);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#ensureDatasource(java.lang.String, java.lang.String)
     */
    @Override
    public Datasource ensureDatasource(String name, String descr) {
        // TODO is this needed?
        throw new UnsupportedOperationException("Ensuring existence of plain datasources is not supported at this time");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#ensureDirectoryDatasource(java.lang.String, java.lang.String, java.nio.file.Path)
     */
    @Override
    public DirectoryDatasource ensureDirectoryDatasource(String name, String descr, Path dir) {
        JpaDirectoryDatasource ds = null;
        List<Datasource> dsList = getDatasources(datasetCriteria().name(name));
        
        if (dsList.isEmpty()) {
            ds = new JpaDirectoryDatasource(name, descr, dir);
            this.entityMgr.persist(ds);
        } else {
            JpaDatasource found = (JpaDatasource) dsList.get(0);
            
            if (found instanceof JpaDirectoryDatasource) {
                ds = (JpaDirectoryDatasource) found;
            } else {
                throw new DatasourceAlreadyExistsException("A datasource with the same name but the wrong type already exists: " 
                                + name + ", type: " + found.getClass().getSimpleName());
            }
        }
        
        return ds;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#ensureHiveTableDatasource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public HiveTableDatasource ensureHiveTableDatasource(String name, String descr, String database, String table) {
        JpaHiveTableDatasource ds = null;
        List<Datasource> dsList = getDatasources(datasetCriteria().name(name));
        
        if (dsList.isEmpty()) {
            ds = new JpaHiveTableDatasource(name, descr, database, table);
            this.entityMgr.persist(ds);
        } else {
            JpaDatasource found = (JpaDatasource) dsList.get(0);
            
            if (found instanceof JpaHiveTableDatasource) {
                ds = (JpaHiveTableDatasource) found;
            } else {
                throw new DatasourceAlreadyExistsException("A datasource with the same name but the wrong type already exists: " 
                                + name + ", type: " + found.getClass().getSimpleName());
            }
        }
        
        return ds;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#asDirectoryDatasource(com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, java.nio.file.Path)
     */
    @Override
    public DirectoryDatasource asDirectoryDatasource(ID dsId, Path dir) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Datasource type conversoun is not supported at this time");
   }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#asHiveTableDatasource(com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, java.lang.String, java.lang.String)
     */
    @Override
    public HiveTableDatasource asHiveTableDatasource(ID dsId, String database, String table) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Datasource type conversoun is not supported at this time");
   }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#getDatasource(com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public Datasource getDatasource(ID id) {
        return this.entityMgr.find(JpaDatasource.class, id); 
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#getDatasources()
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Datasource> getDatasources() {
        return new ArrayList<Datasource>(this.entityMgr.createQuery("select f from JpaDatasource f").getResultList());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#getDatasources(com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria)
     */
    @Override
    public List<Datasource> getDatasources(DatasourceCriteria criteria) {
        Criteria critImpl = (Criteria) criteria; 
        return new ArrayList<Datasource>(critImpl.select(this.entityMgr, JpaDatasource.class));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#resolve(java.io.Serializable)
     */
    @Override
    public ID resolve(Serializable id) {
        if (id instanceof JpaDatasource.DatasourceId) {
            return (JpaDatasource.DatasourceId) id;
        } else {
            return new JpaDatasource.DatasourceId(id);
        }
    }
    
    
    
    private static class Criteria extends AbstractMetadataCriteria<DatasourceCriteria> 
        implements DatasourceCriteria, Predicate<Datasource> {
    
        private String name;
        private DateTime createdOn;
        private DateTime createdAfter;
        private DateTime createdBefore;
        private Class<? extends Datasource> type;
        private DateTimeFormatter dateTimeFormatter;
        
        public Criteria(DateTimeFormatter formatter) {
            this.dateTimeFormatter = formatter;
        }
    
        @Override
        public boolean apply(Datasource input) {
            if (this.type != null && ! this.type.isAssignableFrom(input.getClass())) return false;
            if (this.name != null && ! name.equals(input.getName())) return false;
            if (this.createdOn != null && ! this.createdOn.equals(input.getCreatedTime())) return false;
            if (this.createdAfter != null && ! this.createdAfter.isBefore(input.getCreatedTime())) return false;
            if (this.createdBefore != null && ! this.createdBefore.isBefore(input.getCreatedTime())) return false;
            return true;
        }
        
        @Override
        protected void applyFilter(StringBuilder query, HashMap<String, Object> params) {
            StringBuilder cond = new StringBuilder();
            
            if (this.name != null) cond.append("e.name = '").append(this.name).append("' ");
            if (this.createdOn != null) {
                if (cond.length() > 0) cond.append("and ");
                cond.append("e.created_time = :createdOn");
                params.put("createdOn", this.createdOn);
            }
            if (this.createdAfter != null) {
                if (cond.length() > 0) cond.append("and ");
                cond.append("e.created_time > :createdAfter");
                params.put("createdAfter", this.createdAfter);
            }
            if (this.createdBefore != null) {
                if (cond.length() > 0) cond.append("and ");
                cond.append("e.created_time < :createdBefore");
                params.put("createdBefore", this.createdBefore);
            }
            
            if (cond.length() > 0) {
                query.append(" where ").append(cond.toString());
            }
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
