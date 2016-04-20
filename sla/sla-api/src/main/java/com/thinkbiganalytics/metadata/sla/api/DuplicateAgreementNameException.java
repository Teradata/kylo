/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

/**
 *
 * @author Sean Felten
 */
public class DuplicateAgreementNameException extends ServiceLevelAgreementException {

    private static final long serialVersionUID = -4161629217364927555L;
    
    private final String name;
    
    /**
     * @param name the duplicate name that was tried.
     */
    public DuplicateAgreementNameException(String name) {
        this("A service level agreement already exists with the given name", name);
    }

    /**
     * @param message the messate
     * @param name the duplicate name that was tried.
     */
    public DuplicateAgreementNameException(String message, String name) {
        super(message);
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

}
