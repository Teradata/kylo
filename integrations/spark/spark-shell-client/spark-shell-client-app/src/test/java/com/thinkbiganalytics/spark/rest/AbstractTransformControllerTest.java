package com.thinkbiganalytics.spark.rest;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.thinkbiganalytics.spark.metadata.TransformJob;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.service.TransformService;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;

public class AbstractTransformControllerTest {

    /**
     * Verify requesting a transformation status.
     */
    @Test
    public void getTable() throws Exception {
        // Mock transform objects
        TransformJob pendingJob = Mockito.mock(TransformJob.class);
        Mockito.when(pendingJob.getGroupId()).thenReturn("PendingJob");
        Mockito.when(pendingJob.progress()).thenReturn(0.5);

        TransformJob successJob = Mockito.mock(TransformJob.class);
        TransformResponse successResponse = new TransformResponse();
        Mockito.when(successJob.get()).thenReturn(successResponse);
        Mockito.when(successJob.isDone()).thenReturn(true);

        TransformService transformService = Mockito.mock(TransformService.class);
        Mockito.when(transformService.getTransformJob("PendingJob")).thenReturn(pendingJob);
        Mockito.when(transformService.getTransformJob("SuccessJob")).thenReturn(successJob);

        // Test with pending job
        AbstractTransformController controller = new AbstractTransformController() {
        };
        controller.transformService = transformService;

        Response response = controller.getTable("PendingJob");
        Assert.assertEquals(Response.Status.OK, response.getStatusInfo());

        TransformResponse transformResponse = (TransformResponse) response.getEntity();
        Assert.assertEquals(0.5, transformResponse.getProgress(), 0.001);
        Assert.assertEquals(TransformResponse.Status.PENDING, transformResponse.getStatus());
        Assert.assertEquals("PendingJob", transformResponse.getTable());

        // Test with success job
        response = controller.getTable("SuccessJob");
        Assert.assertEquals(successResponse, response.getEntity());
        Assert.assertEquals(Response.Status.OK, response.getStatusInfo());
    }
}
