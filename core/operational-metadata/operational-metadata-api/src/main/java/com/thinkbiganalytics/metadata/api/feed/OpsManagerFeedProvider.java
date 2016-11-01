package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 9/15/16.
 */
public interface OpsManagerFeedProvider {


    OpsManagerFeed.ID resolveId(Serializable id);

    OpsManagerFeed findByName(String name);

    OpsManagerFeed findById(OpsManagerFeed.ID id);

    List<? extends OpsManagerFeed> findByFeedIds(List<OpsManagerFeed.ID> ids);

    void save(List<? extends OpsManagerFeed> feeds);

    OpsManagerFeed save(OpsManagerFeed.ID feedManagerId, String systemName);

    void delete(OpsManagerFeed.ID id);

    boolean isFeedRunning(OpsManagerFeed.ID id);


}
