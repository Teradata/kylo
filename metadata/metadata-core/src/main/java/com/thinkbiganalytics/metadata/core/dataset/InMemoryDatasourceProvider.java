/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.Datasource.ID;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.core.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.core.dataset.files.BaseDirectoryDatasource;
import com.thinkbiganalytics.metadata.core.dataset.hive.BaseHiveTableDatasource;

/**
 *
 * @author Sean Felten
 */
public class InMemoryDatasourceProvider implements DatasourceProvider {
    
    private Map<Datasource.ID, Datasource> datasets = new ConcurrentHashMap<>();

    public DatasourceCriteria datasetCriteria() {
        return new DatasetCriteriaImpl();
    }
    
    @Override
    public ID resolve(Serializable id) {
        if (id instanceof BaseDatasource.DatasourceId) {
            return (BaseDatasource.DatasourceId) id;
        } else {
            return new BaseDatasource.DatasourceId(id);
        }
    }

    @Override
    public Datasource ensureDatasource(String name, String descr) {
        synchronized (this.datasets) {
            BaseDatasource ds = getExistingDataset(name);
            
            if (ds == null) {
                ds = new BaseDatasource(name, descr);
                this.datasets.put(ds.getId(), ds);
            }
            
            return ds;
        }
    }

    public DirectoryDatasource ensureDirectoryDatasource(String name, String descr, Path dir) {
        synchronized (this.datasets) {
            Datasource ds = getExistingDataset(name);
            
            if (ds != null) {
                if (ds.getClass().isAssignableFrom(BaseDirectoryDatasource.class)) {
                    return (BaseDirectoryDatasource) ds;
                } else {
                    throw new DatasourceException("A non-directory dataset already exists with the given name:" + name);
                }
            }
            
            BaseDirectoryDatasource dds = new BaseDirectoryDatasource(name, descr, dir);
            this.datasets.put(dds.getId(), dds);
            return dds;
        }
    }

    @Override
    public HiveTableDatasource ensureHiveTableDatasource(String name, String descr, String database, String table) {
        synchronized (this.datasets) {
            Datasource ds = getExistingDataset(name);
            
            if (ds != null) {
                if (ds.getClass().isAssignableFrom(BaseHiveTableDatasource.class)) {
                    return (BaseHiveTableDatasource) ds;
                } else {
                    throw new DatasourceException("A non-hive dataset already exists with the given name:" + name);
                }
            }
            
            BaseHiveTableDatasource hds = new BaseHiveTableDatasource(name, descr, database, table);
            this.datasets.put(hds.getId(), hds);
            return hds;
        }
    }
    
    
    @Override
    public DirectoryDatasource asDirectoryDatasource(ID dsId, Path dir) {
        synchronized (this.datasets) {
            BaseDatasource ds = (BaseDatasource) this.datasets.get(dsId);
            
            if (ds != null) {
                BaseDirectoryDatasource dds = new BaseDirectoryDatasource(ds, dir);
                this.datasets.put(dds.getId(), dds);
                return dds;
            } else {
                throw new DatasourceException("A no dataset exists with the given ID: " + dsId);
            }
        }
    }

    @Override
    public HiveTableDatasource asHiveTableDatasource(ID dsId, String database, String table) {
        synchronized (this.datasets) {
            BaseDatasource ds = (BaseDatasource) this.datasets.get(dsId);
            
            if (ds != null) {
                BaseHiveTableDatasource hds = new BaseHiveTableDatasource(ds, database, table);
                this.datasets.put(hds.getId(), hds);
                return hds;
            } else {
                throw new DatasourceException("A no dataset exists with the given ID: " + dsId);
            }
        }
    }

    @Override
    public Datasource getDatasource(ID id) {
        return this.datasets.get(id);
    }

    @Override
    public Set<Datasource> getDatasources() {
        return new HashSet<Datasource>(this.datasets.values());
    }

    @Override
    public List<Datasource> getDatasources(DatasourceCriteria criteria) {
        // TODO replace cast with copy method
        DatasetCriteriaImpl critImpl = (DatasetCriteriaImpl) criteria;
        Iterator<Datasource> filtered = Iterators.filter(this.datasets.values().iterator(), critImpl);
        Iterator<Datasource> limited = Iterators.limit(filtered, critImpl.getLimit());
        List<Datasource> list = Lists.newArrayList(limited);
        
        Collections.sort(list, critImpl);
        return list;
    }

    
    private BaseDatasource getExistingDataset(String name) {
        synchronized (this.datasets) {
            for (Datasource ds : this.datasets.values()) {
                if (ds.getName().equals(name)) {
                    return (BaseDatasource) ds;
                }
            }
            return null;
        }
    }


    private static class DatasetCriteriaImpl extends AbstractMetadataCriteria<DatasourceCriteria> 
        implements DatasourceCriteria, Predicate<Datasource>, Comparator<Datasource> {
        
        private String name;
        private DateTime createdOn;
        private DateTime createdAfter;
        private DateTime createdBefore;
        private Class<? extends Datasource> type;

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
