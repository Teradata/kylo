/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark;

import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.hive.HiveContext;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GroovySpark implements Serializable {


    private URI path;

    public GroovySpark(URI path) {
        this.path = path;
    }

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
            System.out.println(e);
        }


    }

}
