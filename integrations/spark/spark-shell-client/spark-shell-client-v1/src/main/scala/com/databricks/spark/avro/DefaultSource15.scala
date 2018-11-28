package com.databricks.spark.avro

import org.apache.spark.sql.sources.DataSourceRegister

class DefaultSource15 extends DefaultSource with DataSourceRegister {
    override def shortName(): String = "avro"
}
