class Transform (destination: String, sendResults: Boolean, sqlContext: org.apache.spark.sql.SQLContext) extends com.thinkbiganalytics.spark.metadata.TransformScript(destination, sendResults, sqlContext) {
override def dataFrame: org.apache.spark.sql.DataFrame = {sqlContext.range(1,10)}
}
new Transform(tableName, false, sqlContext).run()
