package com.thinkbiganalytics.spark.service;

import com.google.common.collect.ImmutableList;
import com.thinkbiganalytics.spark.metadata.TransformRequest;
import com.thinkbiganalytics.spark.metadata.TransformResponse;
import com.thinkbiganalytics.spark.repl.ScriptEngine;

import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import scala.tools.nsc.interpreter.NamedParam;

public class TransformServiceTest {

    /** Verify executing a transformation request. */
    @Test
    @SuppressWarnings("unchecked")
    public void execute() throws Exception {
        // Mock SQL context and script engine
        DataFrame dataFrame = Mockito.mock(DataFrame.class);
        Mockito.when(dataFrame.collectAsList()).thenReturn(new ArrayList<Row>());

        SQLContext context = Mockito.mock(SQLContext.class);
        Mockito.when(context.sql(Mockito.anyString())).thenReturn(dataFrame);

        ScriptEngine engine = Mockito.mock(ScriptEngine.class);
        Mockito.when(engine.eval(Mockito.anyString(), Mockito.any(List.class))).thenReturn(new Callable<TransformResponse>() {
            @Override
            public TransformResponse call() throws Exception {
                TransformResponse response = new TransformResponse();
                response.setStatus(TransformResponse.Status.SUCCESS);
                return response;
            }
        });
        Mockito.when(engine.getSparkContext()).thenReturn(Mockito.mock(SparkContext.class));
        Mockito.when(engine.getSQLContext()).thenReturn(context);

        // Test executing a request
        TransformRequest request = new TransformRequest();
        request.setScript("sqlContext.range(1,10)");

        TransformService service = new TransformService(engine);
        service.startAsync();
        service.awaitRunning();

        TransformResponse response = null;
        try {
            response = service.execute(request);
        } finally {
            service.stopAsync();
        }

        Assert.assertEquals(TransformResponse.Status.SUCCESS, response.getStatus());

        // Test eval arguments
        ArgumentCaptor<String> evalScript = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List> evalBindings = ArgumentCaptor.forClass(List.class);
        Mockito.verify(engine).eval(evalScript.capture(), evalBindings.capture());

        String expectedScript = IOUtils.toString(getClass().getResourceAsStream("transform-service-script1.scala"), "UTF-8");
        Assert.assertEquals(expectedScript, evalScript.getValue());

        List<NamedParam> bindings = evalBindings.getValue();
        Assert.assertEquals(2, bindings.size());
        Assert.assertEquals("database", bindings.get(0).name());
        Assert.assertEquals("String", bindings.get(0).tpe());
        Assert.assertEquals("spark_shell_temp", bindings.get(0).value());
        Assert.assertEquals("tableName", bindings.get(1).name());
        Assert.assertEquals("String", bindings.get(1).tpe());
        Assert.assertTrue(((String)bindings.get(1).value()).matches("^[0-9a-f]{32}$"));
    }

    /** Verify setting up database during start-up. */
    @Test
    public void startUp() {
        // Mock SQL context and script engine
        DataFrame dataFrame = Mockito.mock(DataFrame.class);
        ImmutableList<Row> tables = ImmutableList.of(RowFactory.create("table1", false), RowFactory.create("table2", false));
        Mockito.when(dataFrame.collectAsList()).thenReturn(tables);

        SQLContext context = Mockito.mock(SQLContext.class);
        Mockito.when(context.sql(Mockito.anyString())).thenReturn(dataFrame);

        ScriptEngine engine = Mockito.mock(ScriptEngine.class);
        Mockito.when(engine.getSparkContext()).thenReturn(Mockito.mock(SparkContext.class));
        Mockito.when(engine.getSQLContext()).thenReturn(context);

        // Verify start-up
        TransformService service = new TransformService(engine);
        service.startUp();

        Mockito.verify(context).sql("CREATE DATABASE IF NOT EXISTS `spark_shell_temp`");
        Mockito.verify(context).sql("DROP TABLE IF EXISTS `spark_shell_temp`.`table1`");
        Mockito.verify(context).sql("DROP TABLE IF EXISTS `spark_shell_temp`.`table2`");
    }

    /**
     * Verify converting a transformation request to a Scala script.
     */
    @Test
    public void toScript() throws Exception {
        // Mock the script engine
        ScriptEngine engine = Mockito.mock(ScriptEngine.class);
        Mockito.when(engine.getSparkContext()).thenReturn(Mockito.mock(SparkContext.class));

        // Build the request
        TransformRequest request = new TransformRequest();
        request.setScript("sqlContext.range(1,10)");

        // Test converting request to script
        String expected = IOUtils.toString(getClass().getResourceAsStream("transform-service-script1.scala"), "UTF-8");

        TransformService service = new TransformService(engine);
        Assert.assertEquals(expected, service.toScript(request));
    }

    /** Verify converting a transformation request with a parent to a Scala script. */
    @Test
    public void toScriptWithParent() throws Exception {
        // Build the request
        TransformRequest.Parent parent = new TransformRequest.Parent();
        parent.setScript("sqlContext.range(1,10)");
        parent.setTable("parent_table");

        TransformRequest request = new TransformRequest();
        request.setParent(parent);
        request.setScript("parent.withColumn(functions.expr(\"id+1\")");

        // Test converting request to script
        String expected = IOUtils.toString(getClass().getResourceAsStream("transform-service-script2.scala"), "UTF-8");

        TransformService service = new TransformService(Mockito.mock(ScriptEngine.class));
        Assert.assertEquals(expected, service.toScript(request));
    }
}
