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

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.Profiler;
import com.thinkbiganalytics.spark.metadata.Job;
import com.thinkbiganalytics.spark.metadata.TransformScript;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import com.thinkbiganalytics.spark.rest.model.Datasource;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.DatasourceProvider;
import com.thinkbiganalytics.spark.shell.DatasourceProviderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import scala.tools.nsc.interpreter.NamedParam;

public class TransformServiceTest {

    /**
     * Verify executing a transformation request.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void execute() throws Exception {
        // Mock data set
        final DataSet dataSet = Mockito.mock(DataSet.class);
        Mockito.when(dataSet.persist(Mockito.any(StorageLevel.class))).thenReturn(dataSet);
        Mockito.when(dataSet.schema()).thenReturn(new StructType());

        // Mock Spark context service
        final SparkContextService sparkContextService = Mockito.mock(SparkContextService.class);

        // Mock Spark script engine
        final SparkScriptEngine engine = Mockito.mock(SparkScriptEngine.class);
        Mockito.when(engine.eval(Mockito.anyString(), Mockito.anyListOf(NamedParam.class))).thenReturn(dataSet);
        Mockito.when(engine.getSparkContext()).thenReturn(Mockito.mock(SparkContext.class));

        // Test executing a request
        final TransformRequest request = new TransformRequest();
        request.setScript("sqlContext.range(1,10)");

        final TransformService service = new TransformService(TransformScript.class, engine, sparkContextService, new MockJobTrackerService());
        final TransformResponse response = service.execute(request);
        Assert.assertEquals(TransformResponse.Status.PENDING, response.getStatus());

        // Test eval arguments
        final ArgumentCaptor<String> evalScript = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<List> evalBindings = ArgumentCaptor.forClass(List.class);
        Mockito.verify(engine).eval(evalScript.capture(), evalBindings.capture());

        String expectedScript = null;
        try (InputStream stream = getClass().getResourceAsStream("transform-service-script1.scala")) {
            expectedScript = IOUtils.toString(stream, "UTF-8");
        }

        if (expectedScript == null) {
            throw new Exception("transform-service-script1.scala failed to load");
        }

        Assert.assertEquals(expectedScript, evalScript.getValue());

        final List<NamedParam> bindings = evalBindings.getValue();
        Assert.assertEquals(1, bindings.size());

        Assert.assertEquals("sparkContextService", bindings.get(0).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.SparkContextService", bindings.get(0).tpe());
        Assert.assertEquals(sparkContextService, bindings.get(0).value());
    }

    /**
     * Verify executing a transformation request with a data source provider factory.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void executeWithDatasourceProviderFactory() throws Exception {
        // Mock data set
        final DataSet dataSet = Mockito.mock(DataSet.class);
        Mockito.when(dataSet.persist(Mockito.any(StorageLevel.class))).thenReturn(dataSet);
        Mockito.when(dataSet.schema()).thenReturn(new StructType());

        // Mock Spark context service
        final SparkContextService sparkContextService = Mockito.mock(SparkContextService.class);

        // Mock Spark script engine
        final SparkScriptEngine engine = Mockito.mock(SparkScriptEngine.class);
        Mockito.when(engine.eval(Mockito.anyString(), Mockito.anyListOf(NamedParam.class))).thenReturn(dataSet);
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

        final TransformService service = new TransformService(TransformScript.class, engine, sparkContextService, new MockJobTrackerService());
        service.setDatasourceProviderFactory(datasourceProviderFactory);
        service.setProfiler(profiler);

        final TransformResponse response = service.execute(request);
        Assert.assertEquals(TransformResponse.Status.PENDING, response.getStatus());

        // Test eval arguments
        final ArgumentCaptor<String> evalScript = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<List> evalBindings = ArgumentCaptor.forClass(List.class);
        Mockito.verify(engine).eval(evalScript.capture(), evalBindings.capture());

        final String expectedScript = IOUtils.toString(getClass().getResourceAsStream("transform-service-script1.scala"), "UTF-8");
        Assert.assertEquals(expectedScript, evalScript.getValue());

        final List<NamedParam> bindings = evalBindings.getValue();
        Assert.assertEquals(2, bindings.size());

        Assert.assertEquals("sparkContextService", bindings.get(0).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.SparkContextService", bindings.get(0).tpe());
        Assert.assertEquals(sparkContextService, bindings.get(0).value());

        Assert.assertEquals("datasourceProvider", bindings.get(1).name());
        Assert.assertEquals("com.thinkbiganalytics.spark.shell.DatasourceProvider[org.apache.spark.sql.DataFrame]", bindings.get(1).tpe());
        Assert.assertEquals(datasourceProvider, bindings.get(1).value());
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
                                                              Mockito.mock(JobTrackerService.class));

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
                                                              Mockito.mock(JobTrackerService.class));

        final String expected = IOUtils.toString(getClass().getResourceAsStream("transform-service-script2.scala"), "UTF-8");
        Assert.assertEquals(expected, service.toScript(request));
    }

    /**
     * A mock implementation of {@link JobTrackerService} for testing.
     */
    private static class MockJobTrackerService extends JobTrackerService {

        /**
         * Constructs a {@code MockJobTrackerService}.
         */
        MockJobTrackerService() {
            super(Thread.currentThread().getContextClassLoader());
        }

        @Override
        public <T> void submitJob(@Nonnull Job<T> job) {
            // ignored
        }
    }
}
