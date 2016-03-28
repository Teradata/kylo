package com.thinkbiganalytics.spark.repl;

import java.net.URLClassLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.repl.SparkILoop;
import org.apache.spark.repl.SparkIMain;

import com.google.common.base.Joiner;

import scala.collection.immutable.List;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.Results;

public class SparkScriptEngine extends ScriptEngine
{
    @Nonnull
    private final SparkConf conf;

    @Nullable
    private SparkIMain interpreter;

    public SparkScriptEngine (@Nonnull final SparkConf conf)
    {
        this.conf = conf;
    }

    public void init ()
    {
        getInterpreter();
    }

    @Nonnull
    @Override
    protected SparkContext createSparkContext ()
    {
        this.conf.set("spark.repl.class.uri", getInterpreter().classServerUri());
        return new SparkContext(this.conf);
    }

    @Override
    protected void execute (@Nonnull final String script)
    {
        getInterpreter().interpret(script);
    }

    @Nonnull
    private SparkIMain getInterpreter ()
    {
        if (this.interpreter == null) {
            // Determine engine settings
            Settings settings = new Settings();

            if (settings.classpath().isDefault()) {
                String classPath = Joiner.on(':').join(((URLClassLoader)getClass().getClassLoader()).getURLs()) + ":" + System.getProperty("java.class.path");
                settings.classpath().value_$eq(classPath);
            }

            // Initialize engine
//            SparkILoop loop = new SparkILoop(null, getPrintWriter());
//            loop.settings_$eq(settings);
//            loop.createInterpreter();
            final ClassLoader parentClassLoader = getClass().getClassLoader();
            SparkIMain interpreter = new SparkIMain(settings, getPrintWriter(), false) {
                @Override
                public ClassLoader parentClassLoader ()  {
                    return parentClassLoader;
                }
            };

//            this.interpreter = loop.intp();
            interpreter.setContextClassLoader();
            interpreter.initializeSynchronous();

            // Setup environment
            Results.Result result = interpreter.bind("engine", SparkScriptEngine.class
                    .getName(), this, List.<String>empty());
            if (result instanceof Results.Error$) {
                throw new RuntimeException("TODO");
            }

            this.interpreter = interpreter;
        }
        return this.interpreter;
    }
}
