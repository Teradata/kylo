/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.Dataset.ChangeType;
import com.thinkbiganalytics.metadata.api.op.DatasetCriteria;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.op.DataOperationCriteria;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.core.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.core.dataset.files.BaseFileList;
import com.thinkbiganalytics.metadata.core.dataset.hive.BaseHiveTableUpdate;
import com.thinkbiganalytics.metadata.event.ChangeEventDispatcher;

/**
 *
 * @author Sean Felten
 */
public class InMemoryDataOperationsProvider implements DataOperationsProvider {
    
    private static final OpCriteria All_OPS = new OpCriteria() {
        public boolean apply(BaseDataOperation input) {
            return true;
        }
    };
    
    private static final ChangeCriteria ALL_CHANGES = new ChangeCriteria() {
        public boolean apply(com.thinkbiganalytics.metadata.api.op.Dataset<?,?> input) {
            return true;
        }
    };
    
    @Inject
    private DatasourceProvider datasetProvider;
    @Inject
    private FeedProvider feedProvider;
    @Inject
    private ChangeEventDispatcher dispatcher;
    
    private Map<DataOperation.ID, DataOperation> operations = new ConcurrentHashMap<>();
    private Map<Datasource.ID, List<Dataset<? extends Datasource, ? extends ChangeSet>>> changeSets = new ConcurrentHashMap<>();
    
    public InMemoryDataOperationsProvider() {
        super();
    }
    
    public InMemoryDataOperationsProvider(DatasourceProvider datasetProvider, FeedProvider feedProvider,
            ChangeEventDispatcher dispatcher) {
        super();
        this.datasetProvider = datasetProvider;
        this.feedProvider = feedProvider;
        this.dispatcher = dispatcher;
    }

    @Inject
    public void setDatasourceProvider(DatasourceProvider datasetProvider) {
        this.datasetProvider = datasetProvider;
    }

    @Inject
    public void setFeedProvider(FeedProvider feedProvider) {
        this.feedProvider = feedProvider;
    }

