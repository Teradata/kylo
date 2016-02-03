/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 *
 * @author Sean Felten
 */
public interface ChangedContent {
    
    DateTime getIntrinsicTime();
    
    Period getIntrinsicPeriod();
    
    int getCompletenessFactor();

}
