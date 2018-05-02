package com.thinkbiganalytics.spark.service

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters.Converter
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorConverters, ObjectInspectorFactory}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.types.DataType

object DataSetConverterServiceV2 {

    val VECTOR_TYPE = "vector"
}

class DataSetConverterServiceV2 extends AbstractDataSetConverterService {

    override protected def findHiveObjectConverter(dataType: DataType): Option[ObjectInspectorConverters.Converter] = {
        if (DataSetConverterServiceV2.VECTOR_TYPE.equals(dataType.typeName)) {
            Option(new Converter {
                override def convert(o: Object): Object = o.asInstanceOf[Vector].toArray
            })
        } else {
            Option.empty
        }
    }

    override protected def findHiveObjectInspector(dataType: DataType): Option[ObjectInspector] = {
        if (DataSetConverterServiceV2.VECTOR_TYPE.equals(dataType.typeName)) {
            Option(ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaDoubleObjectInspector))
        } else {
            Option.empty
        }
    }
}
