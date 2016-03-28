package com.thinkbiganalytics.spark.repl;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.util.Utils;

import scala.Function1;
import scala.collection.immutable.List;
import scala.runtime.BoxedUnit;
import scala.tools.nsc.GenericRunnerSettings;
import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

public class ScalaScriptEngine extends ScriptEngine
{
    private static final String OUTPUTDIR = "spark.repl.class.outputDir";

    @Nonnull
    private final SparkConf conf;

    @Nullable
    private IMain interpreter;

    public ScalaScriptEngine (@Nonnull final SparkConf conf)
    {
        this.conf = conf;
    }

    @Nonnull
    @Override
    protected SparkContext createSparkContext ()
    {
        // Set output directory
        String rootDir = this.conf.get("spark.repl.classdir", Utils.getLocalDir(this.conf));
        File outputDir = Utils.createTempDir(rootDir, "repl");
        this.conf.set("spark.repl.class.outputDir", outputDir.getAbsolutePath());

        // Create Spark context
        return new SparkContext(this.conf);
    }

    @Override
    protected void execute (@Nonnull String script)
    {
        getInterpreter().interpret(script);
    }

    @Nonnull
    private IMain getInterpreter ()
    {
        if (this.interpreter == null) {
            List<String> interpArguments = List.fromArray(new String[] {"-Yrepl-class-based",
                    "-Yrepl-outdir", getOutputDirectory()});

//            GenericRunnerSettings settings = new GenericRunnerSettings(
//                    new Function1<String,BoxedUnit>() {
//                        @Override
//                        public BoxedUnit apply (final String v1) {
//                            System.err.println(v1);
//                            return BoxedUnit.UNIT;
//                        }
//                    });
            Settings settings = new Settings();
            settings.processArguments(interpArguments, true);

            // Initialize engine
            this.interpreter = new IMain(settings, getPrintWriter());
            this.interpreter.initializeSynchronous();

            // Setup environment
            this.interpreter.bind("engine", ScalaScriptEngine.class.getName(), this,
                    List.<String>empty());
        }
        return this.interpreter;
    }

    @Nonnull
    private String getOutputDirectory ()
    {
        return getSparkContext().getConf().get(OUTPUTDIR);
    }
}
