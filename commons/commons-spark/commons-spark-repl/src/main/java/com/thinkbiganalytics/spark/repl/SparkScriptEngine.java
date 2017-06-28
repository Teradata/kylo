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
import com.google.common.base.Throwables;
import com.thinkbiganalytics.spark.SparkInterpreterBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptException;

import scala.collection.JavaConversions;
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
     * Matches a multi-line comment in Scala
     */
    private static final Pattern COMMENT = Pattern.compile("/\\*.*\\*/");

    /**
     * Matches continuation lines in Scala
     */
    private static final Pattern LINE_CONTINUATION = Pattern.compile("^\\s*\\.");

    /**
     * Spark configuration
     */
    @Autowired
    private SparkConf conf;

    /**
     * List of patterns to deny in scripts
     */
    @Nullable
    private List<Pattern> denyPatterns;

    /**
     * Spark REPL interface
     */
    @Nullable
    private IMain interpreter;

    @Autowired
    private SparkInterpreterBuilder builder;

    @Nonnull
    @Override
    public ClassLoader getClassLoader() {
        // Get current context class loader
        final Thread currentThread = Thread.currentThread();
        final ClassLoader contextClassLoader = currentThread.getContextClassLoader();

        // Get interpreter class loader from context
        getInterpreter().setContextClassLoader();
        final ClassLoader interpreterClassLoader = currentThread.getContextClassLoader();

        // Reset context
        currentThread.setContextClassLoader(contextClassLoader);
        return interpreterClassLoader;
    }

    @Nonnull
    @Override
    protected SparkContext createSparkContext() {
        // Allow interpreter to modify Thread context for Spark
        getInterpreter().setContextClassLoader();

        // The SparkContext ClassLoader is needed during initialization (only for YARN master)
        return executeWithSparkClassLoader(new Callable<SparkContext>() {
            @Override
            public SparkContext call() throws Exception {
                log.info("Creating spark context with spark conf {}", conf);
                return new SparkContext(conf);
            }
        });
    }

    @Override
    protected void execute(@Nonnull final String script) throws ScriptException {
        log.debug("Executing script:\n{}", script);

        // Convert script to single line (for checking security violations)
        final StringBuilder safeScriptBuilder = new StringBuilder(script.length());

        for (final String line : script.split("\n")) {
            if (!LINE_CONTINUATION.matcher(line).find()) {
                safeScriptBuilder.append(';');
            }
            safeScriptBuilder.append(line);
        }

        final String safeScript = COMMENT.matcher(safeScriptBuilder.toString()).replaceAll("");

        // Check for security violations
        for (final Pattern pattern : getDenyPatterns()) {
            if (pattern.matcher(safeScript).find()) {
                log.error("Not executing script that matches deny pattern: {}", pattern);
                throw new ScriptException("Script not executed due to security policy.");
            }
        }

        // Execute script
        try {
            getInterpreter().interpret(safeScript);
        } catch (final AssertionError e) {
            log.warn("Caught assertion error when executing script. Retrying...", e);
            reset();
            getInterpreter().interpret(safeScript);
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
     * Executes the specified callable after replacing the current context class loader.
     *
     * <p>This is a work-around to avoid {@link ClassCastException} issues caused by conflicts between Hadoop and Kylo Spark Shell. Spark uses the context class loader when loading Hadoop components
     * for running Spark on YARN. When both Hadoop and Kylo Spark Shell provide the same class then both classes are loaded when creating a {@link SparkContext}. The fix is to set the context class
     * loader to the same class loader that was used to load the {@link SparkContext} class.</p>
     *
     * @param callable the function to be executed
     * @param <T>      the return type
     * @return the return value
     */
    private <T> T executeWithSparkClassLoader(@Nonnull final Callable<T> callable) {
        // Set context class loader
        final Thread currentThread = Thread.currentThread();
        final ClassLoader contextClassLoader = currentThread.getContextClassLoader();

        final ClassLoader sparkClassLoader = new ForwardingClassLoader(SparkContext.class.getClassLoader(), contextClassLoader);
        currentThread.setContextClassLoader(sparkClassLoader);

        // Execute callable
        try {
            return callable.call();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        } finally {
            // Reset context class loader
            currentThread.setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * Gets the list of patterns that should prevent a script from executing.
     *
     * @return the deny patterns list
     * @throws IllegalStateException if the spark-deny-patterns.conf file cannot be found
     */
    @Nonnull
    private List<Pattern> getDenyPatterns() {
        if (denyPatterns == null) {
            // Load custom or default deny patterns
            String resourceName = "spark-deny-patterns.conf";
            InputStream resourceStream = getClass().getResourceAsStream("/" + resourceName);
            if (resourceStream == null) {
                resourceName = "spark-deny-patterns.default.conf";
                resourceStream = getClass().getResourceAsStream(resourceName);
            }

            // Parse lines
            final List<String> denyPatternLines;
            if (resourceStream != null) {
                try {
                    denyPatternLines = IOUtils.readLines(resourceStream, "UTF-8");
                    log.info("Loaded Spark deny patterns from {}.", resourceName);
                } catch (final IOException e) {
                    throw new IllegalStateException("Unable to load " + resourceName, e);
                }
            } else {
                log.info("Missing default Spark deny patterns.");
                denyPatternLines = Collections.emptyList();
            }

            // Compile patterns
            denyPatterns = new ArrayList<>();
            for (final String line : denyPatternLines) {
                final String trimLine = line.trim();
                if (!line.startsWith("#") && !trimLine.isEmpty()) {
                    denyPatterns.add(Pattern.compile(line));
                }
            }
        }
        return denyPatterns;
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
            final Settings settings = getSettings();

            // Initialize engine
            final ClassLoader parentClassLoader = getClass().getClassLoader();
            final SparkInterpreterBuilder b = this.builder.withSettings(settings)
                .withPrintWriter(getPrintWriter())
                .withClassLoader(parentClassLoader);
            final IMain interpreter = b.newInstance();

            interpreter.setContextClassLoader();
            interpreter.initializeSynchronous();

            // Setup environment
            final scala.collection.immutable.List<String> empty = JavaConversions.asScalaBuffer(new ArrayList<String>()).toList();
            final Results.Result result = interpreter.bind("engine", SparkScriptEngine.class.getName(), this, empty);
            if (result instanceof Results.Error$) {
                throw new IllegalStateException("Failed to initialize interpreter");
            }

            this.interpreter = interpreter;
        }
        return this.interpreter;
    }

    /**
     * Gets the settings for the interpreter.
     *
     * @return the interpreter settings
     */
    @Nonnull
    private Settings getSettings() {
        final Settings settings = new Settings();

        if (settings.classpath().isDefault()) {
            final String classPath = Joiner.on(':').join(((URLClassLoader) getClass().getClassLoader()).getURLs()) + ":" + System.getProperty("java.class.path");
            settings.classpath().value_$eq(classPath);
        }
        return settings;
    }
}
