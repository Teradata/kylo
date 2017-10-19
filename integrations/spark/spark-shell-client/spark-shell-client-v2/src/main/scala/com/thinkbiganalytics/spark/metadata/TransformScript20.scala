package com.thinkbiganalytics.spark.metadata

import com.thinkbiganalytics.policy.rest.model.FieldPolicy
import com.thinkbiganalytics.spark.SparkContextService
import com.thinkbiganalytics.spark.dataprofiler.Profiler
import com.thinkbiganalytics.spark.datavalidator.DataValidator
import com.thinkbiganalytics.spark.rest.model.TransformResponse
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.slf4j.LoggerFactory

/** Wraps a transform script into a function that can be evaluated.
  *
  * @param destination the name of the destination Hive table
  * @param sqlContext  the Spark SQL context
  */
abstract class TransformScript20(destination: String, policies: Array[FieldPolicy], validator: DataValidator, profiler: Profiler, sqlContext: SQLContext, sparkContextService: SparkContextService) extends TransformScript(destination, policies, validator, profiler) {

    private[this] val log = LoggerFactory.getLogger(classOf[TransformScript])

    /** Evaluates this transform script and stores the result in a Hive table. */
    def run(): QueryResultCallable = {
        new QueryResultCallable20
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

    /** Re-generates the parent transformation.
      *
      * @return the parent transformation
      */
    protected override def parentDataFrame: DataFrame = {
        throw new UnsupportedOperationException
    }

    override protected def toDataSet(rows: RDD[Row], schema: StructType) = {
        sparkContextService.toDataSet(sqlContext, rows, schema)
    }

    /** Stores the `DataFrame` results in a [[com.thinkbiganalytics.discovery.schema.QueryResult]] and returns the object. */
    private class QueryResultCallable20 extends QueryResultCallable {
        override def call(): TransformResponse = {
            // Cache data frame
            val cache = dataFrame.cache
            cache.registerTempTable(destination)

            // Build response object
            toResponse(sparkContextService.toDataSet(cache))
        }
    }

}
