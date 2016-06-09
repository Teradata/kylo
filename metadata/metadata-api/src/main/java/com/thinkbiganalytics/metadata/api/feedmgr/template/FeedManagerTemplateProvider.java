package com.thinkbiganalytics.metadata.api.feedmgr.template;

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerTemplateProvider  extends BaseProvider<FeedManagerTemplate,FeedManagerTemplate.ID> {

     FeedManagerTemplate findByName(String name);

     FeedManagerTemplate findByNifiTemplateId(String nifiTemplateId);

     FeedManagerTemplate ensureTemplate(String name);
}
