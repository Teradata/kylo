package com.thinkbiganalytics.spark.service

import com.thinkbiganalytics.spark.metadata.StandardSparkListener
import org.apache.spark.SparkContext
import org.apache.spark.scheduler.SparkListener
import org.junit.Test
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.{Matchers, Mockito}

class SparkListenerService16Test {

    /** Verify that SparkListener.onOtherEvent() can be called for CDH 5.8+ */
    //noinspection ConvertExpressionToSAM
    @Test
    def testOnOtherEvent(): Unit = {
        // Mock SparkContext
        val sc = Mockito.mock(classOf[SparkContext])
        Mockito.when(sc.addSparkListener(Matchers.any())).thenAnswer(new Answer[Object] {
            override def answer(invocation: InvocationOnMock): Object = {
                invocation.getArgumentAt(0, classOf[SparkListener]).onOtherEvent(null)
                null
            }
        })

        // Test listener
        val service = new SparkListenerService16(sc)
        service.addSparkListener(Mockito.mock(classOf[StandardSparkListener]))
    }
}
