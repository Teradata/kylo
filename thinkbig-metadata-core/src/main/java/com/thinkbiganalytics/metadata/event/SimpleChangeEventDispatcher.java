/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.ResolvableType;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.Dataset.ID;
import com.thinkbiganalytics.metadata.api.event.DataChangeEvent;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public class SimpleChangeEventDispatcher implements ChangeEventDispatcher {

    private Map<DataChangeEventListener<? extends Dataset, ? extends ChangedContent>, ListenerMatcher> listeners = new HashMap<>();
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.thinkbiganalytics.metadata.event.ChangeEventDispatcher#addListener(
     * com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <D extends Dataset, C extends ChangedContent> void addListener(DataChangeEventListener<D, C> listener) {
        ResolvableType type = ResolvableType.forClass(DataChangeEventListener.class, listener.getClass());
        Class<? extends Dataset> datasetType = (Class<? extends Dataset>) type.resolveGeneric(0);
        Class<? extends ChangedContent> contentType = (Class<? extends ChangedContent>) type.resolveGeneric(1);
        ListenerMatcher m = new ListenerMatcher(datasetType, contentType);
        
        this.listeners.put(listener, m);
    }
    
    @Override
    public <D extends Dataset, C extends ChangedContent> void addListener(D dataset, DataChangeEventListener<D, C> listener) {
        ResolvableType type = ResolvableType.forClass(DataChangeEventListener.class, listener.getClass());
        Class<? extends Dataset> datasetType = (Class<? extends Dataset>) type.resolveGeneric(0);
        Class<? extends ChangedContent> contentType = (Class<? extends ChangedContent>) type.resolveGeneric(1);
        ListenerMatcher m = new ListenerMatcher(dataset.getId(), datasetType, contentType);
        
        this.listeners.put(listener, m);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.thinkbiganalytics.metadata.event.ChangeEventDispatcher#nofifyChange(
     * com.thinkbiganalytics.metadata.api.op.ChangeSet)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <D extends Dataset, C extends ChangedContent> void nofifyChange(ChangeSet<D, C> change) {
        for (Entry<DataChangeEventListener<? extends Dataset, ? extends ChangedContent>, 
                ListenerMatcher> entry : this.listeners.entrySet()) {
            for (C content : change.getChanges()) {
                if (entry.getValue().matches(change.getDataset(), content)) {
                    DataChangeEvent<D, C> event = new BaseDataChangeEvent<>(change);
                    DataChangeEventListener<D, C> listener = (DataChangeEventListener<D, C>) entry.getKey();
                    listener.handleEvent(event);
                }
            }
        }
            
    }

    private static class ListenerMatcher {
        private final Class<? extends Dataset> datasetType;
        private final Class<? extends ChangedContent> contentType;
        private final Dataset.ID datasetId;

        public ListenerMatcher(Class<? extends Dataset> datasetType, Class<? extends ChangedContent> contentType) {
            this(null, datasetType, contentType);
        }

        public ListenerMatcher(ID id, Class<? extends Dataset> datasetType, Class<? extends ChangedContent> contentType) {
            this.datasetType = datasetType;
            this.contentType = contentType;
            this.datasetId = id;
        }

        public boolean matches(Dataset dataset, ChangedContent content) {
            return this.datasetType.isAssignableFrom(dataset.getClass()) && 
                    this.contentType.isAssignableFrom(content.getClass()) &&
                    (this.datasetId == null || this.datasetId.equals(dataset.getId()));
        }
    }
}
