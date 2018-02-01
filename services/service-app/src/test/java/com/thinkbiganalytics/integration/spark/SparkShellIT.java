package com.thinkbiganalytics.integration.spark;

import com.jayway.restassured.response.Response;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.spark.rest.controller.SparkShellProxyController;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

public class SparkShellIT extends IntegrationTestBase {

    /**
     * Verify message for compile errors.
     */
    @Test
    public void testCompileError() {
        final TransformResponse response = executeScript("import org.apache.spark.sql._\nfunctions.abs(-1)", HTTP_INTERNAL_ERROR);
        Assert.assertEquals(TransformResponse.Status.ERROR, response.getStatus());
        Assert.assertEquals("error: type mismatch; in <console> at line number 8", response.getMessage());
    }

    /**
     * Verify message for runtime errors.
     */
    @Test
    public void testRuntimeError() {
        final TransformResponse response = executeScript("sqlContext.sql(\"SELECT * FROM invalid-table-name\")", HTTP_INTERNAL_ERROR);
        Assert.assertEquals(TransformResponse.Status.ERROR, response.getStatus());
        Assert.assertEquals("AnalysisException: cannot recognize input near 'invalid' '-' 'table' in from source; line 1 pos 21", response.getMessage());
    }

    @Override
    protected void cleanup() {
        // nothing to do
    }

    /**
     * Executes the specified script synchronously.
     *
     * @param script             the Scala script
     * @param expectedStatusCode the expected HTTP status code
     * @return the result
     */
    private TransformResponse executeScript(@Nonnull final String script, final int expectedStatusCode) {
        // Create request body
        final TransformRequest request = new TransformRequest();
        request.setScript(script);

        // Execute request
        final Response response = given(SparkShellProxyController.BASE)
            .body(request)
            .when()
            .post(SparkShellProxyController.TRANSFORM);

        // Verify response
        response.then().statusCode(expectedStatusCode);
        return response.as(TransformResponse.class);
    }
}
