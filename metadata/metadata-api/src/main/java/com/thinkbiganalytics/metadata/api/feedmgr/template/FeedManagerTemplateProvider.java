package com.thinkbiganalytics.metadata.api.feedmgr.template;

import com.thinkbiganalytics.metadata.api.BaseProvider;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerTemplateProvider  extends BaseProvider<FeedManagerTemplate,FeedManagerTemplate.ID> {

     FeedManagerTemplate findByName(String name);

     FeedManagerTemplate findByNifiTemplateId(String nifiTemplateId);

     FeedManagerTemplate ensureTemplate(String name);

     FeedManagerTemplate enable(FeedManagerTemplate.ID id);

     FeedManagerTemplate disable(FeedManagerTemplate.ID id);

     boolean deleteTemplate(FeedManagerTemplate.ID id) throws TemplateDeletionException;

}
