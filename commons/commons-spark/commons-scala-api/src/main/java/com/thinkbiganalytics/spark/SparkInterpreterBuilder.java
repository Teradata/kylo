package com.thinkbiganalytics.spark;

import org.springframework.stereotype.Component;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

import java.io.PrintWriter;

/**
 * Created by ru186002 on 17/10/2016.
 */
@Component
public interface SparkInterpreterBuilder {

    SparkInterpreterBuilder withSettings(Settings param);
    SparkInterpreterBuilder withPrintWriter(PrintWriter param);
    SparkInterpreterBuilder withClassLoader(ClassLoader param);
    IMain newInstance();

}
