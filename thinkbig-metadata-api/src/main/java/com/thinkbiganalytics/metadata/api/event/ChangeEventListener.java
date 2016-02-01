/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public interface ChangeEventListener<D extends Dataset, C extends ChangedContent> {

    void handleEvent(ChangeEvent<D, C> event);
}
