class Transform (destination: String, sqlContext: org.apache.spark.sql.SQLContext) extends com.thinkbiganalytics.spark.metadata.TransformScript(destination, sqlContext) {
override def dataFrame: org.apache.spark.sql.DataFrame = {sqlContext.range(1,10)}
}
new Transform(tableName, sqlContext).run()
