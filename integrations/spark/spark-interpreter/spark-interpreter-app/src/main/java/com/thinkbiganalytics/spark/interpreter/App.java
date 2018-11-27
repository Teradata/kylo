package com.thinkbiganalytics.spark.interpreter;

import com.fasterxml.jackson.databind.Module;

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
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.security.core.SecurityCoreConfig;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import com.thinkbiganalytics.spark.rest.model.Datasource;
import com.thinkbiganalytics.spark.shell.CatalogDataSetProvider;
import com.thinkbiganalytics.spark.shell.CatalogDataSetProviderFactory;
import com.thinkbiganalytics.spark.shell.DatasourceProvider;
import com.thinkbiganalytics.spark.shell.DatasourceProviderFactory;

import org.apache.spark.SparkConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.encrypt.EncryptionBootstrapConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Named;

import scala.tools.nsc.interpreter.NamedParam;
import scala.tools.nsc.interpreter.NamedParamClass;

/**
 * Reads in a Scala file with Spark code and executes it.
 */
@Component
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

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
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(SecurityCoreConfig.class);
        ctx.scan("com.thinkbiganalytics.spark", "com.thinkbiganalytics.kylo.catalog");
        ctx.refresh();

        File scriptFile = new File(args[0]);
        if (scriptFile.exists() && scriptFile.isFile()) {
            log.info("Loading script file at {} ", args[0]);
        } else {
            log.info("Couldn't find script file at {} will check classpath.", args[0]);
            String fileName = scriptFile.getName();
            scriptFile = new File("./" + fileName);
        }

        final String script = Files.toString(scriptFile, Charsets.UTF_8);

        // Prepare bindings
        final List<NamedParam> bindings = new ArrayList<>();

        final DatasourceProvider datasourceProvider = ctx.getBean(DatasourceProvider.class);
        bindings.add(new NamedParamClass("datasourceProvider", datasourceProvider.getClass().getName(), datasourceProvider));

        final CatalogDataSetProvider catalogDataSetProvider = ctx.getBean(CatalogDataSetProvider.class);
        bindings.add(new NamedParamClass("catalogDataSetProvider", catalogDataSetProvider.getClass().getName(), catalogDataSetProvider));

        // Execute script
        final SparkScriptEngine engine = ctx.getBean(SparkScriptEngine.class);
        engine.eval(script, bindings);
    }

    /**
     * Creates a data source provider.
     *
     * @param datasourceProviderFactory the data source provider factory
     * @return the data source provider
     * @throws IOException if an I/O error occurs
     */
    @Bean
    public DatasourceProvider datasourceProvider(final DatasourceProviderFactory datasourceProviderFactory,
                                                 final @Named("decryptionModule") Module decryptionModule) throws IOException {
        final List<Datasource> datasources;
        final String env = System.getenv("DATASOURCES");

        final List<DataSource> catalogDataSources;
        final String catalogDatasourcesEnv = System.getenv("CATALOG_DATASOURCES");

        if (env != null) {
            datasources = new ObjectMapper().registerModule(decryptionModule).readValue(env, TypeFactory.defaultInstance().constructCollectionType(List.class, Datasource.class));
        } else {
            datasources = Collections.emptyList();
        }
        if (catalogDatasourcesEnv != null) {
            catalogDataSources = new ObjectMapper().registerModule(decryptionModule).readValue(catalogDatasourcesEnv, TypeFactory.defaultInstance().constructCollectionType(List.class, DataSource.class));
        } else {
            catalogDataSources = Collections.emptyList();
        }

        return datasourceProviderFactory.getDatasourceProvider(datasources, catalogDataSources);
    }

    @Bean
    public CatalogDataSetProvider catalogDataSetProvider(final CatalogDataSetProviderFactory catalogDataSetProviderFactory,
                                                         final @Named("decryptionModule") Module decryptionModule) throws IOException {
        final List<DataSet> dataSets;
        final String env = System.getenv("DATASETS");

        if (env != null) {
            dataSets = new ObjectMapper().registerModule(decryptionModule).readValue(env, TypeFactory.defaultInstance().constructCollectionType(List.class, DataSet.class));
        } else {
            dataSets = Collections.emptyList();
        }

        return catalogDataSetProviderFactory.getDataSetProvider(dataSets);
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
