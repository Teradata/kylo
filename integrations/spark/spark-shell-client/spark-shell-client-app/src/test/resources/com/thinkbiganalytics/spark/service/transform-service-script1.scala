class Transform (destination: String, policies: Array[com.thinkbiganalytics.policy.rest.model.FieldPolicy], validator: com.thinkbiganalytics.spark.datavalidator.DataValidator, profiler: com.thinkbiganalytics.spark.dataprofiler.Profiler, sqlContext: org.apache.spark.sql.SQLContext, sparkContextService: com.thinkbiganalytics.spark.SparkContextService) extends com.thinkbiganalytics.spark.metadata.TransformScript(destination, policies, validator, profiler, sqlContext, sparkContextService) {
override def dataFrame: org.apache.spark.sql.DataFrame = {sqlContext.range(1,10)}
}
new Transform(tableName, policies, validator, profiler, sqlContext, sparkContextService).run()
