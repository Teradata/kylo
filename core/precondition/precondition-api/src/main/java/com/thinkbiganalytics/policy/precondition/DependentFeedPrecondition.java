package com.thinkbiganalytics.policy.precondition;

import java.util.List;

/**
 * Base class used for the precondition where a Feed depends upon the successful completion of another feed
 *
 * Refer to the precondition-default module for implementations of this interface
 */
public interface DependentFeedPrecondition {

    /**
     *
     * @return a list of the <systemCategory.systemFeedName> of the feeds that a feed depends upon
     */
    List<String> getDependentFeedNames();

}
