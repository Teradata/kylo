package com.thinkbiganalytics.spark.interpreter;

/*-
 * #%L
 * thinkbig-spark-interpreter-app
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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;

import org.apache.spark.SparkConf;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;

import javax.annotation.Nonnull;

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
