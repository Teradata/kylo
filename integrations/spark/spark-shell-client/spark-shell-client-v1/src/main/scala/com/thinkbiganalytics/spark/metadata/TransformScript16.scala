package com.thinkbiganalytics.spark.metadata

import com.thinkbiganalytics.spark.SparkContextService
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.slf4j.LoggerFactory

/** Wraps a transform script into a function that can be evaluated.
  *
  * @param sqlContext  the Spark SQL context
  */
abstract class TransformScript16(sqlContext: SQLContext, sparkContextService: SparkContextService) extends TransformScript(sparkContextService) {

    private[this] val log = LoggerFactory.getLogger(classOf[TransformScript])

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
}
