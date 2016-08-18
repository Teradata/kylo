package com.thinkbiganalytics.nifi.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowDeserializer;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 8/12/16.
 */
public class TestFlowGraph {

    private AtomicLong processorId = new AtomicLong(0L);


    private void connect(NifiFlowProcessGroup group, NifiFlowProcessor source, NifiFlowProcessor dest) {
        source.getDestinations().add(dest);
        dest.getSources().add(source);
        source.getDestinationIds().add(dest.getId());
        dest.getSourceIds().add(source.getId());
        group.getProcessorMap().put(source.getId(), source);
        group.getProcessorMap().put(dest.getId(), dest);
    }

    private NifiFlowProcessor processor() {
        return new NifiFlowProcessor(UUID.randomUUID().toString(), "Processor " + processorId.incrementAndGet());
    }


    @Test
    public void testDeserialization() throws IOException {

        //URL url = Resources.getResource("flows.json");
        //String json = Resources.toString(url, Charsets.UTF_8);

        NifiFlowProcessGroup group = new NifiFlowProcessGroup(UUID.randomUUID().toString(), "Group");

        NifiFlowProcessor processor1 = processor();
        NifiFlowProcessor processor2 = processor();
        NifiFlowProcessor processor3 = processor();
        NifiFlowProcessor processor4 = processor();
        NifiFlowProcessor processor5 = processor();
        NifiFlowProcessor processor6 = processor();
        group.getStartingProcessors().add(processor1);
        connect(group, processor1, processor2);
        connect(group, processor2, processor3);
        connect(group, processor2, processor4);
        connect(group, processor4, processor5);
        connect(group, processor5, processor6);
        //json the graph

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String json = mapper.writeValueAsString(group);

        try {
            NifiFlowProcessGroup flow = mapper.readValue(json, NifiFlowProcessGroup.class);
            NifiFlowDeserializer.constructGraph(flow);

            //test a group of them

            List<NifiFlowProcessGroup> groups = new ArrayList<>();
            groups.add(flow);
            groups.add(flow);
            groups.add(flow);
            if (groups != null) {
                groups.stream().forEach(g -> NifiFlowDeserializer.constructGraph(g));
                int i = 0;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
