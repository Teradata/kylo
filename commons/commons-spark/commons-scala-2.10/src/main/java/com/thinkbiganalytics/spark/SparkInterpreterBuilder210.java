package com.thinkbiganalytics.spark;

import org.springframework.stereotype.Component;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

import java.io.PrintWriter;

/**
 * Created by ru186002 on 17/10/2016.
 */
@Component
public class SparkInterpreterBuilder210 implements SparkInterpreterBuilder {

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
}
