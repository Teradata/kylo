package com.thinkbiganalytics.spark.repl;

import com.thinkbiganalytics.spark.SparkInterpreterBuilder;
import org.apache.spark.SparkConf;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

import javax.script.ScriptException;
import java.io.PrintWriter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparkScriptEngine.class, SparkScriptEngineTest.class})
public class SparkScriptEngineTest {

    @Bean
    public SparkConf sparkConf() {
        SparkConf sparkConf = new SparkConf();
        sparkConf.set("spark.driver.allowMultipleContexts", "true");
        return sparkConf;
    }

    @Bean
    public SparkInterpreterBuilder sparkInterpreterBuilder() {
        return new SparkInterpreterBuilder() {
            private Settings settings;
            private PrintWriter printWriter;
            private ClassLoader classLoader;

            @Override
            public SparkInterpreterBuilder withSettings(Settings param) {
                this.settings = param;
                return this;
            }

            @Override
            public SparkInterpreterBuilder withPrintWriter(PrintWriter param) {
                this.printWriter = param;
                return this;
            }

            @Override
            public SparkInterpreterBuilder withClassLoader(ClassLoader param) {
                this.classLoader = param;
                return this;
            }

            @Override
            public IMain newInstance() {
                return new IMain(settings, printWriter) {
                    @Override
                    public ClassLoader parentClassLoader() {
                        return classLoader;
                    }

                    @Override
                    public PrintWriter out() {
                        return printWriter;
                    }
                };
            }
        };
    }

    @Autowired
    private SparkScriptEngine engine;

    /** Expected thrown exception */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /** Verify evaluating a script and returning a value. */
    @Test
    public void test() throws Exception {
        Assert.assertEquals(3, engine.eval("new Integer(1 + 2)"));
    }

    /** Verify evaluating a script with a compile error. */
    @Test
    public void testWithCompileError() throws Exception {
        // Configure expected exception
        thrown.expect(ScriptException.class);
        thrown.expectMessage(CoreMatchers.startsWith("error: not found: value Method in <console> at line number 1"));

        // Test evaluating
        engine.eval("Method invalid");
    }

    /** Verify evaluating a script with an exception. */
    @Test(expected = UnsupportedOperationException.class)
    public void testWithException() throws Exception {
        engine.eval("throw new UnsupportedOperationException()");
    }
}
