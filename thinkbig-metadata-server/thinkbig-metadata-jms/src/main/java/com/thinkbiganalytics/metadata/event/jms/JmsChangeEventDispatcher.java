/**
 * 
 */
package com.thinkbiganalytics.metadata.event.jms;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.event.ChangeEventDispatcher;

/**
 *
 * @author Sean Felten
 */
public class JmsChangeEventDispatcher implements ChangeEventDispatcher {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.event.ChangeEventDispatcher#addListener(com.thinkbiganalytics.metadata.api.dataset.Dataset, com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    public <D extends Dataset, C extends ChangedContent> void addListener(
            D dataset, DataChangeEventListener<D, C> listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.event.ChangeEventDispatcher#addListener(com.thinkbiganalytics.metadata.api.event.DataChangeEventListener)
     */
    @Override
    public <D extends Dataset, C extends ChangedContent> void addListener(DataChangeEventListener<D, C> listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.event.ChangeEventDispatcher#nofifyChange(com.thinkbiganalytics.metadata.api.op.ChangeSet)
     */
    @Override
    public <D extends Dataset, C extends ChangedContent> void nofifyChange(ChangeSet<D, C> change) {
        // TODO Auto-generated method stub

    }

}
