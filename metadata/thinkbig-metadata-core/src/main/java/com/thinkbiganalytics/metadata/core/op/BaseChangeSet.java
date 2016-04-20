/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public class BaseChangeSet implements ChangeSet {

    private DateTime intrinsicTime;
    private Period intrinsicPeriod;
    private int completenessFactor;

    @Override
    public DateTime getIntrinsicTime() {
        return this.intrinsicTime;
    }

    @Override
    public Period getIntrinsicPeriod() {
        return this.intrinsicPeriod;
    }

    @Override
    public int getCompletenessFactor() {
        return this.completenessFactor;
    }

}
