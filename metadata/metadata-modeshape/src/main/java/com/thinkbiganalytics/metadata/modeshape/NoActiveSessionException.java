/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

/**
 *
 * @author Sean Felten
 */
public class NoActiveSessionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public NoActiveSessionException() {
        this("There is no active JCR session");
    }

    /**
     * @param message
     */
    public NoActiveSessionException(String message) {
        super(message);
    }
}
