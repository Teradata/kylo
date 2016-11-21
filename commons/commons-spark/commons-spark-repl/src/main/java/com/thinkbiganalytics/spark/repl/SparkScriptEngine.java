package com.thinkbiganalytics.spark.repl;

import com.google.common.base.Joiner;
import com.thinkbiganalytics.spark.SparkInterpreterBuilder;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;
import scala.tools.nsc.interpreter.Results;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * Evaluates Scala scripts using the Spark REPL interface.
 */
@Component
@ComponentScan("com.thinkbiganalytics.spark")
public class SparkScriptEngine extends ScriptEngine {

    private static final Logger log = LoggerFactory.getLogger(SparkScriptEngine.class);

    /** Spark configuration */
    @Autowired
    private SparkConf conf;

    /** Spark REPL interface */
    @Nullable
    private IMain interpreter;

    @Autowired
    private SparkInterpreterBuilder builder;

    public SparkScriptEngine() {
        //required for Spring
    }
    /**
     * Constructs a {@code SparkScriptEngine} with the specified Spark configuration.
     *
     * @param sparkConf the Spark configuration
     */
    SparkScriptEngine(@Nonnull final SparkConf sparkConf) {
        this.conf = sparkConf;
    }

    public void setSparkInterpreterBuilder(SparkInterpreterBuilder builder) {
        this.builder = builder;
    }

    public void setSparkConf(SparkConf sparkConf) {
        this.conf = sparkConf;
    }

    @Override
    public void init() {
        getInterpreter();
    }

    @Nonnull
    @Override
    protected SparkContext createSparkContext() {
        // Let Spark know where to find class files
        IMain interpreter = getInterpreter();

        // The SparkContext ClassLoader is needed during initialization (only for YARN master)
        Thread currentThread = Thread.currentThread();
        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(SparkContext.class.getClassLoader());

        log.info("Creating spark context with spark conf " + conf);

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
