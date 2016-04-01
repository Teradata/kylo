package com.thinkbiganalytics.spark.repl;

import javax.script.ScriptException;

import org.apache.spark.SparkConf;
import org.junit.Assert;
import org.junit.Test;

public class SparkScriptEngineTest
{
    /** Spark configuration for engine */
    private static final SparkConf CONF = new SparkConf();
    static {
        CONF.set("spark.driver.allowMultipleContexts", "true");
    }

    /** Verify evaluating a script and returning a value. */
    @Test
    public void test () throws Exception
    {
        SparkScriptEngine engine = new SparkScriptEngine(CONF);
        Assert.assertEquals(3, engine.eval("new Integer(1 + 2)"));
    }

    /** Verify evaluating a script with a compile error. */
    @Test(expected=ScriptException.class)
    public void testWithCompileError () throws Exception
    {
        SparkScriptEngine engine = new SparkScriptEngine(CONF);
        engine.eval("+1");
    }

    /** Verify evaluating a script with an exception. */
    @Test(expected=UnsupportedOperationException.class)
    public void testWithException () throws Exception
    {
        SparkScriptEngine engine = new SparkScriptEngine(CONF);
        engine.eval("throw new UnsupportedOperationException()");
    }
}
