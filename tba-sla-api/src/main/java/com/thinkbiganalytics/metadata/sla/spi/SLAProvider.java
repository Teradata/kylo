/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import java.io.Serializable;
import java.util.List;

import com.thinkbiganalytics.metadata.sla.api.SLA;

/**
 *
 * @author Sean Felten
 */
public interface SLAProvider {
    
    SLA.ID resolve(Serializable ser);
    
    List<SLA> getSLAs();
    
    SLA getSLA(SLA.ID id);
    
    SLA remove(SLA.ID id);

    SLABuilder builder();
    
}
