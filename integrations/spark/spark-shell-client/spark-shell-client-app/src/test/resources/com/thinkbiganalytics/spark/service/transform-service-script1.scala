class Transform (sqlContext: org.apache.spark.sql.SQLContext, sparkContextService: com.thinkbiganalytics.spark.SparkContextService) extends com.thinkbiganalytics.spark.metadata.TransformScript(sqlContext, sparkContextService) {
override def dataFrame: org.apache.spark.sql.DataFrame = {sqlContext.range(1,10)}
}
new Transform(sqlContext, sparkContextService).run()
