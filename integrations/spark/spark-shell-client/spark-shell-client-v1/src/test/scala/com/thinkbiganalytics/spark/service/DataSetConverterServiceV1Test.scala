package com.thinkbiganalytics.spark.service

import org.apache.spark.mllib.linalg.{VectorUDT, Vectors}
import org.junit.{Assert, Test}

class DataSetConverterServiceV1Test {

    /** Verify converting Spark Vector to Hive object. */
    @Test
    def getHiveObjectConverter(): Unit = {
        val converterService = new DataSetConverterServiceV1
        val vectorConverter = converterService.getHiveObjectConverter(new VectorUDT)
        Assert.assertArrayEquals(Array(2.0, 4.0, 6.0), vectorConverter.convert(Vectors.dense(2.0, 4.0, 6.0)).asInstanceOf[Array[Double]], 0.1)
    }
}
