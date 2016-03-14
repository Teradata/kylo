/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;

/**
 *
 * @author Sean Felten
 */
public interface ChangeSetCriteria extends MetadataCriteria<ChangeSetCriteria> {

    ChangeSetCriteria type(ChangeSet.ChangeType... types);
    ChangeSetCriteria changedOn(DateTime time);
    ChangeSetCriteria changedAfter(DateTime time);
    ChangeSetCriteria changedBefore(DateTime time);
}
