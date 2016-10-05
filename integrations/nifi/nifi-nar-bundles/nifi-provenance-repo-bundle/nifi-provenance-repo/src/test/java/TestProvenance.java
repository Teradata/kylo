import com.google.common.collect.Lists;
import com.thinkbiganalytics.activemq.config.ActiveMqConfig;
import com.thinkbiganalytics.nifi.config.NifiProvenanceConfig;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.v2.ProvenanceEventRecordConverter;
import com.thinkbiganalytics.nifi.provenance.v2.writer.ProvenanceEventActiveMqWriter;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.provenance.StandardProvenanceEventRecord;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import javax.jms.JMSException;

/**
 * Created by sr186054 on 2/24/16.
 */
// Ignore due to dependency on MySQL
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ActiveMqConfig.class, NifiProvenanceConfig.class, TestConfig.class})
public class TestProvenance {


    @Autowired
    private ProvenanceEventActiveMqWriter activeMqWriter;

    //@Test
    public void sendSimpleMessage() throws InterruptedException, JMSException {

        Thread runner = new Thread(new TestProducer());
        runner.start();
        while (true) {
            //block
        }

    }

    private class TestProducer implements Runnable {

        int counter = 0;

        @Override
        public void run() {
            while (true) {
                counter++;
                ProvenanceEventRecord record = new StandardProvenanceEventRecord.Builder().setEventType(ProvenanceEventType.CONTENT_MODIFIED)
                        .setComponentId(UUID.randomUUID().toString())
                        .setFlowFileUUID(UUID.randomUUID().toString())
                        .setComponentType("PROCESSOR")
                        .setCurrentContentClaim("container", "section", "identifier", 0L, 1000L)
                        .build();

                ProvenanceEventRecordDTOHolder eventHolder = new ProvenanceEventRecordDTOHolder();
                eventHolder.setEvents(Lists.newArrayList(ProvenanceEventRecordConverter.convert(record)));
                activeMqWriter.writeEvents(eventHolder);
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
