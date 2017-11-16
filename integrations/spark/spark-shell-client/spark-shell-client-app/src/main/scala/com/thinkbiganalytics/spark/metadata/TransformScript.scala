package com.thinkbiganalytics.spark.metadata

import com.thinkbiganalytics.spark.{DataSet, SparkContextService}

/** Wraps a transform script into a function that can be evaluated. */
abstract class TransformScript(sparkContextService: SparkContextService) {

    /** Evaluates this transform script and stores the result in a Hive table. */
    def run(): DataSet = {
        sparkContextService.toDataSet(dataFrame)
    }

    /** Evaluates the transform script.
      *
      * @return the transformation result
      */
    protected[metadata] def dataFrame: Object

    /** Fetches or re-generates the results of the parent transformation, if available.
      *
      * @return the parent results
      */
    protected def parent: Object

    /** Re-generates the parent transformation.
      *
      * @return the parent transformation
      */
    protected def parentDataFrame: Object = {
        throw new UnsupportedOperationException
    }

    /** Gets the name of the Hive table with the results of the parent transformation.
      *
      * @return the parent transformation
      */
    protected def parentTable: String = {
        throw new UnsupportedOperationException
    }
}
