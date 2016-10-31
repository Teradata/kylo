package com.thinkbiganalytics.policy.precondition;

import java.util.List;

/**
 * Created by sr186054 on 10/25/16.
 */
public interface DependentFeedPrecondition {

    /**
     * return a list of the <systemCategory.systemFeedName> of the feeds that this feed is dependant upon.
     */
    List<String> getDependentFeedNames();

}