    @Inject
    public void setDispatcher(ChangeEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#asOperationId(java.lang.String)
     */
    @Override
    public DataOperation.ID asOperationId(String opIdStr) {
        return new BaseDataOperation.OpId(opIdStr);
    }
    
    
    @Override
    public DataOperation beginOperation(FeedDestination dest, DateTime start) {
        return beginOperation(dest.getFeed().getId(), dest.getDatasource().getId(), new DateTime());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#beginOperation(com.thinkbiganalytics.metadata.api.feed.Feed.ID, Datasource.ID)
     */
    public DataOperation beginOperation(Feed.ID feedId, Datasource.ID dsId, DateTime start) {
        Datasource ds = this.datasetProvider.getDatasource(dsId);
        Feed feed = this.feedProvider.getFeed(feedId);
        
        if (feed == null) {
            throw new DataOperationException("No feed with the given ID exists: " + feedId);
        }
        
        if (ds == null) {
            throw new DataOperationException("No dataset with the given ID exists: " + feedId);
        }
        
        FeedDestination feedDest = this.feedProvider.ensureFeedDestination(feed.getId(), ds.getId());
        BaseDataOperation op = new BaseDataOperation(ds, feedDest, start);
        this.operations.put(op.getId(), op);
        return op;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.DataOperation.State)
     */
    public DataOperation updateOperation(DataOperation.ID id, String status, State state) {
        BaseDataOperation op = (BaseDataOperation) this.operations.get(id);
        
        if (op == null) {
            throw new DataOperationException("No operation with the given ID exists: " + id);
        }
        
        String msg = StringUtils.isEmpty(status) ? "Operation in " + state.name().toLowerCase() + " state" : status;
        op = new BaseDataOperation(op, state, msg);
        
        this.operations.put(id, op);
        // TODO What do we do if the state was changed to COMPLETE and there is no change set?
        
        return op;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, java.lang.Exception)
     */
    public DataOperation updateOperation(DataOperation.ID id, String status, Exception ex) {
        StringWriter out = new StringWriter();
        PrintWriter prt = new PrintWriter(out);
        prt.println(status);
        prt.println("==============");
        ex.printStackTrace(prt);
        prt.flush();
        
        return updateOperation(id, out.toString(), State.FAILURE);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.Dataset)
     */
    @SuppressWarnings("unchecked")
    public <D extends Datasource, C extends ChangeSet> DataOperation updateOperation(DataOperation.ID id, String status, Dataset<D, C> changes) {
        BaseDataOperation op = (BaseDataOperation) this.operations.get(id);
        
        if (op == null) {
            throw new DataOperationException("No operation with the given ID exists: " + id);
        }
        
        String msg = StringUtils.isEmpty(status) ? "Operation completed successfully" : status;
        op = new BaseDataOperation(op, msg, (Dataset<Datasource, ChangeSet>) changes);
        
        this.operations.put(id, op);
        this.dispatcher.nofifyChange(changes);
        
        return op;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createChangeSet(com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource, java.util.List)
     */
    public Dataset<DirectoryDatasource, FileList> createDataset(DirectoryDatasource ds, List<Path> paths) {
        BaseFileList content = new BaseFileList(paths);
        return new BaseDataset<DirectoryDatasource, FileList>(ds, content);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createChangeSet(com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource, int)
     */
    public Dataset<HiveTableDatasource, HiveTableUpdate> createDataset(HiveTableDatasource ds, int count) {
        BaseHiveTableUpdate content = new BaseHiveTableUpdate(count);
        return new BaseDataset<HiveTableDatasource, HiveTableUpdate>(ds, content);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#dataOperationCriteria()
     */
    public DataOperationCriteria dataOperationCriteria() {
        return new OpCriteria();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID)
     */
    public DataOperation getDataOperation(DataOperation.ID id) {
        return this.operations.get(id);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperations()
     */
    public List<DataOperation> getDataOperations() {
        return getDataOperations(All_OPS);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperations(com.thinkbiganalytics.metadata.api.op.DataOperationCriteria)
     */
    public List<DataOperation> getDataOperations(DataOperationCriteria criteria) {
        OpCriteria critImpl = (OpCriteria) criteria;
        Iterator<DataOperation> filtered = Iterators.filter(this.operations.values().iterator(), critImpl);
        Iterator<DataOperation> limited = Iterators.limit(filtered, critImpl.getLimit());
        ArrayList<DataOperation> list = Lists.newArrayList(limited);
        
        Collections.sort(list, critImpl);
        return list;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#changeSetCriteria()
     */
    public DatasetCriteria DatasetCriteria() {
        return new ChangeCriteria();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getChangeSets(Datasource.ID)
     */
    @SuppressWarnings("unchecked")
    public List<Dataset<? extends Datasource, ? extends ChangeSet>> getDatasets(Datasource.ID dsId) {
        return getDatasets(dsId, ALL_CHANGES);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getChangeSets(Datasource.ID, com.thinkbiganalytics.metadata.api.op.DatasetCriteria)
     */
    @SuppressWarnings("unchecked")
    public List<Dataset<? extends Datasource, ? extends ChangeSet>> getDatasets(Datasource.ID dsId, DatasetCriteria criteria) {
        ChangeCriteria critImpl = (ChangeCriteria) criteria;
        Iterator<Dataset<? extends Datasource, ? extends ChangeSet>> filtered 
            = Iterators.filter(this.changeSets.get(dsId).iterator(), critImpl);
        Iterator<Dataset<? extends Datasource, ? extends ChangeSet>> limited 
            = Iterators.limit(filtered, critImpl.getLimit());
        ArrayList<Dataset<? extends Datasource, ? extends ChangeSet>> list = Lists.newArrayList(limited);

        Collections.sort(list, critImpl);
        return list;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.datasource.Datasource, com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    public void addListener(DataChangeEventListener<Datasource, ChangeSet> listener) {
        this.dispatcher.addListener(listener);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource, com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    public void addListener(DirectoryDatasource ds, DataChangeEventListener<DirectoryDatasource, FileList> listener) {
        this.dispatcher.addListener(ds, listener);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource, com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    public void addListener(HiveTableDatasource ds, DataChangeEventListener<HiveTableDatasource, HiveTableUpdate> listener) {
        this.dispatcher.addListener(ds, listener);
    }

    
    private static class OpCriteria extends AbstractMetadataCriteria<DataOperationCriteria> 
        implements DataOperationCriteria, Predicate<DataOperation>, Comparator<DataOperation> {

        private Feed.ID feedId;
        private Datasource.ID datasetId;
        private Set<State> states = new HashSet<>();
        private Set<Class<? extends Datasource>> types = new HashSet<>();
        
        @Override
        public boolean apply(DataOperation input) {
            if (this.feedId != null && ! this.feedId.equals(input.getProducer().getFeed().getId())) return false;
            if (this.datasetId != null && ! this.datasetId.equals(input.getDataset().getDatasource().getId())) return false;
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
        public int compare(DataOperation o1, DataOperation o2) {
            return ComparisonChain.start()
                    .compare(o2.getStopTime(), o1.getStopTime(), Ordering.natural().nullsFirst())
                    .compare(o2.getStartTime(), o1.getStartTime(), Ordering.natural().nullsLast())
                    .result();
        }
        
        @Override
        public DataOperationCriteria state(State... states) {
            for (State state : states) {
                this.states.add(state);
            }
            return this;
        }

        @Override
        public DataOperationCriteria feed(ID srcId) {
            this.feedId = srcId;
            return this;
        }

        @Override
        public DataOperationCriteria dataset(Datasource.ID dsId) {
            this.datasetId = dsId;
            return this;
        }

        @Override
        public DataOperationCriteria dataset(Class<? extends Datasource>... dsTypes) {
            for (Class<? extends Datasource> type : dsTypes) {
                this.types.add(type);
            }
            return this;
        }
    }
    
    private static class ChangeCriteria extends AbstractMetadataCriteria<DatasetCriteria> 
        implements DatasetCriteria, Predicate<Dataset<? extends Datasource, ? extends ChangeSet>>,
                   Comparator<Dataset<? extends Datasource, ? extends ChangeSet>> {

        private Set<ChangeType> types = new HashSet<>();
        private DateTime changedOn;
        private DateTime changedAfter;
        private DateTime changedBefore;
        
        @Override
        public boolean apply(Dataset<?, ?> input) {
            if (this.changedOn != null && ! this.changedOn.equals(input.getTime())) return false;
            if (this.changedAfter != null && ! this.changedAfter.isBefore(input.getTime())) return false;
            if (this.changedBefore != null && ! this.changedBefore.isBefore(input.getTime())) return false;
            if (this.types.size() > 0 && ! this.types.contains(input.getType())) return false;
            return true;
        }
        
        @Override
        public int compare(Dataset<? extends Datasource, ? extends ChangeSet> o1,
                           Dataset<? extends Datasource, ? extends ChangeSet> o2) {
            return o2.getTime().compareTo(o1.getTime());
        }

        @Override
        public DatasetCriteria type(ChangeType... types) {
            for (ChangeType type : types) {
                this.types.add(type);
            }
            return null;
        }

        @Override
        public ChangeCriteria changedOn(DateTime time) {
            this.changedOn = time;
            return this;
        }

        @Override
        public ChangeCriteria changedAfter(DateTime time) {
            this.changedAfter = time;
            return this;
        }

        @Override
        public ChangeCriteria changedBefore(DateTime time) {
            this.changedBefore = time;
            return this;
        }
    }
}
