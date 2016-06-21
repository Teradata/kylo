/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.op;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Predicate;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedNotFoundExcepton;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.ID;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.op.DataOperationCriteria;
import com.thinkbiganalytics.metadata.api.op.DataOperationNotFound;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.Dataset.ChangeType;
import com.thinkbiganalytics.metadata.api.op.DatasetCriteria;
import com.thinkbiganalytics.metadata.jpa.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.jpa.datasource.files.JpaDirectoryDatasource;
import com.thinkbiganalytics.metadata.jpa.datasource.files.JpaFileList;
import com.thinkbiganalytics.metadata.jpa.datasource.hive.JpaHiveTableDatasource;
import com.thinkbiganalytics.metadata.jpa.datasource.hive.JpaHiveTableUpdate;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeedDestination;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author Sean Felten
 */
public class JpaDataOperationsProvider implements DataOperationsProvider {
    
    @Inject
    @Named("metadataDateTimeFormatter")
    private DateTimeFormatter dateTimeFormatter;
    
    @Inject
    private FeedProvider feedProvider;

    @Inject
    @Qualifier("metadataEntityManager")
    private EntityManager entityMgr;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#asOperationId(java.lang.String)
     */
    @Override
    public ID resolve(Serializable id) {
        if (id instanceof JpaDataOperation.OpId) {
            return (JpaDataOperation.OpId) id;
        } else {
            return new JpaDataOperation.OpId(id);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#beginOperation(com.thinkbiganalytics.metadata.api.feed.FeedDestination, org.joda.time.DateTime)
     */
    @Override
    public DataOperation beginOperation(FeedDestination dest, DateTime start) {
        JpaDataOperation op = new JpaDataOperation((JpaFeedDestination) dest);
        this.entityMgr.persist(op);
        return op;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#beginOperation(com.thinkbiganalytics.metadata.api.feed.Feed.ID, Datasource.ID, org.joda.time.DateTime)
     */
    @Override
    public DataOperation beginOperation(Feed.ID feedId, Datasource.ID dsId, DateTime start) {
        JpaFeed feed = (JpaFeed) this.feedProvider.getFeed(feedId);
        
        if (feed != null) {
            JpaFeedDestination dest = (JpaFeedDestination) this.feedProvider.ensureFeedDestination(feedId, dsId);
            
            return beginOperation(dest, DateTime.now());
        } else {
            throw new FeedNotFoundExcepton(feedId);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.DataOperation.State)
     */
    @Override
    public DataOperation updateOperation(ID id, String status, State result) {
        JpaDataOperation op = this.entityMgr.find(JpaDataOperation.class, id);
        
        if (op != null) {
            op.setStatus(status);
            op.setState(result);
            return this.entityMgr.merge(op);
        } else {
            throw new DataOperationNotFound(id);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, java.lang.Exception)
     */
    @Override
    public DataOperation updateOperation(ID id, String status, Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter prt = new PrintWriter(sw);
        prt.flush();
        String msg = status + "\n\n" + sw.toString();
        
        return updateOperation(id, msg.substring(0, Math.min(2048, msg.length())), State.FAILURE);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.Dataset)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <D extends Datasource, C extends ChangeSet> DataOperation updateOperation(ID id,
                                                                                     String status,
                                                                                     Dataset<D, C> changes) {
        JpaDataOperation op = (JpaDataOperation) updateOperation(id, status, State.SUCCESS);
        op.setDataset((JpaDataset<Datasource, ChangeSet>) changes);
        return op;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createDataset(filesys.DirectoryDatasource, java.util.List)
     */
    @Override
    public Dataset<DirectoryDatasource, FileList> createDataset(DirectoryDatasource ds, List<Path> paths) {
        JpaDirectoryDatasource dds = (JpaDirectoryDatasource) ds;
        JpaFileList fileList = new JpaFileList(paths);
        JpaDataset<DirectoryDatasource, FileList> dataset = new JpaDataset<DirectoryDatasource, FileList>(dds, fileList);
        
        this.entityMgr.persist(dataset);
        return dataset;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createDataset(hive.HiveTableDatasource, int)
     */
    @Override
    public Dataset<HiveTableDatasource, HiveTableUpdate> createDataset(HiveTableDatasource ds, int count) {
        JpaHiveTableDatasource dds = (JpaHiveTableDatasource) ds;
        JpaHiveTableUpdate hiveUpdate = new JpaHiveTableUpdate(count);
        JpaDataset<HiveTableDatasource, HiveTableUpdate> dataset = new JpaDataset<HiveTableDatasource, HiveTableUpdate>(dds, hiveUpdate);
        
        this.entityMgr.persist(dataset);
        return dataset;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#dataOperationCriteria()
     */
    @Override
    public DataOperationCriteria dataOperationCriteria() {
        return new OpCriteria();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID)
     */
    @Override
    public DataOperation getDataOperation(ID id) {
        return this.entityMgr.find(JpaDataOperation.class, id);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperations()
     */
    @Override
    public List<DataOperation> getDataOperations() {
        return new ArrayList<DataOperation>(this.entityMgr.createQuery("select o from JpaDataOperation o", 
                                                                       JpaDataOperation.class).getResultList());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperations(com.thinkbiganalytics.metadata.api.op.DataOperationCriteria)
     */
    @Override
    public List<DataOperation> getDataOperations(DataOperationCriteria criteria) {
        OpCriteria critImpl = (OpCriteria) criteria;
        return new ArrayList<DataOperation>(critImpl.select(this.entityMgr, JpaDataOperation.class));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#DatasetCriteria()
     */
    @Override
    public DatasetCriteria datasetCriteria() {
        return new DsCriteria();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDatasets(Datasource.ID)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Dataset<Datasource, ChangeSet>> getDatasets(Datasource.ID dsId) {
        return new ArrayList<Dataset<Datasource, ChangeSet>>(this.entityMgr.createQuery("select d from JpaDataset d").getResultList());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDatasets(com.thinkbiganalytics.metadata.api.op.DatasetCriteria)
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Dataset<Datasource, ChangeSet>> getDatasets(DatasetCriteria criteria) {
        DsCriteria critImpl = (DsCriteria) criteria;
        return new ArrayList(critImpl.select(this.entityMgr, JpaDataset.class));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    public void addListener(DataChangeEventListener<Datasource, ChangeSet> listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(filesys.DirectoryDatasource, com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    public void addListener(DirectoryDatasource ds, DataChangeEventListener<DirectoryDatasource, FileList> listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(hive.HiveTableDatasource, com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    public void addListener(HiveTableDatasource ds, DataChangeEventListener<HiveTableDatasource, HiveTableUpdate> listener) {
        // TODO Auto-generated method stub

    }

    private static class OpCriteria extends AbstractMetadataCriteria<DataOperationCriteria> 
        implements DataOperationCriteria, Predicate<DataOperation> {

        private Feed.ID feedId;
        private Datasource.ID datasourceId;
        private Set<State> states = new HashSet<>();
        private Set<Class<? extends Datasource>> types = new HashSet<>();
        
        @Override
        protected void applyFilter(StringBuilder queryStr, HashMap<String, Object> params) {
            StringBuilder cond = new StringBuilder();
            
            if (this.feedId != null) {
                cond.append("e.producer.feed.id = :feedId ");
                params.put("feedId", this.feedId);
            }
            
            if (this.datasourceId != null) {
                if (cond.length() > 0) cond.append("and ");
                cond.append("e.producer.dataource.id = :dsId " );
                params.put("dsId", this.datasourceId);
            }
            
            if (! this.states.isEmpty()) {
                if (cond.length() > 0) cond.append("and ");
                cond.append("e.state in :states");
                params.put("states", this.states);
            }
            
            if (! this.types.isEmpty()) {
                if (cond.length() > 0) cond.append("and ");
                cond.append("type(e) in :types");
                params.put("types", this.types);
            }
            
            if (cond.length() > 0) {
                queryStr.append("where ").append(cond.toString());
            }
        }
            
        
        @Override
        public boolean apply(DataOperation input) {
            if (this.feedId != null && ! this.feedId.equals(input.getProducer().getFeed().getId())) return false;
            if (this.datasourceId != null && ! this.datasourceId.equals(input.getDataset().getDatasource().getId())) return false;
            if (this.states.size() > 0 && ! this.states.contains(input.getState())) return false;
            if (this.types.size() > 0) {
                for (Class<? extends Datasource> type : types) {
                    if (type.isAssignableFrom(input.getClass())) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
        
        @Override
        public DataOperationCriteria state(State... states) {
            for (State state : states) {
                this.states.add(state);
            }
            return this;
        }

        @Override
        public DataOperationCriteria feed(Feed.ID srcId) {
            this.feedId = srcId;
            return this;
        }

        @Override
        public DataOperationCriteria dataset(Datasource.ID dsId) {
            this.datasourceId = dsId;
            return this;
        }

        @Override
        public DataOperationCriteria dataset(@SuppressWarnings("unchecked") Class<? extends Datasource>... dsTypes) {
            for (Class<? extends Datasource> type : dsTypes) {
                this.types.add(type);
            }
            return this;
        }
    }
    
    private static class DsCriteria extends AbstractMetadataCriteria<DatasetCriteria> 
        implements DatasetCriteria, Predicate<Dataset<? extends Datasource, ? extends ChangeSet>> {

        private Set<Datasource.ID> datasourceIds = new HashSet<>();
        private Set<ChangeType> types = new HashSet<>();
        private DateTime changedOn;
        private DateTime changedAfter;
        private DateTime changedBefore;
        
        @Override
        protected void applyFilter(StringBuilder queryStr, HashMap<String, Object> params) {
            StringBuilder cond = new StringBuilder();
            
            if (! this.datasourceIds.isEmpty()) {
                cond.append("e.datasource.id in :dsIds ");
                params.put("dsIds", this.datasourceIds);
            }
            if (! this.types.isEmpty()) {
                cond.append("e.type in :types ");
                params.put("types", this.types);
            }
            if (this.changedOn != null) {
                if (cond.length() > 0) cond.append("and ");
                cond.append("e.created_time = :changedOn ");
                params.put("changedOn", this.changedOn);
            }
            if (this.changedAfter != null) {
                if (cond.length() > 0) cond.append("and ");
                cond.append("e.created_time > :changedAfter ");
                params.put("changedAfter", this.changedAfter);
            }
            if (this.changedBefore != null) {
                if (cond.length() > 0) cond.append("and ");
                cond.append("e.created_time < :changedBefore ");
                params.put("changedBefore", this.changedBefore);
            }
            
            if (cond.length() > 0) {
                queryStr.append(" where ").append(cond.toString());
            }
        }
        
        @Override
        public boolean apply(Dataset<?, ?> input) {
            if (this.changedOn != null && ! this.changedOn.equals(input.getCreatedTime())) return false;
            if (this.changedAfter != null && ! this.changedAfter.isBefore(input.getCreatedTime())) return false;
            if (this.changedBefore != null && ! this.changedBefore.isBefore(input.getCreatedTime())) return false;
            if (this.types.size() > 0 && ! this.types.contains(input.getType())) return false;
            if (this.datasourceIds.size() > 0 && ! this.datasourceIds.contains(input.getDatasource().getId())) return false;

            return true;
        }
        
        @Override
        public DatasetCriteria datasource(Datasource.ID... dsIds) {
            for (Datasource.ID id : dsIds) {
                this.datasourceIds.add(id);
            }
            return this;
        }

        @Override
        public DatasetCriteria type(ChangeType... types) {
            for (ChangeType type : types) {
                this.types.add(type);
            }
            return null;
        }

        @Override
        public DsCriteria changedOn(DateTime time) {
            this.changedOn = time;
            return this;
        }

        @Override
        public DsCriteria changedAfter(DateTime time) {
            this.changedAfter = time;
            return this;
        }

        @Override
        public DsCriteria changedBefore(DateTime time) {
            this.changedBefore = time;
            return this;
        }
    }
}
