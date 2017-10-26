package com.thinkbiganalytics.spark.service;

/*-
 * #%L
 * thinkbig-spark-shell-client-app
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

import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.Profiler;
import com.thinkbiganalytics.spark.datavalidator.DataValidator;
import com.thinkbiganalytics.spark.metadata.TransformScript;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import com.thinkbiganalytics.spark.rest.model.Datasource;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.DatasourceProvider;
import com.thinkbiganalytics.spark.shell.DatasourceProviderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import scala.tools.nsc.interpreter.NamedParam;

public class TransformServiceTest {

    /**
     * Verify executing a transformation request.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void execute() throws Exception {
        // Mock Spark context service
        final SparkContextService sparkContextService = Mockito.mock(SparkContextService.class);

        // Mock Spark script engine
        final SparkScriptEngine engine = Mockito.mock(SparkScriptEngine.class);
        Mockito.when(engine.eval(Mockito.anyString(), Mockito.anyListOf(NamedParam.class))).thenReturn(new MockTransformResult());
        Mockito.when(engine.getSparkContext()).thenReturn(Mockito.mock(SparkContext.class));

        // Test executing a request
        final TransformRequest request = new TransformRequest();
        request.setScript("sqlContext.range(1,10)");

        final TransformService service = new TransformService(TransformScript.class, engine, sparkContextService, new MockTransformJobTracker());
        final TransformResponse response = service.execute(request);
        Assert.assertEquals(TransformResponse.Status.SUCCESS, response.getStatus());

        // Test eval arguments
        final ArgumentCaptor<String> evalScript = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<List> evalBindings = ArgumentCaptor.forClass(List.class);
        Mockito.verify(engine).eval(evalScript.capture(), evalBindings.capture());

        final String expectedScript = IOUtils.toString(getClass().getResourceAsStream("transform-service-script1.scala"), "UTF-8");
        Assert.assertEquals(expectedScript, evalScript.getValue());

        final List<NamedParam> bindings = evalBindings.getValue();
        Assert.assertEquals(5, bindings.size());

        Assert.assertEquals("policies", bindings.get(0).name());
        Assert.assertEquals("Array[com.thinkbiganalytics.policy.rest.model.FieldPolicy]", bindings.get(0).tpe());
        Assert.assertNull(bindings.get(0).value());

        Assert.assertEquals("profiler", bindings.get(1).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.dataprofiler.Profiler", bindings.get(1).tpe());
        Assert.assertNull(bindings.get(1).value());

        Assert.assertEquals("sparkContextService", bindings.get(2).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.SparkContextService", bindings.get(2).tpe());
        Assert.assertEquals(sparkContextService, bindings.get(2).value());

        Assert.assertEquals("tableName", bindings.get(3).name());
        Assert.assertEquals("String", bindings.get(3).tpe());
        Assert.assertTrue(((String) bindings.get(3).value()).matches("^[0-9a-f]{32}$"));

        Assert.assertEquals("validator", bindings.get(4).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.datavalidator.DataValidator", bindings.get(4).tpe());
        Assert.assertNull(bindings.get(4).value());
    }

    /**
     * Verify executing a transformation request with a data source provider factory.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void executeWithDatasourceProviderFactory() throws Exception {
        // Mock Spark context service
        final SparkContextService sparkContextService = Mockito.mock(SparkContextService.class);

        // Mock Spark script engine
        final SparkScriptEngine engine = Mockito.mock(SparkScriptEngine.class);
        Mockito.when(engine.eval(Mockito.anyString(), Mockito.anyListOf(NamedParam.class))).thenReturn(new MockTransformResult());
        Mockito.when(engine.getSparkContext()).thenReturn(Mockito.mock(SparkContext.class));

        // Mock data source provider factory
        final DatasourceProvider datasourceProvider = Mockito.mock(DatasourceProvider.class);
        final DatasourceProviderFactory datasourceProviderFactory = Mockito.mock(DatasourceProviderFactory.class);
        Mockito.when(datasourceProviderFactory.getDatasourceProvider(Mockito.anyCollectionOf(Datasource.class))).thenReturn(datasourceProvider);

        // Mock profiler
        final Profiler profiler = Mockito.mock(Profiler.class);

        // Test executing a request
        final TransformRequest request = new TransformRequest();
        request.setDatasources(Collections.singletonList(Mockito.mock(Datasource.class)));
        request.setScript("sqlContext.range(1,10)");

        final TransformService service = new TransformService(TransformScript.class, engine, sparkContextService, new MockTransformJobTracker());
        service.setDatasourceProviderFactory(datasourceProviderFactory);
        service.setProfiler(profiler);

        final TransformResponse response = service.execute(request);
        Assert.assertEquals(TransformResponse.Status.SUCCESS, response.getStatus());

        // Test eval arguments
        final ArgumentCaptor<String> evalScript = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<List> evalBindings = ArgumentCaptor.forClass(List.class);
        Mockito.verify(engine).eval(evalScript.capture(), evalBindings.capture());

        final String expectedScript = IOUtils.toString(getClass().getResourceAsStream("transform-service-script1.scala"), "UTF-8");
        Assert.assertEquals(expectedScript, evalScript.getValue());

        final List<NamedParam> bindings = evalBindings.getValue();
        Assert.assertEquals(6, bindings.size());

        Assert.assertEquals("policies", bindings.get(0).name());
        Assert.assertEquals("Array[com.thinkbiganalytics.policy.rest.model.FieldPolicy]", bindings.get(0).tpe());
        Assert.assertNull(bindings.get(0).value());

        Assert.assertEquals("profiler", bindings.get(1).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.dataprofiler.Profiler", bindings.get(1).tpe());
        Assert.assertEquals(profiler, bindings.get(1).value());

        Assert.assertEquals("sparkContextService", bindings.get(2).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.SparkContextService", bindings.get(2).tpe());
        Assert.assertEquals(sparkContextService, bindings.get(2).value());

        Assert.assertEquals("tableName", bindings.get(3).name());
        Assert.assertEquals("String", bindings.get(3).tpe());
        Assert.assertTrue(((String) bindings.get(3).value()).matches("^[0-9a-f]{32}$"));

        Assert.assertEquals("validator", bindings.get(4).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.datavalidator.DataValidator", bindings.get(4).tpe());
        Assert.assertNull(bindings.get(4).value());

        Assert.assertEquals("datasourceProvider", bindings.get(5).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.shell.DatasourceProvider[org.apache.spark.sql.DataFrame]", bindings.get(5).tpe());
        Assert.assertEquals(datasourceProvider, bindings.get(5).value());
    }

    /**
     * Verify executing a transformation request with data validation.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void executeWithValidator() throws Exception {
        // Mock Spark context service
        final SparkContextService sparkContextService = Mockito.mock(SparkContextService.class);

        // Mock Spark script engine
        final SparkScriptEngine engine = Mockito.mock(SparkScriptEngine.class);
        Mockito.when(engine.eval(Mockito.anyString(), Mockito.anyListOf(NamedParam.class))).thenReturn(new MockTransformResult());
        Mockito.when(engine.getSparkContext()).thenReturn(Mockito.mock(SparkContext.class));

        // Mock validator
        final List<FieldPolicy> policies = new ArrayList<>();
        policies.add(new FieldPolicy());
        policies.add(new FieldPolicy());

        final DataValidator validator = Mockito.mock(DataValidator.class);

        // Test executing a request
        final TransformRequest request = new TransformRequest();
        request.setPolicies(policies);
        request.setScript("sqlContext.range(1,10)");

        final TransformService service = new TransformService(TransformScript.class, engine, sparkContextService, new MockTransformJobTracker());
        service.setValidator(validator);

        final TransformResponse response = service.execute(request);
        Assert.assertEquals(TransformResponse.Status.SUCCESS, response.getStatus());

        // Test eval arguments
        final ArgumentCaptor<String> evalScript = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<List> evalBindings = ArgumentCaptor.forClass(List.class);
        Mockito.verify(engine).eval(evalScript.capture(), evalBindings.capture());

        final String expectedScript = IOUtils.toString(getClass().getResourceAsStream("transform-service-script1.scala"), "UTF-8");
        Assert.assertEquals(expectedScript, evalScript.getValue());

        final List<NamedParam> bindings = evalBindings.getValue();
        Assert.assertEquals(5, bindings.size());

        Assert.assertEquals("policies", bindings.get(0).name());
        Assert.assertEquals("Array[com.thinkbiganalytics.policy.rest.model.FieldPolicy]", bindings.get(0).tpe());
        Assert.assertArrayEquals((Object[]) bindings.get(0).value(), policies.toArray());

        Assert.assertEquals("profiler", bindings.get(1).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.dataprofiler.Profiler", bindings.get(1).tpe());
        Assert.assertNull(bindings.get(1).value());

        Assert.assertEquals("sparkContextService", bindings.get(2).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.SparkContextService", bindings.get(2).tpe());
        Assert.assertEquals(sparkContextService, bindings.get(2).value());

        Assert.assertEquals("tableName", bindings.get(3).name());
        Assert.assertEquals("String", bindings.get(3).tpe());
        Assert.assertTrue(((String) bindings.get(3).value()).matches("^[0-9a-f]{32}$"));

        Assert.assertEquals("validator", bindings.get(4).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.datavalidator.DataValidator", bindings.get(4).tpe());
        Assert.assertEquals(validator, bindings.get(4).value());
    }

    /**
     * Verify converting a transformation request to a Scala script.
     */
    @Test
    public void toScript() throws Exception {
        // Build the request
        final TransformRequest request = new TransformRequest();
        request.setScript("sqlContext.range(1,10)");

        // Test converting request to script
        final TransformService service = new TransformService(TransformScript.class, Mockito.mock(SparkScriptEngine.class), Mockito.mock(SparkContextService.class),
                                                              Mockito.mock(TransformJobTracker.class));

        final String expected = IOUtils.toString(getClass().getResourceAsStream("transform-service-script1.scala"), "UTF-8");
        Assert.assertEquals(expected, service.toScript(request));
    }

    /**
     * Verify converting a transformation request with a parent to a Scala script.
     */
    @Test
    public void toScriptWithParent() throws Exception {
        // Build the request
        final TransformRequest.Parent parent = new TransformRequest.Parent();
        parent.setScript("sqlContext.range(1,10)");
        parent.setTable("parent_table");

        final TransformRequest request = new TransformRequest();
        request.setParent(parent);
        request.setScript("parent.withColumn(functions.expr(\"id+1\")");

        // Test converting request to script
        final TransformService service = new TransformService(TransformScript.class, Mockito.mock(SparkScriptEngine.class), Mockito.mock(SparkContextService.class),
                                                              Mockito.mock(TransformJobTracker.class));

        final String expected = IOUtils.toString(getClass().getResourceAsStream("transform-service-script2.scala"), "UTF-8");
        Assert.assertEquals(expected, service.toScript(request));
    }

    /**
     * A mock implementation of {@link TransformJobTracker} for testing.
     */
    private static class MockTransformJobTracker extends TransformJobTracker {

        /**
         * Constructs a {@code MockTransformJobTracker}.
         */
        MockTransformJobTracker() {
            super(Thread.currentThread().getContextClassLoader());
        }

        @Override
        public void addSparkListener(@Nonnull SparkScriptEngine engine) {
            // ignored
        }
    }

    /**
     * A mock result from a {@link TransformScript} for testing.
     */
    private static class MockTransformResult implements Callable<TransformResponse> {

        @Override
        public TransformResponse call() throws Exception {
            final TransformResponse response = new TransformResponse();
            response.setStatus(TransformResponse.Status.SUCCESS);
            return response;
        }
    }
}
