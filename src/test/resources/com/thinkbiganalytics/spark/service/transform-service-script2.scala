class Transform (destination: String, sqlContext: org.apache.spark.sql.SQLContext) extends com.thinkbiganalytics.spark.metadata.TransformScript(destination, sqlContext) {
override def dataFrame: org.apache.spark.sql.DataFrame = {parent.withColumn(functions.expr("id+1")}
override def parentDataFrame: org.apache.spark.sql.DataFrame = {sqlContext.range(1,10)}
override def parentTable: String = {"parent_table"}
}
new Transform(tableName, sqlContext).run()
