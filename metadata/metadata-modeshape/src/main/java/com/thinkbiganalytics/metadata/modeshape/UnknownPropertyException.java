/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

/**
 *
 * @author Sean Felten
 */
public class UnknownPropertyException extends RuntimeException {

    private static final long serialVersionUID = 4878659919254883237L;
    
    private final String propertyName;
    

    /**
     * @param message
     */
    public UnknownPropertyException(String propName) {
        super("Unknown property: " + propName);
        this.propertyName = propName;
    }

    /**
     * @param propName
     * @param cause
     */
    public UnknownPropertyException(String propName, Throwable cause) {
        super("Unknown property: " + propName, cause);
        this.propertyName = propName;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
}
