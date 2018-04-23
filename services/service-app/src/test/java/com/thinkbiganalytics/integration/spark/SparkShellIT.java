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

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.spark.rest.controller.SparkShellProxyController;
import com.thinkbiganalytics.spark.rest.model.JdbcDatasource;
import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

public class SparkShellIT extends IntegrationTestBase {

    /**
     * Sample script for tests.
     */
    private static final String SCRIPT = "import org.apache.spark.sql._\n"
                                         + "import org.apache.spark.sql.types._\n"
                                         + "val data = Seq(Row(1, \"a\"), Row(2, \"b\"), Row(3, \"c\"))\n"
                                         + "val schema = Seq(StructField(\"id\", IntegerType), StructField(\"value\", StringType))\n"
                                         + "sqlContext.createDataFrame(sc.parallelize(data), StructType(schema))";

    /**
     * Timeout for executing scripts or queries.
     */
    private static final long TIMEOUT = 30;

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

    /**
     * Verify downloading the result of a script.
     */
    @Test
    public void testScriptDownload() throws Exception {
        final TransformResponse transform = executeScript(SCRIPT, true);
        final SaveResponse save = saveScript(transform.getTable(), "json", null, null);
        Assert.assertEquals(SaveResponse.Status.SUCCESS, save.getStatus());
        Assert.assertEquals("./zip", save.getLocation());

        final InputStream stream = given(SparkShellProxyController.BASE)
            .accept(ContentType.ANY)
            .get(SparkShellProxyController.TRANSFORM_DOWNLOAD, transform.getTable(), save.getId())
            .then().statusCode(HTTP_OK)
            .extract().asInputStream();
        final ZipInputStream zip = new ZipInputStream(stream);

        final ZipEntry success = zip.getNextEntry();
        Assert.assertEquals("_SUCCESS", success.getName());
        Assert.assertEquals(-1, zip.read());
        zip.closeEntry();

        zip.getNextEntry();
        Assert.assertEquals("{\"id\":1,\"value\":\"a\"}\n{\"id\":2,\"value\":\"b\"}\n{\"id\":3,\"value\":\"c\"}\n", CharStreams.toString(new InputStreamReader(zip)));
        zip.closeEntry();

        Assert.assertNull("Expected no more zip entries", zip.getNextEntry());
    }

    /**
     * Verify saving the result of a script.
     */
    @Test
    public void testScriptSave() {
        final TransformResponse transform = executeScript(SCRIPT, true);

        final String tableName = newTableName();
        final SaveResponse save = saveScript(transform.getTable(), "orc", null, tableName);
        Assert.assertEquals(SaveResponse.Status.SUCCESS, save.getStatus());

        try {
            final List<HashMap<String, String>> table = getHiveQuery("SELECT * FROM " + tableName);
            Assert.assertEquals(ImmutableList.of(
                ImmutableMap.of("id", 1, "value", "a"),
                ImmutableMap.of("id", 2, "value", "b"),
                ImmutableMap.of("id", 3, "value", "c")
            ), table);
        } finally {
            runCommandOnRemoteSystem("hive -e \"drop table " + tableName + "\"", IntegrationTestBase.APP_HADOOP);
        }
    }

    @Override
    protected void cleanup() {
        // nothing to do
    }

    /**
     * Executes the specified script.
     *
     * @param script the Scala script to be executed
     * @param async  {@code true} to execute asynchronously
     * @return the result of the script
     */
    private TransformResponse executeScript(@Nonnull final String script, final boolean async) {
        return executeScript(script, async, HTTP_OK);
    }

    /**
     * Executes the specified script synchronously.
     *
     * @param script             the Scala script to be executed.
     * @param expectedStatusCode the expected response code
     * @return the result of the script
     */
    private TransformResponse executeScript(@Nonnull final String script, final int expectedStatusCode) {
        return executeScript(script, false, expectedStatusCode);
    }

    /**
     * Executes the specified script.
     *
     * @param script             the Scala script
     * @param async              {@code true} to execute asynchronously
     * @param expectedStatusCode the expected HTTP status code
     * @return the result
     */
    private TransformResponse executeScript(@Nonnull final String script, final boolean async, final int expectedStatusCode) {
        // Create request body
        final TransformRequest request = new TransformRequest();
        request.setAsync(async);
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

    /**
     * Generates a new, unique table name.
     *
     * @return the table name
     * @throws IllegalStateException if a table name cannot be generated
     */
    private String newTableName() {
        for (int i = 0; i < 100; ++i) {
            final String name = UUID.randomUUID().toString();
            if (name.matches("^[a-fA-F].*")) {
                return name.replace("-", "");
            }
        }
        throw new IllegalStateException("Unable to generate a new table name");
    }

    /**
     * Saves the result of a script or query.
     *
     * @param id          the transform or query identifier
     * @param requestPath the URL path to initiate the save
     * @param resultPath  the URL path to get the result
     * @param request     the save request
     * @return the save response
     */
    private SaveResponse save(@Nonnull final String id, @Nonnull final String requestPath, @Nonnull final String resultPath, @Nonnull final SaveRequest request) {
        // Execute request
        Response response = given(SparkShellProxyController.BASE)
            .body(request)
            .when()
            .post(requestPath, id);
        SaveResponse save = null;
        Stopwatch timeout = Stopwatch.createStarted();

        do {
            if (save != null) {
                response = given(SparkShellProxyController.BASE).get(resultPath, id, save.getId());
            }

            save = response.as(SaveResponse.class);
            if (save.getStatus() == SaveResponse.Status.PENDING && TIMEOUT - timeout.elapsed(TimeUnit.SECONDS) > 1) {
                waitFor(1, TimeUnit.SECONDS, "for save to finish");
            }
        } while (save.getStatus() == SaveResponse.Status.PENDING && timeout.elapsed(TimeUnit.SECONDS) < TIMEOUT);

        // Verify response
        response.then().statusCode(HTTP_OK);
        return response.as(SaveResponse.class);
    }

    /**
     * Saves the result of a script.
     *
     * @param transformId the transform identifier
     * @param format      the output format
     * @param jdbc        the target JDBC data source
     * @param tableName   the target table name
     * @return the save response
     */
    private SaveResponse saveScript(@Nonnull final String transformId, @Nullable final String format, @Nullable final JdbcDatasource jdbc, @Nullable final String tableName) {
        // Create request body
        final SaveRequest request = new SaveRequest();
        request.setFormat(format);
        request.setJdbc(jdbc);
        request.setTableName(tableName);

        // Execute request
        return save(transformId, SparkShellProxyController.TRANSFORM_SAVE, SparkShellProxyController.TRANSFORM_SAVE_RESULT, request);
    }
}
