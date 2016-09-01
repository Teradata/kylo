package com.thinkbiganalytics.nifi.provenance;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Consume the Events added by the DelayedProvenanceEventProducer after the delay has expired After consuming the events it will process each event by: - Collect the events in a group by Feed. - for
 * each group Process the events and determine if the event is a Stream (Rapid fire of events) or a Batch based upon the StreamConfiguration that was provided.
 *
 * Created by sr186054 on 8/14/16.
 */
public abstract class BatchedQueue<T> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BatchedQueue.class);

    private BlockingQueue<T> queue;

    private Long delayMillis;

    private DateTime lastTakeTime = null;
    private DateTime nextBatchTime = null;


    public BatchedQueue(Long delayMillis, BlockingQueue<T> queue) {
        this.queue = queue;
        this.lastTakeTime = DateTime.now();
        this.delayMillis = delayMillis;
        nextBatchTime = (lastTakeTime.plus(delayMillis));
        log.info("New Batched.  will attempt to batched items at {} ", nextBatchTime);
    }

    public abstract void processQueue(List<T> elements);


    private List<T> takeAll() throws InterruptedException {
        List<T> expired = new ArrayList<>();
        expired.add(queue.take());
        this.lastTakeTime = DateTime.now();
        nextBatchTime = (lastTakeTime.plus(this.delayMillis));
        queue.drainTo(expired);
        return expired;
    }


    private void process() throws InterruptedException {

        DateTime now = DateTime.now();
        DateTime lastTakeTime = this.lastTakeTime;
        if (now.isAfter(this.nextBatchTime)) {
            //everything in queue collect and process

            List<T> events = takeAll();

            processQueue(events);
            log.info("Take All from Queue for time {}, taken: {}, left: {}.  Next batch time: {} ", this.nextBatchTime, events.size(), this.queue.size(), this.nextBatchTime);


        }

    }


    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                process();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
