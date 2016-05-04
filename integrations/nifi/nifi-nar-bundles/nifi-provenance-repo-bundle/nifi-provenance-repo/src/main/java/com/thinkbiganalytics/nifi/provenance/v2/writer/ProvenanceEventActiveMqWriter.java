package com.thinkbiganalytics.nifi.provenance.v2.writer;

import com.thinkbiganalytics.activemq.ObjectMapperSerializer;
import com.thinkbiganalytics.activemq.SendJmsMessage;
import com.thinkbiganalytics.nifi.activemq.ProvenanceEventReceiverDatabaseWriter;
import com.thinkbiganalytics.nifi.activemq.Topics;
import com.thinkbiganalytics.nifi.provenance.v2.ProvenanceEventConverter;
import com.thinkbiganalytics.util.SpringApplicationContext;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jms.JmsException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.jms.Topic;

import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 3/3/16.
 */
@Component
public class ProvenanceEventActiveMqWriter extends AbstractProvenanceEventWriter {

    private static final Logger logger = LoggerFactory.getLogger(ProvenanceEventActiveMqWriter.class);
    private boolean jmsUnavailable;
    private EmbeddedDatabase db;
    private Server h2Console;

    @Override
    public void setMaxEventId(Long l) {
        eventIdIncrementer.setId(l);
    }

    @Override
    public Long getMaxEventId() {
        return eventIdIncrementer.getId();
    }


    @Autowired
    @Qualifier(Topics.NIFI_EVENT_TOPIC_BEAN)
    private Topic topic;

    @Autowired
    private SendJmsMessage sendJmsMessage;

    @Autowired
    ObjectMapperSerializer objectMapperSerializer;

    @Autowired
    ProvenanceEventReceiverDatabaseWriter databaseWriter;


    @Value("${thinkbig.provenance.h2.databaseName:/opt/tba/tmp/thinkbig-nifi}")
    private String h2DatabaseName;

    @Value("${thinkbig.provenance.h2.showWebConsole:false}")
    private boolean startH2WebConsole;

    @Value("${thinkbig.provenance.h2.webConsolePort:8082}")
    private String h2WebConsolePort;

    public ProvenanceEventActiveMqWriter() {

    }

    @PostConstruct
    public void postConstruct() {
        logger.debug("!!!!!!!!!!!!!!! CREATED NEW ProvenanceEventRecordActiveMQWriter ");
    }

    @Override
    public Long writeEvent(ProvenanceEventRecord event) {
        ProvenanceEventDTO dto = ProvenanceEventConverter.convert(event);
        dto.setEventId(eventIdIncrementer.incrementAndGet());
        logger.debug(
            "SENDING JMS PROVENANCE_EVENT for EVENT_ID: " + dto.getEventId() + ", COMPONENT_ID: " + event.getComponentId()
            + ", COMPONENT_TYPE: " + event.getComponentType() + ", EVENT_TYPE: " + event.getEventType());
        try {
            if (jmsUnavailable) {
                persistEventToTemporaryTable(dto);
            } else {
                logger.info("Processing the JMS message as normal");
                sendJmsMessage.sendObject(topic, dto);
            }
        } catch (Exception e) {
            logger.error("JMS Error has occurred. Enable temporary queue", e);
            jmsUnavailable = true;
            try {
                initializeTemporaryDatabase();
                databaseWriter.writeEvent(dto);
            } catch (Exception dwe) {
                logger.error("Error writing the temporary provenance event to the database", dwe);
            }
        }

        return dto.getEventId();
    }

    private void initializeTemporaryDatabase() throws Exception {
        logger.info("Starting H2 database");
        db = new EmbeddedDatabaseBuilder()
            .generateUniqueName(false)
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .setName(h2DatabaseName)
            .build();
        if (startH2WebConsole) {
            logger.info("Starting the H2 web console");
            h2Console = Server.createWebServer("-web", "-webAllowOthers", "-webDaemon", "-webPort", h2WebConsolePort);
            h2Console.start();
        }
        logger.info("Started H2 database");

        databaseWriter.createTables();
    }

    private void persistEventToTemporaryTable(ProvenanceEventDTO dto) throws Exception {
        boolean isJmsRunningNow = sendJmsMessage.testJmsIsRunning();
        if (isJmsRunningNow) {
            logger.info("JMS is running now. Processing the cached messages");
            // catch up on the cached messages then send the last message
            List<ProvenanceEventDTO> eventsFromDatabase = databaseWriter.getEvents();
            for (ProvenanceEventDTO eventDTO : eventsFromDatabase) {
                sendJmsMessage.sendObject(topic, eventDTO);
            }
            databaseWriter.clearEvents();
            sendJmsMessage.sendObject(topic, dto);

            shutdownTemporaryDatabaseAndResumeJms();
        } else {
            logger.info("JMS server still down so caching the new message");
            databaseWriter.writeEvent(dto);
        }
    }

    private void shutdownTemporaryDatabaseAndResumeJms() {
        jmsUnavailable = false;
        if (h2Console != null) {
            h2Console.stop();
        }
        db.shutdown();
    }

}
