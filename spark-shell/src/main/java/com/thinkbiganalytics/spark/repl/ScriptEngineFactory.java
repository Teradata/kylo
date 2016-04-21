package com.thinkbiganalytics.spark.repl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.spark.SparkConf;

public class ScriptEngineFactory {

    @Nullable
    private static ScriptEngine INSTANCE;

    @Nonnull
    public static synchronized ScriptEngine getScriptEngine(@Nonnull final SparkConf conf) {
        if (INSTANCE == null) {
            INSTANCE = new SparkScriptEngine(conf);
        }
        return INSTANCE;
    }
}
