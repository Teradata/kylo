/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.dataset.ChangedContent;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface ChangeEventListener<D extends Dataset, C extends ChangedContent> {

    void handleEvent(ChangeEvent<D, C> event);
}
