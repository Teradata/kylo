/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

/**
 *
 * @author Sean Felten
 */
public class DataOperationNotFound extends RuntimeException {

    private static final long serialVersionUID = 830649193095358862L;
    
    private final DataOperation.ID id;
    
    /**
     * @param message
     */
    public DataOperationNotFound(DataOperation.ID id) {
        this("The operation with the given ID does not exist", id);
    }
    
    /**
     * @param message
     */
    public DataOperationNotFound(String message, DataOperation.ID id) {
        super(message);
        this.id = id;
    }

    public DataOperation.ID getId() {
        return id;
    }
}
