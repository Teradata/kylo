package com.thinkbiganalytics.spark.repl;

/*-
 * #%L
 * thinkbig-commons-spark-repl
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

import com.thinkbiganalytics.spark.SparkInterpreterBuilder;

import org.apache.spark.SparkConf;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.PrintWriter;

import javax.script.ScriptException;

import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparkScriptEngine.class, SparkScriptEngineTest.class})
public class SparkScriptEngineTest {

    /**
     * Expected thrown exception
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Autowired
    private SparkScriptEngine engine;

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

    /**
     * Verify evaluating a script and returning a value.
     */
    @Test
    public void test() throws Exception {
        Assert.assertEquals(3, engine.eval("new Integer(1 + 2)"));
    }

    /**
     * Verify security exception for rule violation.
     */
    @Test(expected = ScriptException.class)
    public void testSecurity() throws Exception {
        engine.eval("System.eval(\"true\")");
    }

    /**
     * Verify security exception if comment in script.
     */
    @Test(expected = ScriptException.class)
    public void testSecurityWithComment() throws Exception {
        engine.eval("System/*PAD*/.eval(\"true\")");
    }

    /**
     * Verify security exception for multi-line scripts.
     */
    @Test(expected = ScriptException.class)
    public void testSecurityWithMultiline() throws Exception {
        engine.eval("System\n.eval(\"true\")");
    }

    /**
     * Verify evaluating a script with a compile error.
     */
    @Test
    public void testWithCompileError() throws Exception {
        // Configure expected exception
        thrown.expect(ScriptException.class);
        thrown.expectMessage(CoreMatchers.startsWith("error: not found: value Method in <console> at line number 8"));

        // Test evaluating
        engine.eval("Method invalid");
    }

    /**
     * Verify evaluating a script with an exception.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testWithException() throws Exception {
        engine.eval("throw new UnsupportedOperationException()");
    }
}
