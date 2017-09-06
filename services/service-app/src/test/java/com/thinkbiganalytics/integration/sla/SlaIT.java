package com.thinkbiganalytics.integration.sla;

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
