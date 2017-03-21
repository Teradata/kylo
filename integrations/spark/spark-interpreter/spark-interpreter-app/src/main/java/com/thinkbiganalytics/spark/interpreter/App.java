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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import com.thinkbiganalytics.spark.rest.model.Datasource;
import com.thinkbiganalytics.spark.shell.DatasourceProvider;

import org.apache.spark.SparkConf;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import scala.tools.nsc.interpreter.NamedParam;
import scala.tools.nsc.interpreter.NamedParamClass;

/**
 * Reads in a Scala file with Spark code and executes it.
 */
@Component
public class App {

    /**
     * Evaluates a Scala file.
     *
     * @param args the command-line arguments
     * @throws Exception if an error occurs
     */
    public static void main(@Nonnull String[] args) throws Exception {
        // Verify arguments
        if (args.length != 1) {
            System.err.println("error: usage: SparkShellApp file");
            System.exit(1);
        }

        // Load environment
        final ApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark");
        final String script = Files.toString(new File(args[0]), Charsets.UTF_8);

        // Prepare bindings
        final List<NamedParam> bindings = new ArrayList<>();
        bindings.add(new NamedParamClass("datasourceProvider", DatasourceProvider.class.getName(), ctx.getBean(DatasourceProvider.class)));

        // Execute script
        final SparkScriptEngine engine = ctx.getBean(SparkScriptEngine.class);
        engine.eval(script, bindings);
    }

    /**
     * Creates the {@link Datasource} provider.
     *
     * @return the data source provider
     */
    @Bean
    public DatasourceProvider datasourceProvider() throws IOException {
        final List<Datasource> datasources;
        final String env = System.getenv("DATASOURCES");

        if (env != null) {
            datasources = new ObjectMapper().readValue(env, TypeFactory.defaultInstance().constructCollectionType(List.class, Datasource.class));
        } else {
            datasources = Collections.emptyList();
        }

        return new DatasourceProvider(datasources);
    }

    /**
     * Creates the Spark configuration.
     *
     * @return the Spark configuration
     */
    @Bean
    public SparkConf sparkConf() {
        return new SparkConf();
    }
}
