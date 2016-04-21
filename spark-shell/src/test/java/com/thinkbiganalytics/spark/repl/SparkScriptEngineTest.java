package com.thinkbiganalytics.spark.repl;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import javax.script.ScriptException;

public class SparkScriptEngineTest {

    /** Spark configuration for engine */
    private static final SparkConf CONF = new SparkConf();

    static {
        CONF.set("spark.driver.allowMultipleContexts", "true");
    }

    /** Expected thrown exception */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /** Verify evaluating a script and returning a value. */
    @Test
    public void test() throws Exception {
        SparkScriptEngine engine = new SparkScriptEngine(CONF);
        Assert.assertEquals(3, engine.eval("new Integer(1 + 2)"));
    }

    /** Verify evaluating a script with a compile error. */
    @Test
    public void testWithCompileError() throws Exception {
        // Configure expected exception
        thrown.expect(ScriptException.class);
        thrown.expectMessage(CoreMatchers.equalTo("error: not found: value Method in <console> at line number 11"));

        // Test evaluating
        SparkScriptEngine engine = new SparkScriptEngine(CONF);
        engine.eval("Method invalid");
    }

    /** Verify evaluating a script with an exception. */
    @Test(expected = UnsupportedOperationException.class)
    public void testWithException() throws Exception {
        SparkScriptEngine engine = new SparkScriptEngine(CONF);
        engine.eval("throw new UnsupportedOperationException()");
    }
}
