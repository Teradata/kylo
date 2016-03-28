package com.thinkbiganalytics.spark.repl

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

abstract class Script (engine: ScriptEngine) extends Runnable
{
    override def run (): Unit = {
        try {
            val result: Any = eval()
            if (result == Unit) {
                engine.setResult(null)
            }
            else {
                engine.setResult(result)
            }
        }
        catch {
            case t: Throwable => engine.setException(t)
        }
    }

    @throws(classOf[Throwable])
    protected def eval (): Any

    protected def getValue[T] (name: String): T = {
        engine.getValue(name).asInstanceOf[T]
    }

    protected def sc: SparkContext = {
        engine.getSparkContext
    }

    protected def sqlContext: SQLContext = {
        engine.getSQLContext
    }
}
