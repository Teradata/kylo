package com.thinkbiganalytics.provenance.rest;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventDtoBuilder;
import com.thinkbiganalytics.provenance.api.ProvenanceEventService;
import com.thinkbiganalytics.provenance.api.ProvenanceException;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestProvenanceRest {

   // @Test
    public void testProvenanceRest(){
        ProvenanceEventService restProvenanceEventService = new KyloRestProvenanceEventService();
        Map<String,String> params = new HashMap<>();
        params.put(KyloRestProvenanceEventService.USERNAME_CONFIG,"dladmin");
        params.put(KyloRestProvenanceEventService.PASSWORD_CONFIG,"thinkbig");
        params.put(KyloRestProvenanceEventService.HOST_CONFIG,"localhost");
        params.put(KyloRestProvenanceEventService.PORT_CONFIG,"8400");
        restProvenanceEventService.configure(params);

        String feedName = "provenance.provenance_test";
        String flowfileId = UUID.randomUUID().toString();
       DateTime startTime = DateTime.now().minusMinutes(1);
       Long start = startTime.getMillis();
        ProvenanceEventRecordDTO event1 = new ProvenanceEventDtoBuilder(feedName,flowfileId,"First Step").startingEvent(true).startTime(start).build();

        ProvenanceEventRecordDTO event2 = new ProvenanceEventDtoBuilder(feedName,flowfileId,"Second Step").startTime(startTime.plusSeconds(30).getMillis()).build();

        ProvenanceEventRecordDTO event3 = new ProvenanceEventDtoBuilder(feedName,flowfileId,"Final Step").endingEvent(true).build();

        List<ProvenanceEventRecordDTO> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);
        events.add(event3);
        try {
            restProvenanceEventService.sendEvents(events);
        }catch (ProvenanceException e)
        {
            e.printStackTrace();
        }
    }

}
