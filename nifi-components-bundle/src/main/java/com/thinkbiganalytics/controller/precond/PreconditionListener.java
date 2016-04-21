/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import com.thinkbiganalytics.metadata.rest.model.event.DatasourceChangeEvent;

/**
 *
 * @author Sean Felten
 */
public interface PreconditionListener {

    void triggered(DatasourceChangeEvent event);
}
