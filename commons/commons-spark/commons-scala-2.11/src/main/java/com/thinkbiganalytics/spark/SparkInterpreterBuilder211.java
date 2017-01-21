package com.thinkbiganalytics.spark;

import org.springframework.stereotype.Component;

import java.io.PrintWriter;

import javax.script.ScriptEngineFactory;

import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

/**
 * Builds a Spark interpreter using Scala 2.11.
 */
@Component
public class SparkInterpreterBuilder211 implements SparkInterpreterBuilder {

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
            public ScriptEngineFactory factory() {
                return new IMain.Factory();
            }

            @Override
            public ScriptEngineFactory getFactory() {
                return new IMain.Factory();
            }

            @Override
            public PrintWriter out() {
                return printWriter;
            }
        };
    }
}
