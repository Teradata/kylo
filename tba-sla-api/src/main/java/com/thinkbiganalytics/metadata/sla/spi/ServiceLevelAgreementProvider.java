/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import java.io.Serializable;
import java.util.List;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public interface ServiceLevelAgreementProvider {
    
    ServiceLevelAgreement.ID resolve(Serializable ser);
    
    List<ServiceLevelAgreement> getAgreements();
    
    ServiceLevelAgreement getAgreement(ServiceLevelAgreement.ID id);
    
    ServiceLevelAgreement removeAgreement(ServiceLevelAgreement.ID id);

    ServiceLevelAgreementBuilder builder();
    
    ServiceLevelAgreementBuilder builder(ServiceLevelAgreement.ID id);
    
}
