/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface ChangeSetCriteria {

    ChangeSetCriteria type(ChangeSet.ChangeType... types);
    ChangeSetCriteria changedOn(DateTime time);
    ChangeSetCriteria changedAfter(DateTime time);
    ChangeSetCriteria changedBefore(DateTime time);
}
