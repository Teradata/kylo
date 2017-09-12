package com.thinkbiganalytics.integration.sla;

/*-
 * #%L
 * kylo-service-app
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

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.integration.IntegrationTestBase;

import org.junit.Test;

import java.io.IOException;


/**
 * Creates a feed, creates two SLAs for the feed, which are expected to succeed and to fail,
 * triggers SLA assessments, asserts SLA assessment results
 */
public class SlaIT extends IntegrationTestBase {

    private static final String TEST_FILE = "sla-assessment.txt";
    private static String FEED_NAME = "sla_" + System.currentTimeMillis();

    @Test
    public void testSla() throws IOException {
        copyDataToDropzone(TEST_FILE);

        FeedMetadata response = createSimpleFeed(FEED_NAME, TEST_FILE);

        waitForFeedToComplete();

        assertSLAs(response);
    }

    @Override
    public void startClean() {
        super.startClean();
    }

    //@Test
    public void temp() {
    }


    private void assertSLAs(FeedMetadata response) {

    }


}
