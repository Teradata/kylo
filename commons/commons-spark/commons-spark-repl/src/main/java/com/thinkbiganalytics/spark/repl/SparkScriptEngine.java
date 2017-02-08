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

import com.google.common.base.Joiner;
import com.thinkbiganalytics.spark.SparkInterpreterBuilder;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;
import scala.tools.nsc.interpreter.Results;

/**
 * Evaluates Scala scripts using the Spark REPL interface.
 */
@Component
@ComponentScan("com.thinkbiganalytics.spark")
public class SparkScriptEngine extends ScriptEngine {

    private static final Logger log = LoggerFactory.getLogger(SparkScriptEngine.class);

    /**
     * Spark configuration
     */
    @Autowired
    private SparkConf conf;

    /**
     * Spark REPL interface
     */
    @Nullable
    private IMain interpreter;

    @Autowired
    private SparkInterpreterBuilder builder;

    @Nonnull
    @Override
    protected SparkContext createSparkContext() {
        // Let Spark know where to find class files
        getInterpreter();

        // The SparkContext ClassLoader is needed during initialization (only for YARN master)
        Thread currentThread = Thread.currentThread();
        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(SparkContext.class.getClassLoader());

        log.info("Creating spark context with spark conf {}", conf);

        SparkContext sparkContext = new SparkContext(this.conf);
        currentThread.setContextClassLoader(contextClassLoader);
        return sparkContext;
    }

    @Override
    protected void execute(@Nonnull final String script) {
        log.debug("Executing script:\n{}", script);

        try {
            getInterpreter().interpret(script);
        } catch (final AssertionError e) {
            log.warn("Caught assertion error when executing script. Retrying...", e);
            reset();
            getInterpreter().interpret(script);
        }
    }

    @Override
    protected void reset() {
        super.reset();

        // Clear the interpreter
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    /**
     * Gets the Spark REPL interface to be used.
     *
     * @return the interpreter
     */
    @Nonnull
    private IMain getInterpreter() {
        if (this.interpreter == null) {
            // Determine engine settings
            Settings settings = getSettings();

            // Initialize engine
            final ClassLoader parentClassLoader = getClass().getClassLoader();
            SparkInterpreterBuilder b = this.builder.withSettings(settings);
            b = b.withPrintWriter(getPrintWriter());
            b = b.withClassLoader(parentClassLoader);
            IMain interpreter = b.newInstance();

            interpreter.setContextClassLoader();
            interpreter.initializeSynchronous();

            // Setup environment
            List<String> empty = JavaConversions.asScalaBuffer(new ArrayList<String>()).toList();
            Results.Result result = interpreter.bind("engine", SparkScriptEngine.class.getName(), this, empty);
            if (result instanceof Results.Error$) {
                throw new IllegalStateException("Failed to initialize interpreter");
            }

            this.interpreter = interpreter;
        }
        return this.interpreter;
    }

    private Settings getSettings() {
        Settings settings = new Settings();

        if (settings.classpath().isDefault()) {
            String classPath = Joiner.on(':').join(((URLClassLoader) getClass().getClassLoader()).getURLs()) + ":" + System
                .getProperty("java.class.path");
            settings.classpath().value_$eq(classPath);
        }
        return settings;
    }
}
