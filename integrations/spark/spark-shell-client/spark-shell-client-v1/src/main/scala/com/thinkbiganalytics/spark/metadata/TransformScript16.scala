package com.thinkbiganalytics.spark.metadata

import com.thinkbiganalytics.discovery.schema.QueryResultColumn
import com.thinkbiganalytics.policy.rest.model.FieldPolicy
import com.thinkbiganalytics.spark.dataprofiler.Profiler
import com.thinkbiganalytics.spark.datavalidator.DataValidator
import com.thinkbiganalytics.spark.model.AsyncTransformResponse
import com.thinkbiganalytics.spark.rest.model.TransformResponse
import com.thinkbiganalytics.spark.{DataSet, SparkContextService}
import org.apache.hadoop.hive.ql.session.SessionState
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.slf4j.LoggerFactory

/** Wraps a transform script into a function that can be evaluated.
  *
  * @param destination the name of the destination Hive table
  * @param sqlContext  the Spark SQL context
  */
abstract class TransformScript16(destination: String, policies: Array[FieldPolicy], validator: DataValidator, profiler: Profiler, sqlContext: SQLContext, sparkContextService: SparkContextService) extends TransformScript(destination, policies, validator, profiler) {

    private[this] val log = LoggerFactory.getLogger(classOf[TransformScript])

    /** The evaluated and cached transform script. */
    private[this] lazy val dataSet = {
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
        sparkContextService.toDataSet(cache)
    }

    /** Evaluates this transform script and stores the result in a Hive table. */
    def run(): AsyncTransformResponse = {
        new QueryResultCallable16().toAsyncResponse(dataSet)
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

    /** Creates a new [[com.thinkbiganalytics.spark.DataSet]] from the specified rows and schema. */
    override protected def toDataSet(rows: RDD[Row], schema: StructType): DataSet = {
        sparkContextService.toDataSet(sqlContext, rows, schema)
    }

    /** Stores the `DataFrame` results in a [[QueryResultColumn]] and returns the object. */
    private class QueryResultCallable16 extends QueryResultCallable {
        override def call(): TransformResponse = {
            toResponse(dataSet)
        }
    }

}
