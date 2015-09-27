package com.thinkbiganalytics.metadata.sla.api;

import java.io.Serializable;
import java.util.Set;

public interface Obligation {

    String getDescription();
    
    ServiceLevelAgreement getSLA();
    
    Set<Metric> getMetrics();

}
