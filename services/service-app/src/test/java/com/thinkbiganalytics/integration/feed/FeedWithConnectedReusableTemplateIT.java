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
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.integration.template.ImportConnectedReusableTemplateIT;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.ExitStatus;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class FeedWithConnectedReusableTemplateIT extends ImportConnectedReusableTemplateIT {
    private static final Logger LOG = LoggerFactory.getLogger(FeedWithConnectedReusableTemplateIT.class);


    @Test
    public void testConnectedFeed() throws Exception{
        ConnectedTemplate connectedTemplate = registerConnectedReusableTemplate();
        URL resource = IntegrationTestBase.class.getResource("feedconnectedflow.template.zip");
        //Create a feed using this template
        //Register simple feed template

        ImportTemplate template = importTemplate(resource.getPath());

        FeedCategory feedCategory = createCategory("connected flows");
        //create a feed using this template
        FeedMetadata feed = FeedTestUtil.getCreateGenerateFlowFileFeedRequest(feedCategory, template, "connected_feed_"+System.currentTimeMillis());

        NifiFeed createdFeed = createFeed(feed);
        Assert.assertNotNull(createdFeed);

        waitForFeedToComplete();
        assertExecutedJobs(feed);




    }

    private void assertExecutedJobs(FeedMetadata feed) throws IOException {
        DefaultExecutedJob[] jobs = getJobs(feed.getCategoryAndFeedName().toLowerCase());

        DefaultExecutedJob ingest = Arrays.stream(jobs).findFirst().orElse(null);
        Assert.assertNotNull(ingest);
        Assert.assertEquals(ExecutionStatus.COMPLETED, ingest.getStatus());
        Assert.assertEquals(ExitStatus.COMPLETED.getExitCode(), ingest.getExitCode());

        LOG.info("Asserting user data jobs has expected number of steps");
        DefaultExecutedJob job = getJobWithSteps(ingest.getExecutionId());
        Assert.assertEquals(ingest.getExecutionId(), job.getExecutionId());
        List<ExecutedStep> steps = job.getExecutedSteps();
        Assert.assertEquals(8, steps.size());
        for (ExecutedStep step : steps) {
            Assert.assertEquals(ExitStatus.COMPLETED.getExitCode(), step.getExitCode());
        }


    }

}
