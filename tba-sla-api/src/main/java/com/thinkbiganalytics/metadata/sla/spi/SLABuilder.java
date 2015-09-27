/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.SLA;

/**
 *
 * @author Sean Felten
 */
public interface SLABuilder {

    SLABuilder name(String name);
    
    SLABuilder description(String description);
    
    SLABuilder obligation(Obligation obligation);
    
    ObligationBuilder obligationBuilder();
    
    SLA build();
}
