package com.thinkbiganalytics.spark.metadata

import java.util
import java.util.Collections
import java.util.concurrent.Callable

import com.thinkbiganalytics.discovery.schema.QueryResultColumn
import com.thinkbiganalytics.policy.rest.model.FieldPolicy
import com.thinkbiganalytics.policy.{FieldPoliciesJsonTransformer, FieldPolicyBuilder}
import com.thinkbiganalytics.spark.DataSet
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow
import com.thinkbiganalytics.spark.dataprofiler.{Profiler, ProfilerConfiguration}
import com.thinkbiganalytics.spark.datavalidator.DataValidator
import com.thinkbiganalytics.spark.model.AsyncTransformResponse
import com.thinkbiganalytics.spark.rest.model.{TransformQueryResult, TransformResponse}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/** Wraps a transform script into a function that can be evaluated.
  *
  * @param destination the name of the destination Hive table
  */
abstract class TransformScript(destination: String, policies: Array[FieldPolicy], validator: DataValidator, profiler: Profiler) {

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

    /** Creates a new [[DataSet]] from the specified rows and schema. */
    protected def toDataSet(rows: RDD[Row], schema: StructType): DataSet

    /** Stores the `DataFrame` results in a [[QueryResultColumn]] and returns the object. */
    protected abstract class QueryResultCallable extends Callable[TransformResponse] {

        /** Builds an asynchronous response model from a data set result. */
        def toAsyncResponse(dataset: DataSet): AsyncTransformResponse = {
            val result = new TransformQueryResult("SELECT * FROM " + destination)
            result.setColumns(new QueryResultRowTransform(dataset.schema(), destination).columns.toSeq)

            val response = new AsyncTransformResponse(this)
            response.setProgress(0.0)
            response.setResults(result)
            response.setStatus(TransformResponse.Status.PENDING)
            response.setTable(destination)
            response
        }

        /** Builds a response model from a data set result.
          *
          * @param dataset the data set
          * @return the response model
          */
        private[metadata] def toResponse(dataset: DataSet) = {
            // Build the result set
            val result = new TransformQueryResult("SELECT * FROM " + destination)

            // Validate rows
            var profile: util.List[OutputRow] = null
            val useValidation = policies != null && !policies.isEmpty && validator != null
            var rows: DataSet = null

            if (useValidation) {
                val validatorResult = validator.validate(dataset, getPolicyMap(dataset.schema()))
                profile = validator.getProfileStats(validatorResult)
                rows = toDataSet(validatorResult.getCleansedRowResultRDD.rdd.map(_.getRow), validatorResult.getSchema)
            } else {
                profile = Collections.emptyList()
                rows = dataset
            }

            // Add rows to result
            val transform = if (useValidation) new QueryResultValidatorRowTransform(rows.schema(), destination) else new QueryResultRowTransform(rows.schema(), destination)
            result.setColumns(transform.columns.toSeq)
            for (row <- rows.collectAsList()) {
                transform.addRow(row, result)
            }

            // Generate the column statistics
            profile = Option(profiler)
                .flatMap(p => {
                    val source = if (useValidation) rows.drop(rows.schema().fieldNames.last) else rows
                    Option(p.profile(source, new ProfilerConfiguration))
                })
                .map(_.getColumnStatisticsMap.asScala)
                .map(_.flatMap(_._2.getStatistics))
                .map(_.toSeq)
                .map(_ ++ profile)
                .map(seqAsJavaList)
                .orNull

            // Build the response
            val response = new TransformResponse
            response.setProfile(profile)
            response.setResults(result)
            response.setStatus(TransformResponse.Status.SUCCESS)
            response.setTable(destination)
            response
        }
    }

    /** Gets the policy map for the specified schema. */
    private def getPolicyMap(schema: StructType): util.Map[String, com.thinkbiganalytics.policy.FieldPolicy] = {
        val policyMap = new FieldPoliciesJsonTransformer(util.Arrays.asList(policies: _*)).buildPolicies()
        schema.fields
            .map(field => field.name.toLowerCase.trim)
            .filterNot(name => policyMap.containsKey(name))
            .map(name => FieldPolicyBuilder.newBuilder().fieldName(name).feedFieldName(name).build())
            .foreach(policy => policyMap.put(policy.getField, policy))
        policyMap
    }
}
