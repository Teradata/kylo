package com.thinkbiganalytics.spark.service;

import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.spark.metadata.TransformRequest;
import com.thinkbiganalytics.spark.metadata.TransformResponse;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import scala.tools.nsc.interpreter.NamedParam;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Callable;

public class TransformServiceTest {

    private KerberosTicketConfiguration kerberosTicketConfiguration;

    @Before
    public void setup() {
        kerberosTicketConfiguration = new KerberosTicketConfiguration();
        kerberosTicketConfiguration.setKerberosEnabled(false);
    }

    /** Verify executing a transformation request. */
    @Test
    @SuppressWarnings("unchecked")
    public void execute() throws Exception {
        SQLContext context = Mockito.mock(SQLContext.class);
        SparkScriptEngine engine = Mockito.mock(SparkScriptEngine.class);
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

        TransformJobTracker tracker = new TransformJobTracker() {

            @Override
            public void addSparkListener(@Nonnull SparkScriptEngine engine) {

            }
        };
        TransformService service = new TransformService(engine, kerberosTicketConfiguration, tracker) {
            @Override
            void createDatabaseWithoutKerberos() {
                //do nothing such that we don't need to mock out context.sql(...) methods
                //which would require mocking either DataFrame or Dataset for different versions of Spark
            }
        };
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
//        Dataset dataset = Mockito.mock(Dataset.class);
//        ImmutableList<Row> tables = ImmutableList.of(RowFactory.create("table1", false), RowFactory.create("table2", false));
//        Mockito.when(dataset.collectAsList()).thenReturn(tables);
//
//        SQLContext context = Mockito.mock(SQLContext.class);
//        Mockito.when(context.sql(Mockito.anyString())).thenReturn(dataset);
//
//        SparkScriptEngine engine = Mockito.mock(SparkScriptEngine.class);
//        Mockito.when(engine.getSparkContext()).thenReturn(Mockito.mock(SparkContext.class));
//        Mockito.when(engine.getSQLContext()).thenReturn(context);
//
//        // Verify start-up
//        TransformService service = new TransformService(engine, kerberosTicketConfiguration);
//        service.startUp();
//
//        Mockito.verify(context).sql("CREATE DATABASE IF NOT EXISTS `spark_shell_temp`");
//        Mockito.verify(context).sql("DROP TABLE IF EXISTS `spark_shell_temp`.`table1`");
//        Mockito.verify(context).sql("DROP TABLE IF EXISTS `spark_shell_temp`.`table2`");
    }

    /**
     * Verify converting a transformation request to a Scala script.
     */
    @Test
    public void toScript() throws Exception {
        // Mock the script engine
        SparkScriptEngine engine = Mockito.mock(SparkScriptEngine.class);
        Mockito.when(engine.getSparkContext()).thenReturn(Mockito.mock(SparkContext.class));

        // Build the request
        TransformRequest request = new TransformRequest();
        request.setScript("sqlContext.range(1,10)");

        // Test converting request to script
        String expected = IOUtils.toString(getClass().getResourceAsStream("transform-service-script1.scala"), "UTF-8");

        TransformJobTracker tracker = Mockito.mock(TransformJobTracker.class);
        TransformService service = new TransformService(engine, kerberosTicketConfiguration, tracker);
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

        TransformJobTracker tracker = Mockito.mock(TransformJobTracker.class);
        TransformService service = new TransformService(Mockito.mock(SparkScriptEngine.class), kerberosTicketConfiguration, tracker);
        Assert.assertEquals(expected, service.toScript(request));
    }
}
