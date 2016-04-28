package com.thinkbiganalytics.spark.rest;

import com.thinkbiganalytics.spark.metadata.TransformRequest;
import com.thinkbiganalytics.spark.metadata.TransformResponse;
import com.thinkbiganalytics.spark.service.TransformService;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.script.ScriptException;
import javax.ws.rs.core.Response;

public class SparkShellControllerTest {

    /** Verify applying a Spark transformation. */
    @Test
    public void transform () throws Exception {
        // Mock transform objects
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setScript("sqlContext.sql(\"SELECT * FROM invalid\")");

        TransformResponse transformResponse = new TransformResponse();
        transformResponse.setStatus(TransformResponse.Status.SUCCESS);
        transformResponse.setTable("results");

        TransformService transformService = Mockito.mock(TransformService.class);
        Mockito.when(transformService.execute(transformRequest)).thenReturn(transformResponse);

        // Test transforming
        SparkShellController controller = new SparkShellController();
        controller.transformService = transformService;

        Response response = controller.transform(transformRequest);
        Assert.assertEquals(Response.Status.OK, response.getStatusInfo());
        Assert.assertEquals(transformResponse, response.getEntity());
    }

    /** Verify response if missing parent script. */
    @Test
    public void transformWithMissingParentScript () {
        // Create transform request
        TransformRequest request = new TransformRequest();
        request.setScript("parent");
        request.setParent(new TransformRequest.Parent());

        // Test missing parent script
        SparkShellController controller = new SparkShellController();
        Response response = controller.transform(request);
        Assert.assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());

        TransformResponse entity = (TransformResponse)response.getEntity();
        Assert.assertEquals("The parent must include a script with the transformations performed.", entity.getMessage());
        Assert.assertEquals(TransformResponse.Status.ERROR, entity.getStatus());
    }

    /** Verify response if missing parent table. */
    @Test
    public void transformWithMissingParentTable () {
        // Create transform request
        TransformRequest request = new TransformRequest();
        request.setScript("parent");

        TransformRequest.Parent parent = new TransformRequest.Parent();
        parent.setScript("sqlContext.sql(\"SELECT * FROM invalid\")");
        request.setParent(parent);

        // Test missing parent table
        SparkShellController controller = new SparkShellController();
        Response response = controller.transform(request);
        Assert.assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());

        TransformResponse entity = (TransformResponse)response.getEntity();
        Assert.assertEquals("The parent must include the table containing the results.", entity.getMessage());
        Assert.assertEquals(TransformResponse.Status.ERROR, entity.getStatus());
    }

    /** Verify response if missing script. */
    @Test
    public void transformWithMissingScript () {
        SparkShellController controller = new SparkShellController();
        Response response = controller.transform(new TransformRequest());
        Assert.assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());

        TransformResponse entity = (TransformResponse)response.getEntity();
        Assert.assertEquals("The request must include a script with the transformations to perform.", entity.getMessage());
        Assert.assertEquals(TransformResponse.Status.ERROR, entity.getStatus());
    }

    /** Verify response if a script exception is thrown. */
    @Test
    public void transformWithScriptException () throws Exception {
        // Create transform objects
        TransformRequest request = new TransformRequest();
        request.setScript("sqlContext.sql(\"SELECT * FROM invalid\")");

        TransformService transformService = Mockito.mock(TransformService.class);
        Mockito.when(transformService.execute(request)).thenThrow(new ScriptException("Invalid script"));

        // Test script exception
        SparkShellController controller = new SparkShellController();
        controller.transformService = transformService;

        Response response = controller.transform(request);
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR, response.getStatusInfo());

        TransformResponse entity = (TransformResponse)response.getEntity();
        Assert.assertEquals("Invalid script", entity.getMessage());
        Assert.assertEquals(TransformResponse.Status.ERROR, entity.getStatus());
    }
}
