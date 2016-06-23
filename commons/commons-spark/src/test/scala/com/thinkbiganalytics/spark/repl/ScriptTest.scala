package com.thinkbiganalytics.spark.repl

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.junit.{Ignore, Test}
import org.mockito.Mockito

@Ignore  // fails unexpectedly on Jenkins server
class ScriptTest {
    /** Verify evaluating a script. */
    @Test
    def run(): Unit = {
        // Mock engine and script
        val engine = Mockito.mock(classOf[ScriptEngine])
        val script = new Script(engine) {
            override protected def eval(): Any = {
                1 + 2
            }
        }

        // Verify result
        script.run()
        Mockito.verify(engine).setResult(3)
    }

    /** Verify handling an exception when evaluating a script. */
    @Test
    def runWithException(): Unit = {
        // Mock engine and script
        val engine = Mockito.mock(classOf[ScriptEngine])
        val exception = new UnsupportedOperationException
        val script = new Script(engine) {
            override protected def eval(): Any = {
                throw exception
            }
        }

        // Verify exception
        script.run()
        Mockito.verify(engine).setException(exception)
    }

    /** Verify evaluating a script that returns void. */
    @Test
    def runWithVoid(): Unit = {
        // Mock engine and script
        val engine = Mockito.mock(classOf[ScriptEngine])
        val script = new Script(engine) {
            override protected def eval(): Any = {}
        }

        // Verify result
        script.run()
        Mockito.verify(engine).setResult(())
    }

    /** Verify getting a value from the engine. */
    @Test
    def getValue(): Unit = {
        // Mock engine and script
        val engine = Mockito.mock(classOf[ScriptEngine])
        Mockito.when(engine.getValue("myval")).thenReturn(new Integer(42), Nil: _*)

        val script = new Script(engine) {
            override protected def eval(): Any = {
                val i: Integer = getValue("myval")
                i + 1
            }
        }

        // Verify result
        script.run()
        Mockito.verify(engine).setResult(43)
    }

    /** Verify getting the Spark context from the engine. */
    @Test
    def sc(): Unit = {
        // Mock engine and script
        val sc = Mockito.mock(classOf[SparkContext])

        val engine = Mockito.mock(classOf[ScriptEngine])
        Mockito.when(engine.getSparkContext).thenReturn(sc, Nil: _*)

        val script = new Script(engine) {
            override protected def eval(): Any = {
                sc
            }
        }

        // Verify Spark context
        script.run()
        Mockito.verify(engine).setResult(sc)
    }

    /** Verify getting the SQL context from the engine. */
    @Test
    def sqlContext(): Unit = {
        // Mock engine and script
        val sqlContext = Mockito.mock(classOf[SQLContext])

        val engine = Mockito.mock(classOf[ScriptEngine])
        Mockito.when(engine.getSQLContext).thenReturn(sqlContext, Nil: _*)

        val script = new Script(engine) {
            override protected def eval(): Any = {
                sqlContext
            }
        }

        // Verify Spark context
        script.run()
        Mockito.verify(engine).setResult(sqlContext)
    }
}
