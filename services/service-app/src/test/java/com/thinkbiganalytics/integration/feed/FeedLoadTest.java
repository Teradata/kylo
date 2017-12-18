package com.thinkbiganalytics.integration.feed;

/*-
 * #%L
 * kylo-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test to load multiple feeds in a loop
 */
@Ignore
public class FeedLoadTest extends FeedIT {

    private static final Logger LOG = LoggerFactory.getLogger(FeedLoadTest.class);

    /**
     * Load the Data Ingest template
     * @throws Exception
     */
    //@Test
    //@Ignore
    public void loadTestDataIngest() throws Exception {
        // the num of categories to use
        int categories = 1;
        //the num of feeds to create in each category
        int maxFeedsInCategory = 20;
        //the category id to start.   categories are created as cat_#

        int startingCategoryId = 62;
        prepare();
        String templatePath = sampleTemplatesPath + DATA_INGEST_ZIP;
        loadTest(templatePath, "feed", categories, maxFeedsInCategory, startingCategoryId);
    }

    /**
     * Load the simple template with just 3 processors
     * @throws Exception
     */
   // @Test
   // @Ignore
    public void loadSimpleFeed() throws Exception {
        // the num of categories to use
        int categories = 50;
        //the num of feeds to create in each category. feeds are labled cat_#_feed_#.  it start creating feeds after the last feed in the category
        int maxFeedsInCategory = 20;

        //the category id to start.   categories are created as cat_#
        int startingCategoryId = 3;
        prepare();
        String templatePath = getClass().getClassLoader().getResource("com/thinkbiganalytics/integration/simple_template.template.zip").toURI().getPath();
        loadTest(templatePath, "simple_feed", categories, maxFeedsInCategory, startingCategoryId);
    }

    /**
     * Bulk load a template into NiFi and Kylo
     * categories are named  'category_#
     * feeds are named  cat_#_feedName_#
     *
     * @param templatePath the template to use
     * @param feedName the name of the feed
     * @param categories the number of categories to use/create
     * @param feedsInCategory the number of feeds to create in each category
     * @param startingCategoryId the category number to use for the first category
     * @throws Exception
     */
    private void loadTest(String templatePath, String feedName, int categories, int feedsInCategory, int startingCategoryId) throws Exception {

        ImportTemplate ingestTemplate = importTemplate(templatePath);

        for (int i = startingCategoryId; i < (startingCategoryId + categories); i++) {
            //create new category
            String categoryName = "category_" + i;
            FeedCategory category = getorCreateCategoryByName(categoryName);
            Integer maxFeedId = category.getRelatedFeeds();
            maxFeedId += 1;
            for (int j = maxFeedId; j < (feedsInCategory + maxFeedId); j++) {
                try {

                    String updateFeedName = "cat_" + i + "_" + feedName + "_" + j;
                    FeedMetadata feed = getCreateFeedRequest(category, ingestTemplate, updateFeedName);
                    long start = System.currentTimeMillis();
                    FeedMetadata response = createFeed(feed).getFeedMetadata();
                    LOG.info("Time to save: {} was: {} ms ", updateFeedName, (System.currentTimeMillis() - start));
                } catch (Exception e) {

                }

            }
        }

    }


    @Override
    public void startClean() {
        int i = 0;
        //   super.startClean();
    }

    @Override
    public void cleanup() {
        //noop
        int i = 0;
    }
}
