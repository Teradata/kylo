/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.joda.time.DateTime;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.ChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangeSet.ChangeType;
import com.thinkbiganalytics.metadata.api.op.ChangeSetCriteria;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.op.DataOperationCriteria;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.core.dataset.files.BaseFileList;
import com.thinkbiganalytics.metadata.core.dataset.hive.BaseHiveTableUpdate;
import com.thinkbiganalytics.metadata.event.ChangeEventDispatcher;

/**
 *
 * @author Sean Felten
 */
public class InMemoryDataOperationsProvider implements DataOperationsProvider {
    
    @Inject
    private DatasetProvider datasetProvider;
    
    @Inject 
    private FeedProvider feedProvider;
    
    @Inject
    private ChangeEventDispatcher dispatcher;
    
    private Map<DataOperation.ID, BaseDataOperation> operations = new ConcurrentHashMap<>();
    private Map<Dataset.ID, Collection<BaseChangeSet<?, ?>>> changeSets = new ConcurrentHashMap<>();

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#beginOperation(com.thinkbiganalytics.metadata.api.feed.Feed.ID, Dataset.ID)
     */
    public DataOperation beginOperation(ID feedId, Dataset.ID dsId) {
        Feed feed = this.feedProvider.getFeed(feedId);
        Dataset ds = this.datasetProvider.getDataset(dsId);
        
        if (feed == null) {
            throw new DataOperationException("No feed with the given ID exists: " + feedId);
        }
        
        if (ds == null) {
            throw new DataOperationException("No dataset with the given ID exists: " + feedId);
        }
        
        return new BaseDataOperation(ds, feed);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.DataOperation.State)
     */
    public DataOperation updateOperation(DataOperation.ID id, String status, State state) {
        BaseDataOperation op = this.operations.get(id);
        
        if (op == null) {
            throw new DataOperationException("No operation with the given ID exists: " + id);
        }
        
        op = new BaseDataOperation(op, state, status);
        
        this.operations.put(id, op);
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
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#updateOperation(com.thinkbiganalytics.metadata.api.op.DataOperation.ID, java.lang.String, com.thinkbiganalytics.metadata.api.op.ChangeSet)
     */
    public DataOperation updateOperation(DataOperation.ID id, String status, ChangeSet<?, ?> changes) {
        BaseDataOperation op = this.operations.get(id);
        
        if (op == null) {
            throw new DataOperationException("No operation with the given ID exists: " + id);
        }
        
        op = new BaseDataOperation(op, status, changes);
        
        this.operations.put(id, op);
        return op;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createChangeSet(com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset, java.util.List)
     */
    public ChangeSet<DirectoryDataset, FileList> createChangeSet(DirectoryDataset ds, List<Path> paths) {
        BaseFileList content = new BaseFileList(paths);
        return new BaseChangeSet<DirectoryDataset, FileList>(ds, content);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#createChangeSet(com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset, int)
     */
    public ChangeSet<HiveTableDataset, HiveTableUpdate> createChangeSet(HiveTableDataset ds, int count) {
        BaseHiveTableUpdate content = new BaseHiveTableUpdate(count);
        return new BaseChangeSet<HiveTableDataset, HiveTableUpdate>(ds, content);
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
    public Collection<DataOperation> getDataOperations() {
        return new ArrayList<DataOperation>(this.operations.values());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getDataOperations(com.thinkbiganalytics.metadata.api.op.DataOperationCriteria)
     */
    public Collection<DataOperation> getDataOperations(DataOperationCriteria criteria) {
        OpCriteria critImpl = (OpCriteria) criteria;
        return new ArrayList<DataOperation>(Collections2.filter(this.operations.values(), critImpl));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#changeSetCriteria()
     */
    public ChangeSetCriteria changeSetCriteria() {
        return new ChangeCriteria();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getChangeSets(Dataset.ID)
     */
    public Collection<ChangeSet<?, ?>> getChangeSets(Dataset.ID dsId) {
        return new ArrayList<ChangeSet<?, ?>>(this.changeSets.get(dsId));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#getChangeSets(Dataset.ID, com.thinkbiganalytics.metadata.api.op.ChangeSetCriteria)
     */
    public Collection<ChangeSet<?, ?>> getChangeSets(Dataset.ID dsId, ChangeSetCriteria criteria) {
        ChangeCriteria critImpl = (ChangeCriteria) criteria;
        return new ArrayList<ChangeSet<?, ?>>(Collections2.filter(this.changeSets.get(dsId), critImpl));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset, com.thinkbiganalytics.metadata.api.event.ChangeEventListener)
     */
    public void addListener(DirectoryDataset ds, ChangeEventListener<DirectoryDataset, FileList> listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.DataOperationsProvider#addListener(com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset, com.thinkbiganalytics.metadata.api.event.ChangeEventListener)
     */
    public void addListener(HiveTableDataset ds, ChangeEventListener<HiveTableDataset, HiveTableUpdate> listener) {
        // TODO Auto-generated method stub

    }

    
    private static class OpCriteria implements DataOperationCriteria, Predicate<BaseDataOperation> {

        private Feed.ID sourceId;
        private Dataset.ID databaseId;
        private Set<State> states = new HashSet<>();
        private Set<Class<? extends Dataset>> types = new HashSet<>();
        
        @Override
        public boolean apply(BaseDataOperation input) {
            if (this.sourceId != null && ! this.sourceId.equals(input.getSource().getId())) return false;
            if (this.databaseId != null && ! this.databaseId.equals(input.getChangeSet().getDataset().getId())) return false;
            if (this.states.size() > 0 && ! this.states.contains(input.getState())) return false;
            if (this.types.size() > 0) {
                for (Class<? extends Dataset> type : types) {
                    if (type.isAssignableFrom(input.getClass())) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        @Override
        public DataOperationCriteria state(State... states) {
            for (State state : states) {
                this.states.add(state);
            }
            return this;
        }

        @Override
        public DataOperationCriteria source(ID srcId) {
            this.sourceId = srcId;
            return this;
        }

        @Override
        public DataOperationCriteria dataset(Dataset.ID dsId) {
            this.databaseId = dsId;
            return this;
        }

        @Override
        public DataOperationCriteria dataset(Class<? extends Dataset>... dsTypes) {
            for (Class<? extends Dataset> type : dsTypes) {
                this.types.add(type);
            }
            return this;
        }
    }
    
    private static class ChangeCriteria implements ChangeSetCriteria, Predicate<BaseChangeSet<?, ?>> {

        private Set<ChangeType> types = new HashSet<>();
        private DateTime changedOn;
        private DateTime changedAfter;
        private DateTime changedBefore;
        
        @Override
        public boolean apply(BaseChangeSet<?, ?> input) {
            if (this.changedOn != null && ! this.changedOn.equals(input.getTime())) return false;
            if (this.changedAfter != null && ! this.changedAfter.isBefore(input.getTime())) return false;
            if (this.changedBefore != null && ! this.changedBefore.isBefore(input.getTime())) return false;
            if (this.types.size() > 0 && ! this.types.contains(input.getType())) return false;
            return true;
        }

        @Override
        public ChangeSetCriteria type(ChangeType... types) {
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
