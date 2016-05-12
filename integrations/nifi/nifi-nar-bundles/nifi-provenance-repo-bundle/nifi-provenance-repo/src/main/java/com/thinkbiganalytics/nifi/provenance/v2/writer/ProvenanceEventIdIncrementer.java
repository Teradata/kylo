package com.thinkbiganalytics.nifi.provenance.v2.writer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 3/4/16.
 */
public class ProvenanceEventIdIncrementer {

    private AtomicLong eventIdCounter = null;

    public boolean isNull() {
        return eventIdCounter == null;
    }

    public void setId(Long l) {
        System.out.println("EVENT ID SET TO " + l);
        if(l == null){
            l = 0L;
        }
        if (eventIdCounter == null) {
            eventIdCounter = new AtomicLong(l);
        } else {
            eventIdCounter.set(l);
        }
    }

    public Long getId() {
        if (eventIdCounter == null) {
            eventIdCounter = new AtomicLong(0L);
        }
        return eventIdCounter.get();
    }


    public Long getAndIncrement() {
        getId();
        return eventIdCounter.getAndIncrement();
    }

    public Long incrementAndGet() {
        getId();
        return eventIdCounter.incrementAndGet();
    }

}
