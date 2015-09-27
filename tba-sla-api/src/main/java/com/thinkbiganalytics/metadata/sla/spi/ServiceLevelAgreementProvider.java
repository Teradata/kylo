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
    
    List<ServiceLevelAgreement> getSLAs();
    
    ServiceLevelAgreement getSLA(ServiceLevelAgreement.ID id);
    
    ServiceLevelAgreement remove(ServiceLevelAgreement.ID id);

    ServiceLevelAgreementBuilder builder();
    
}
