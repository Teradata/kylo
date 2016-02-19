/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 *
 * @author Sean Felten
 */
public interface ChangedContent extends Serializable {
    
    DateTime getIntrinsicTime();
    
    Period getIntrinsicPeriod();
    
    int getCompletenessFactor();

}
