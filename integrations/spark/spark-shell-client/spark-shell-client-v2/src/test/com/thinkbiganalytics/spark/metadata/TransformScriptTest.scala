package com.thinkbiganalytics.spark.metadata

import java.util
import java.util.concurrent.Callable

import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, _}

class TransformScriptTest {

    /** Verify running and inserting into Hive. */
    @Test
    def runWithInsertHive(): Unit = {
        val schema = DataTypes.createStructType(Array(DataTypes.createStructField("id", IntegerType, false), DataTypes.createStructField("name", StringType, false)))
        val script = new TransformScript("target", false, Mockito.mock(classOf[SQLContext])) {
            override def dataFrame: DataFrame = null
        }

        Assert.assertEquals("CREATE TABLE `target`(`id` int, `name` string) STORED AS ORC", new script.InsertHiveCallable().toSQL(schema))
    }

    /** Verify running and returning the results. */
    @Test
    def runWithQueryResult(): Unit = {
        // Mock DataFrame
        val mockDataFrame = Mockito.mock(classOf[DataFrame])
        Mockito.when(mockDataFrame.cache()).thenReturn(mockDataFrame)
        Mockito.when(mockDataFrame.collect()).thenReturn(Array(Row(1, 42.0, Vectors.dense(Array(1.0, 2.0, 3.0)), "test1"), Row(2, 64.0, Vectors.dense(Array(2.0)), "test2")))
        Mockito.when(mockDataFrame.schema).thenReturn(StructType(StructField("id", LongType) :: StructField("SUM(amount)", DoubleType) :: StructField("LR(amount)", new VectorUDT)
                                                                 :: StructField("col2", StringType) :: Nil))

        // Test script result
        val script = new TransformScript("target", true, Mockito.mock(classOf[SQLContext])) {
            override def dataFrame: DataFrame = mockDataFrame
        }
        val transformResponse = script.run().asInstanceOf[Callable[TransformResponse]].call()
        Assert.assertEquals(TransformResponse.Status.SUCCESS, transformResponse.getStatus)
        Assert.assertEquals("target", transformResponse.getTable)

        val columns = transformResponse.getResults.getColumns
        Assert.assertEquals(4, columns.size())

        Assert.assertEquals("bigint", columns.get(0).getDataType)
        Assert.assertEquals("id", columns.get(0).getDisplayName)
        Assert.assertEquals("id", columns.get(0).getField)
        Assert.assertEquals("id", columns.get(0).getHiveColumnLabel)

        Assert.assertEquals("double", columns.get(1).getDataType)
        Assert.assertEquals("col1", columns.get(1).getDisplayName)
        Assert.assertEquals("col1", columns.get(1).getField)
        Assert.assertEquals("SUM(amount)", columns.get(1).getHiveColumnLabel)

        Assert.assertEquals("array<double>", columns.get(2).getDataType)
        Assert.assertEquals("col3", columns.get(2).getDisplayName)
        Assert.assertEquals("col3", columns.get(2).getField)
        Assert.assertEquals("LR(amount)", columns.get(2).getHiveColumnLabel)

        Assert.assertEquals("string", columns.get(3).getDataType)
        Assert.assertEquals("col2", columns.get(3).getDisplayName)
        Assert.assertEquals("col2", columns.get(3).getField)
        Assert.assertEquals("col2", columns.get(3).getHiveColumnLabel)

        val rows = transformResponse.getResults.getRows.asInstanceOf[util.List[util.Map[String, Any]]]
        Assert.assertEquals(2, rows.size())

        Assert.assertEquals(1, rows.get(0).get("id"))
        Assert.assertEquals(42.0, rows.get(0).get("col1"))
        Assert.assertArrayEquals(Array(1.0, 2.0, 3.0), rows.get(0).get("col3").asInstanceOf[Array[Double]], 0.1)
        Assert.assertEquals("test1", rows.get(0).get("col2"))

        Assert.assertEquals(2, rows.get(1).get("id"))
        Assert.assertEquals(64.0, rows.get(1).get("col1"))
        Assert.assertArrayEquals(Array(2.0), rows.get(1).get("col3").asInstanceOf[Array[Double]], 0.1)
        Assert.assertEquals("test2", rows.get(1).get("col2"))
    }

    /** Verify using parent DataFrame. */
    @Test
    def parentWithDataFrame(): Unit = {
        // Mock DataFrame and SQLContext
        val mockDataFrame = Mockito.mock(classOf[DataFrame])

        val dataFrameReader = Mockito.mock(classOf[DataFrameReader])
        Mockito.when(dataFrameReader.table("invalid")).thenThrow(new RuntimeException)

        val sqlContext = Mockito.mock(classOf[SQLContext])
        Mockito.when(sqlContext.read).thenReturn(dataFrameReader)

        // Test reading parent DataFrame
        val script = new TransformScript("dest", false, sqlContext) {
            override def dataFrame: DataFrame = parent
            override def parentDataFrame: DataFrame = mockDataFrame
            override def parentTable: String = "invalid"
        }

        Assert.assertEquals(mockDataFrame, script.dataFrame)
    }

    /** Verify using parent table. */
    @Test
    def parentWithTable(): Unit = {
        // Mock DataFrame and SQLContext
        val dataFrame = Mockito.mock(classOf[DataFrame])

        val dataFrameReader = Mockito.mock(classOf[DataFrameReader])
        Mockito.when(dataFrameReader.table("papa")).thenReturn(dataFrame)

        val sqlContext = Mockito.mock(classOf[SQLContext])
        Mockito.when(sqlContext.read).thenReturn(dataFrameReader)

        // Test reading parent table
        val script = new TransformScript("dest", false, sqlContext) {
            override def dataFrame: DataFrame = parent
            override def parentTable: String = "papa"
        }

        Assert.assertEquals(dataFrame, script.dataFrame)
    }
}
