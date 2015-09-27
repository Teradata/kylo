/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;

/**
 *
 * @author Sean Felten
 */
public interface ObligationBuilder {

    ObligationBuilder description(String descr);
    
    ObligationBuilder metric(Metric metric);
    
    Obligation build();
    
    ServiceLevelAgreementBuilder add();
}
