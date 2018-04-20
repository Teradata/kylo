package com.thinkbiganalytics.spark.service

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters.Converter
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorConverters, ObjectInspectorFactory}
import org.apache.spark.mllib.linalg.{Vector, VectorUDT}
import org.apache.spark.sql.types.DataType

class DataSetConverterServiceV1 extends AbstractDataSetConverterService {
    /** Finds a converter for the specified Spark SQL type.
      *
      * @param dataType the Spark SQL type
      * @return the converter to a Hive object
      */
    override protected def findHiveObjectConverter(dataType: DataType): Option[ObjectInspectorConverters.Converter] = dataType match {
        case _: VectorUDT => Option(new Converter {
            override def convert(o: Object): Object = o.asInstanceOf[Vector].toArray
        })
        case _ => Option.empty
    }

    /** Finds a Hive ObjectInspector for the specified Spark SQL type.
      *
      * @param dataType the Spark SQL type
      * @return the Hive ObjectInspector
      */
    override protected def findHiveObjectInspector(dataType: DataType): Option[ObjectInspector] = dataType match {
        case _: VectorUDT => Option(ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaDoubleObjectInspector))
        case _ => Option.empty
    }
}
