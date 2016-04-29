/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 *
 * @author Sean Felten
 */
public class DatasourceAlreadyExistsException extends MetadataException {

    private static final long serialVersionUID = -4192283206171412498L;

    /**
     * @param message
     */
    public DatasourceAlreadyExistsException(String message) {
        super(message);
    }

}
