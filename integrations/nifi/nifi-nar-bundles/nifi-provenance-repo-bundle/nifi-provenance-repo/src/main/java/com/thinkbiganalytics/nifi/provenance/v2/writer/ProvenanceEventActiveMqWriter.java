package com.thinkbiganalytics.nifi.provenance.v2.writer;

import com.thinkbiganalytics.activemq.ObjectMapperSerializer;
import com.thinkbiganalytics.activemq.SendJmsMessage;
import com.thinkbiganalytics.nifi.activemq.Topics;
import com.thinkbiganalytics.nifi.provenance.v2.ProvenanceEventConverter;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.jms.Topic;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Created by sr186054 on 3/3/16.
 */
@Component
public class ProvenanceEventActiveMqWriter extends AbstractProvenanceEventWriter {

    @Override
    public void setMaxEventId(Long l) {
        eventIdIncrementer.setId(l);
    }

    @Override
    public Long getMaxEventId() {
        return eventIdIncrementer.getId();
    }

    private TreeSet<ProvenanceEventDTO> unprocessedEvents = new TreeSet<ProvenanceEventDTO>(new Comparator<ProvenanceEventDTO>() {
        @Override
        public int compare(ProvenanceEventDTO o1, ProvenanceEventDTO o2) {
            if (o1 == null && o1 == null) {
                return 0;
            } else if (o1 == null && o2 != null) {
                return 1;
            } else if (o1 != null && o2 != null) {
                return -1;
            } else if (o1.getEventId().equals(o2.getEventId())) {
                return o1.getEventTime().compareTo(o2.getEventTime());
            } else {
                return o1.getEventId().compareTo(o2.getEventId());
            }
        }
    });

    @Autowired
    @Qualifier(Topics.NIFI_EVENT_TOPIC_BEAN)
    private Topic topic;

    @Autowired
    private SendJmsMessage sendJmsMessage;

    @Autowired
    ObjectMapperSerializer objectMapperSerializer;

    public ProvenanceEventActiveMqWriter() {

    }

    @PostConstruct
    public void postConstruct() {
        System.out.println("!!!!!!!!!!!!!!! CREATED NEW ProvenanceEventRecordActiveMQWriter ");
    }

    @Override
    public Long writeEvent(ProvenanceEventRecord event) {
        //String json = objectMapperSerializer.serialize(event);
        //sendJmsMessage.sendMessage(topic,json);
        ProvenanceEventDTO dto = ProvenanceEventConverter.convert(event);
        dto.setEventId(eventIdIncrementer.incrementAndGet());
        System.out.println("SENDING JMS PROVENANCE_EVENT for EVENT_ID: " + dto.getEventId() + ", COMPONENT_ID: " + event.getComponentId() + ", COMPONENT_TYPE: " + event.getComponentType() + ", EVENT_TYPE: " + event.getEventType());
        try {
            sendJmsMessage.sendObject(topic, dto);
        } catch (JmsException e) {
            e.printStackTrace();
            unprocessedEvents.add(dto);
            //TODO try these events later in a new thread/timer
        }

        return dto.getEventId();
    }


}
