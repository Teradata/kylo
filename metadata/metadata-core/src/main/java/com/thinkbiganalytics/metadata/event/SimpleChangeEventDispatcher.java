/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.ResolvableType;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.Datasource.ID;
import com.thinkbiganalytics.metadata.api.event.DataChangeEvent;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public class SimpleChangeEventDispatcher implements ChangeEventDispatcher {

    private Map<DataChangeEventListener<? extends Datasource, ? extends ChangeSet>, ListenerMatcher> listeners = new HashMap<>();
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.thinkbiganalytics.metadata.event.ChangeEventDispatcher#addListener(
     * com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <D extends Datasource, C extends ChangeSet> void addListener(DataChangeEventListener<D, C> listener) {
        ResolvableType type = ResolvableType.forClass(DataChangeEventListener.class, listener.getClass());
        Class<? extends Datasource> datasetType = (Class<? extends Datasource>) type.resolveGeneric(0);
        Class<? extends ChangeSet> contentType = (Class<? extends ChangeSet>) type.resolveGeneric(1);
        ListenerMatcher m = new ListenerMatcher(datasetType, contentType);
        
        this.listeners.put(listener, m);
    }
    
    @Override
    public <D extends Datasource, C extends ChangeSet> void addListener(D dataset, DataChangeEventListener<D, C> listener) {
        ResolvableType type = ResolvableType.forClass(DataChangeEventListener.class, listener.getClass());
        Class<? extends Datasource> datasetType = (Class<? extends Datasource>) type.resolveGeneric(0);
        Class<? extends ChangeSet> contentType = (Class<? extends ChangeSet>) type.resolveGeneric(1);
        ListenerMatcher m = new ListenerMatcher(dataset.getId(), datasetType, contentType);
        
        this.listeners.put(listener, m);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.thinkbiganalytics.metadata.event.ChangeEventDispatcher#nofifyChange(
     * com.thinkbiganalytics.metadata.api.op.Dataset)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <D extends Datasource, C extends ChangeSet> void nofifyChange(Dataset<D, C> change) {
        for (Entry<DataChangeEventListener<? extends Datasource, ? extends ChangeSet>, 
                ListenerMatcher> entry : this.listeners.entrySet()) {
            for (C content : change.getChanges()) {
                if (entry.getValue().matches(change.getDatasource(), content)) {
                    DataChangeEvent<D, C> event = new BaseDataChangeEvent<>(change);
                    DataChangeEventListener<D, C> listener = (DataChangeEventListener<D, C>) entry.getKey();
                    listener.handleEvent(event);
                }
            }
        }
            
    }

    private static class ListenerMatcher {
        private final Class<? extends Datasource> datasetType;
        private final Class<? extends ChangeSet> contentType;
        private final Datasource.ID datasetId;

        public ListenerMatcher(Class<? extends Datasource> datasetType, Class<? extends ChangeSet> contentType) {
            this(null, datasetType, contentType);
        }

        public ListenerMatcher(ID id, Class<? extends Datasource> datasetType, Class<? extends ChangeSet> contentType) {
            this.datasetType = datasetType;
            this.contentType = contentType;
            this.datasetId = id;
        }

        public boolean matches(Datasource dataset, ChangeSet content) {
            return this.datasetType.isAssignableFrom(dataset.getClass()) && 
                    this.contentType.isAssignableFrom(content.getClass()) &&
                    (this.datasetId == null || this.datasetId.equals(dataset.getId()));
        }
    }
}
