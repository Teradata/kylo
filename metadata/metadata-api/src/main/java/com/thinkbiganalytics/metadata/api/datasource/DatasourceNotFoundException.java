/**
 * 
 */
package com.thinkbiganalytics.metadata.api.datasource;

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.api.datasource.Datasource.ID;

/**
 *
 * @author Sean Felten
 */
public class DatasourceNotFoundException extends MetadataException {

    private static final long serialVersionUID = -1787057996932331481L;
    
    private Datasource.ID id;

    public DatasourceNotFoundException(ID id) {
        super();
        this.id = id;
    }
    
    public DatasourceNotFoundException(String message, ID id) {
        super(message);
        this.id = id;
    }
    
    public Datasource.ID getId() {
        return id;
    }
}
