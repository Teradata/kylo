package com.thinkbiganalytics.spark;

import org.springframework.stereotype.Component;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

import java.io.PrintWriter;

/**
 * Builds a Spark interpreter.
 */
@Component
public interface SparkInterpreterBuilder {

    /**
     * Sets the settings for the Spark interpreter.
     * @param param the settings
     * @return this builder
     */
    SparkInterpreterBuilder withSettings(Settings param);

    /**
     * Sets the print writer for the Spark interpreter.
     * @param param the print writer
     * @return this builder
     */
    SparkInterpreterBuilder withPrintWriter(PrintWriter param);

    /**
     * Sets the class loader for the Spark interpreter.
     * @param param the class loader
     * @return this builder
     */
    SparkInterpreterBuilder withClassLoader(ClassLoader param);

    /**
     * Builds a new Spark interpreter.
     * @return a Spark interpreter
     */
    IMain newInstance();
}
