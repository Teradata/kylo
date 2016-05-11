package com.thinkbiganalytics.spark.interpreter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.thinkbiganalytics.spark.repl.ScriptEngine;
import com.thinkbiganalytics.spark.repl.ScriptEngineFactory;

import org.apache.spark.SparkConf;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Reads in a Scala file with Spark code and executes it.
 */
public class App {

    /**
     * Evaluates a Scala file.
     *
     * @param args the command-line arguments
     * @throws Exception if an error occurs
     */
    public static void main (@Nonnull String[] args) throws Exception {
        // Verify arguments
        if (args.length != 1) {
            System.err.println("error: usage: SparkShellApp file");
            System.exit(1);
        }

        // Read script from file
        String script = Files.toString(new File(args[0]), Charsets.UTF_8);

        // Execute script
        ScriptEngine engine = ScriptEngineFactory.getScriptEngine(new SparkConf());
        engine.eval(script);
    }
}
