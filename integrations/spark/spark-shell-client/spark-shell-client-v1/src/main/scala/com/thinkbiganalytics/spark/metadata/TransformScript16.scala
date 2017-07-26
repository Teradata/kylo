package com.thinkbiganalytics.spark.metadata

import com.thinkbiganalytics.discovery.schema.QueryResultColumn
import com.thinkbiganalytics.spark.SparkContextService
import com.thinkbiganalytics.spark.dataprofiler.Profiler
import com.thinkbiganalytics.spark.rest.model.TransformResponse
import org.apache.hadoop.hive.ql.session.SessionState
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.slf4j.LoggerFactory

/** Wraps a transform script into a function that can be evaluated.
  *
  * @param destination the name of the destination Hive table
  * @param sqlContext  the Spark SQL context
  */
abstract class TransformScript16(destination: String, profiler: Profiler, sqlContext: SQLContext, sparkContextService: SparkContextService) extends TransformScript(destination, profiler) {

    private[this] val log = LoggerFactory.getLogger(classOf[TransformScript])

    /** Evaluates this transform script and stores the result in a Hive table. */
    def run(): QueryResultCallable = {
        new QueryResultCallable16
    }

    /** Evaluates the transform script.
      *
      * @return the transformation result
      */
    protected[metadata] def dataFrame: DataFrame

    /** Fetches or re-generates the results of the parent transformation, if available.
      *
      * @return the parent results
      */
    protected def parent: DataFrame = {
        try {
            sqlContext.read.table(parentTable)
        }
        catch {
            case e: Exception =>
                log.trace("Exception reading parent table: {}", e.toString)
                log.debug("Parent table not found: {}", parentTable)
                parentDataFrame
        }
    }

    protected override def parentDataFrame: DataFrame = {
        throw new UnsupportedOperationException
    }

    /** Stores the `DataFrame` results in a [[QueryResultColumn]] and returns the object. */
    private class QueryResultCallable16 extends QueryResultCallable {
        override def call(): TransformResponse = {
            // SPARK-17245 Create a new session if SessionState is unavailable
            if (SessionState.get() == null) {
                try {
                    sqlContext.setConf("com.thinkbiganalytics.spark.spark17245", "")
                } catch {
                    case _: NullPointerException => sqlContext.newSession()
                }
            }

            // Cache data frame
            val cache = dataFrame.cache
            cache.registerTempTable(destination)

            // Build response object
            toResponse(sparkContextService.toDataSet(cache))
        }
    }

}
