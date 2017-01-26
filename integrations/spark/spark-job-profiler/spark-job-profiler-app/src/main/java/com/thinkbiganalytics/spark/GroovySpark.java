package com.thinkbiganalytics.spark;

/*-
 * #%L
 * thinkbig-spark-job-profiler-app
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

import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.hive.HiveContext;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.script.Bindings;
import javax.script.ScriptException;

public class GroovySpark implements Serializable {


    private URI path;

    public GroovySpark(URI path) {
        this.path = path;
    }

    @SuppressWarnings("deprecation")
    public void doScript() throws ScriptException, IOException {
        Path pathToResource = Paths.get(path);
        if (!pathToResource.toFile().exists()) {
            throw new IOException(pathToResource + " does not exist");
        }
        StringWriter sw = new StringWriter();
        IOUtils.copy(new FileInputStream(pathToResource.toFile()), sw);
        GroovyScriptEngineFactory f = new GroovyScriptEngineFactory();

        SparkContext sparkContext = SparkContext.getOrCreate();
        HiveContext hiveContext = new org.apache.spark.sql.hive.HiveContext(sparkContext);

        Bindings bindings = f.getScriptEngine().createBindings();
        bindings.put("sparkContext", sparkContext);
        bindings.put("hiveContext", hiveContext);
        f.getScriptEngine().eval(sw.toString(), bindings);

    }

    public static void main(String[] args) throws Exception {
        try {
            if (args.length == 0) {
                System.out.println("Usage: <pathToResource>");
                System.exit(-1);
            }
            String path = args[0];
            GroovySpark gs = new GroovySpark(URI.create(path));
            gs.doScript();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
