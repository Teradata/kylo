class Transform (destination: String, profiler: com.thinkbiganalytics.spark.dataprofiler.Profiler, sqlContext: org.apache.spark.sql.SQLContext, sparkContextService: com.thinkbiganalytics.spark.SparkContextService) extends com.thinkbiganalytics.spark.metadata.TransformScript(destination, profiler, sqlContext, sparkContextService) {
override def dataFrame: org.apache.spark.sql.DataFrame = {parent.withColumn(functions.expr("id+1")}
override def parentDataFrame: org.apache.spark.sql.DataFrame = {sqlContext.range(1,10)}
override def parentTable: String = {"parent_table"}
}
new Transform(tableName, profiler, sqlContext, sparkContextService).run()
