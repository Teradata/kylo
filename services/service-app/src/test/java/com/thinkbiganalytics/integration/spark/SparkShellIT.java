package com.thinkbiganalytics.integration.spark;

/*-
 * #%L
 * kylo-service-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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
        Assert.assertEquals("error: type mismatch; in <console> at line number 9", response.getMessage());
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
