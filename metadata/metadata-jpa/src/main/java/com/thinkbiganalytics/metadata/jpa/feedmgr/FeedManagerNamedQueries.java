package com.thinkbiganalytics.metadata.jpa.feedmgr;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerNamedQueries {
      String FEED_FIND_BY_SYSTEM_NAME = "FeedManagerFeed.findBySystemName";
      String FEED_FIND_BY_TEMPLATE_ID = "FeedManagerFeed.findByTemplateId";
      String FEED_FIND_BY_CATEGORY_ID = "FeedManagerFeed.findByCategoryId";

      String CATEGORY_FIND_BY_SYSTEM_NAME ="FeedManagerCategory.findBySystemName";

      String TEMPLATE_FIND_BY_NAME ="FeedManagerTemplate.findByName";
      String TEMPLATE_FIND_BY_NIFI_ID = "FeedManagerTemplate.findByNifiTemplateId";
}
