/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.Dataset.ID;
import com.thinkbiganalytics.metadata.api.dataset.DatasetCriteria;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.core.dataset.files.BaseDirectoryDataset;
import com.thinkbiganalytics.metadata.core.dataset.hive.BaseHiveTableDataset;

/**
 *
 * @author Sean Felten
 */
public class InMemoryDatasetProvider implements DatasetProvider {
    
    private Map<Dataset.ID, BaseDataset> datasets = new ConcurrentHashMap<>();

    public DatasetCriteria datasetCriteria() {
        return new DatasetCriteriaImpl();
    }

    public DirectoryDataset createDirectoryDataset(String name, String descr, Path dir) {
        return new BaseDirectoryDataset(name, descr, dir);
    }

    @Override
    public HiveTableDataset createHiveTableDataset(String name, String descr, String database, String table) {
        return new BaseHiveTableDataset(name, descr, database, table);
    }

    @Override
    public Dataset getDataset(ID id) {
        return this.datasets.get(id);
    }

    @Override
    public Set<Dataset> getDatasets() {
        return new HashSet<Dataset>(this.datasets.values());
    }

    @Override
    public Set<Dataset> getDatasets(DatasetCriteria criteria) {
        // TODO replace cast with copy method
        DatasetCriteriaImpl critImpl = (DatasetCriteriaImpl) criteria;
        return new HashSet<Dataset>(Sets.filter(new HashSet<>(this.datasets.values()), critImpl));
    }

    
    private static class DatasetCriteriaImpl implements DatasetCriteria, Predicate<BaseDataset> {
        
        private String name;
        private DateTime createdOn;
        private DateTime createdAfter;
        private DateTime createdBefore;
        private Class<? extends Dataset> type;

        @Override
        public boolean apply(BaseDataset input) {
            if (this.type != null && ! this.type.isAssignableFrom(input.getClass())) return false;
            if (this.name != null && ! name.equals(input.getName())) return false;
            if (this.createdOn != null && ! this.createdOn.equals(input.getCreationTime())) return false;
            if (this.createdAfter != null && ! this.createdAfter.isBefore(input.getCreationTime())) return false;
            if (this.createdBefore != null && ! this.createdBefore.isBefore(input.getCreationTime())) return false;
            return true;
        }

        @Override
        public DatasetCriteria name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public DatasetCriteria createdOn(DateTime time) {
            this.createdOn = time;
            return this;
        }

        @Override
        public DatasetCriteria createdAfter(DateTime time) {
            this.createdAfter = time;
            return this;
        }

        @Override
        public DatasetCriteria createdBefore(DateTime time) {
            this.createdBefore = time;
            return this;
        }

        @Override
        public DatasetCriteria type(Class<? extends Dataset> type) {
            this.type = type;
            return this;
        }
        
    }
}
