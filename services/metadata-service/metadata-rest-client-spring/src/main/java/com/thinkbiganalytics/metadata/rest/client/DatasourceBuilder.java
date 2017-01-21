/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;

/**
 *
 * @author Sean Felten
 */
public interface DatasourceBuilder<B extends DatasourceBuilder<B, D>, D extends Datasource> {

    B description(String descr);
    B ownder(String owner);
    B encrypted(boolean flag);
    B compressed(boolean flag);
    
    D build();
    D post();
}
