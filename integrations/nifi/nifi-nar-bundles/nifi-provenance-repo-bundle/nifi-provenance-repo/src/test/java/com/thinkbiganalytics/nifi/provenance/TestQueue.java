package com.thinkbiganalytics.nifi.provenance;/*-
 * #%L
 * thinkbig-nifi-provenance-repo
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.thinkbiganalytics.nifi.provenance.repo.KyloProvenanceEventConsumer;

import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.joda.time.DateTime;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sr186054 on 6/8/17.
 */
public class TestQueue {

    private static final Logger log = LoggerFactory.getLogger(TestQueue.class);

    ProvenanceEventObjectPool provenanceEventObjectPool = null;
    private   BlockingQueue<Map.Entry<Long,ProvenanceEventRecord>> processingQueue;
    TestProducer producer = null;

    private KyloProvenanceEventConsumer consumer;

  public  TestQueue() {
        init();
    }

        private void init(){
        this.processingQueue = new LinkedBlockingQueue<>();
        this.producer = new TestProducer(this.processingQueue);
            consumer = new KyloProvenanceEventConsumer(processingQueue,false);
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(5000);
            config.setMaxIdle(5000);
            config.setBlockWhenExhausted(false);
            config.setTestOnBorrow(false);
            config.setTestOnReturn(false);
            AbandonedConfig abandonedConfig = new AbandonedConfig();
            provenanceEventObjectPool =  new ProvenanceEventObjectPool(new ProvenanceEventObjectFactory(), config, abandonedConfig);
            consumer.setObjectPool(provenanceEventObjectPool);
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(this.consumer);

        }


    public void run() {

            long RUN_TIME = 40000L;
            //run for 20 sec
            int pauseAfterEvents = 100;
            long sleepTime = 0L;

            int maxEvents = 1000000;
        AtomicInteger count = new AtomicInteger(maxEvents);
        boolean exhaustedCreation = false;
        DateTime startAdditionalProcessingTime = null;
        DateTime startTime = DateTime.now();
        while(System.currentTimeMillis() - startTime.getMillis() < RUN_TIME){
            if(count.get() > 0) {
                //sleep ever 100 events
                if(count.get() % pauseAfterEvents == 0) {
                try {
                    Thread.sleep(sleepTime);
                }catch(Exception e){
                    ;
                }
                }

                this.producer.create(true);
                count.decrementAndGet();
            }
            else {
                if(exhaustedCreation == false){
                    exhaustedCreation = true;
                    startAdditionalProcessingTime = DateTime.now();
                }
            }
        }
        log.info("Events processed: {}, TotalTime: {}, Additional Time {}, skippedEvents:{} ",maxEvents - count.get(),(DateTime.now().getMillis() - startTime.getMillis()), startAdditionalProcessingTime != null ? (DateTime.now().getMillis() - startAdditionalProcessingTime.getMillis()): 0,producer.getSkippedCount());


    }

    @Test
    public void testIt(){
           run();
    }



}
