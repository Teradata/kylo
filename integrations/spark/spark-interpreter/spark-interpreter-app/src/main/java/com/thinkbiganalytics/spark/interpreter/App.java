package com.thinkbiganalytics.spark.interpreter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import org.apache.spark.SparkConf;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Reads in a Scala file with Spark code and executes it.
 */
@Component
public class App {

    @Bean
    public SparkConf sparkConf() {
        return new SparkConf();
    }

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

        ApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark");
        SparkScriptEngine engine = ctx.getBean(SparkScriptEngine.class);
        engine.eval(script);
    }
}
