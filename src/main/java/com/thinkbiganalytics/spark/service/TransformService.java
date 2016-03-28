package com.thinkbiganalytics.spark.service;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.script.ScriptException;

import com.thinkbiganalytics.spark.metadata.TransformRequest;
import com.thinkbiganalytics.spark.repl.ScriptEngine;

public class TransformService
{
    @Nonnull
    private final ScriptEngine engine;

    public TransformService (@Nonnull final ScriptEngine engine)
    {
        this.engine = engine;
    }

    @Nonnull
    public String execute (@Nonnull final TransformRequest request) throws ScriptException
    {
        String uuid = UUID.randomUUID().toString().replace('-', '_');

        StringBuilder script = new StringBuilder();
        script.append("class Transform (destination: String, sqlContext: org.apache.spark.sql.SQLContext)");
        script.append("    extends com.thinkbiganalytics.spark.metadata.TransformScript(");
        script.append("    destination, sqlContext) {\n");
        script.append("  override def dataFrame: org.apache.spark.sql.DataFrame = {\n");
        script.append(request.getScript());
        script.append("  }\n");

        if (request.getParent() != null) {
            script.append("  override def parentDataFrame: org.apache.spark.sql.DataFrame = {\n");
            script.append(request.getParent().getScript());
            script.append("  }\n");
            script.append("  override def parentTable: String = {\n");
            script.append(request.getParent().getTable());
            script.append("  }\n");
        }

        script.append("}\n");
        script.append("new Transform(\"");
        script.append(uuid);
        script.append("\", sqlContext).run()\n");
        script.append("null\n");  // TODO

        this.engine.eval(script.toString());
        return uuid;
    }
}
