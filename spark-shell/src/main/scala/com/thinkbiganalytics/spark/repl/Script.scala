package com.thinkbiganalytics.spark.repl

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

/**
  * Wraps a Scala script into a function that can be evaluated.
  *
  * @param engine the script engine
  */
abstract class Script(engine: ScriptEngine) extends Runnable {
    /**
      * Evaluates this script and passes the result to the script engine.
      */
    override def run(): Unit = {
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

    /**
      * Evaluates the script.
      *
      * @throws java.lang.Throwable if an error occurs
      * @return the result
      */
    @throws(classOf[Throwable])
    protected def eval(): Any

    /**
      * Gets the value for a bound variable.
      *
      * @param name the name of the variable
      * @tparam T the type
      * @return the value
      */
    protected def getValue[T](name: String): T = {
        engine.getValue(name).asInstanceOf[T]
    }

    /**
      * Gets the spark context used by the script engine.
      *
      * @return the spark context
      */
    protected def sc: SparkContext = {
        engine.getSparkContext
    }

    /**
      * Gets the SQL context used by the script engine.
      *
      * @return the SQL context
      */
    protected def sqlContext: SQLContext = {
        engine.getSQLContext
    }
}
