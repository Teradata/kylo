/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

/**
 *
 * @author Sean Felten
 */
public class AgreementNotFoundException extends ServiceLevelAgreementException {

    private static final long serialVersionUID = 3471025919178590016L;
    
    private final ServiceLevelAgreement.ID id;
    
    /**
     * @param message
     */
    public AgreementNotFoundException(ServiceLevelAgreement.ID id) {
        this("No service level agreement was found with the specified ID", id);
    }
    
    /**
     * @param message
     */
    public AgreementNotFoundException(String message, ServiceLevelAgreement.ID id) {
        super(message);
        this.id = id;
    }

    public ServiceLevelAgreement.ID getId() {
        return id;
    }
}
