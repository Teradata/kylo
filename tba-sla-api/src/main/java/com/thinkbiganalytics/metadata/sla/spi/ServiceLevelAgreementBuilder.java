/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public interface ServiceLevelAgreementBuilder {

    ServiceLevelAgreementBuilder name(String name);
    
    ServiceLevelAgreementBuilder description(String description);
    
    ObligationBuilder obligationBuilder();
    
    ServiceLevelAgreement build();
}
